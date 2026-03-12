"""
LookForBest 推荐服务 (FastAPI + sentence-transformers)
提供三种推荐策略：文本相似度、协同过滤、热门机器人
"""
import os
import logging
from contextlib import asynccontextmanager
from typing import Optional

import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from pydantic_settings import BaseSettings
from sqlalchemy import create_engine, text
from sklearn.metrics.pairwise import cosine_similarity

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ============================================================
# 配置
# ============================================================
class Settings(BaseSettings):
    db_host: str = "localhost"
    db_port: int = 3306
    db_name: str = "lookforbest"
    db_user: str = "lookforbest"
    db_password: str = "lookforbest"
    port: int = 8000
    embedding_model: str = "paraphrase-multilingual-MiniLM-L12-v2"

    class Config:
        env_file = ".env"
        extra = "ignore"

settings = Settings()

# ============================================================
# 全局状态
# ============================================================
_model = None       # sentence-transformers model
_engine = None      # SQLAlchemy engine
_robot_cache: dict = {}      # id -> {name, description, category_id, ...}
_embeddings: Optional[np.ndarray] = None  # shape (N, dim)
_robot_ids: list = []         # 与 _embeddings 行对应的 robot_id 列表


def get_db():
    return _engine


def get_model():
    return _model


# ============================================================
# 启动 / 关闭
# ============================================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    global _model, _engine
    # 初始化数据库连接
    url = (f"mysql+pymysql://{settings.db_user}:{settings.db_password}"
           f"@{settings.db_host}:{settings.db_port}/{settings.db_name}?charset=utf8mb4")
    _engine = create_engine(url, pool_pre_ping=True, pool_recycle=3600)
    logger.info("数据库连接初始化完成")

    # 加载句向量模型
    try:
        from sentence_transformers import SentenceTransformer
        _model = SentenceTransformer(settings.embedding_model)
        logger.info(f"模型 {settings.embedding_model} 加载完成")
    except Exception as e:
        logger.warning(f"模型加载失败，文本相似度推荐不可用: {e}")

    # 预构建机器人嵌入缓存
    _build_embedding_cache()
    yield
    logger.info("推荐服务关闭")


def _build_embedding_cache():
    """从数据库加载所有机器人并构建 embedding 矩阵"""
    global _robot_cache, _embeddings, _robot_ids
    if _engine is None:
        return
    try:
        with _engine.connect() as conn:
            rows = conn.execute(text(
                "SELECT id, name, name_en, description, description_en, category_id, manufacturer_id "
                "FROM robots WHERE status = 'active'"
            )).fetchall()
        _robot_cache = {
            row[0]: {
                "id": row[0], "name": row[1], "nameEn": row[2],
                "description": row[3] or "", "descriptionEn": row[4] or "",
                "categoryId": row[5], "manufacturerId": row[6]
            }
            for row in rows
        }
        if _model and _robot_cache:
            _robot_ids = list(_robot_cache.keys())
            texts = [
                f"{_robot_cache[rid]['name']} {_robot_cache[rid]['nameEn'] or ''} "
                f"{_robot_cache[rid]['description'][:300]}"
                for rid in _robot_ids
            ]
            _embeddings = _model.encode(texts, show_progress_bar=False, normalize_embeddings=True)
            logger.info(f"嵌入缓存构建完成，共 {len(_robot_ids)} 个机器人")
    except Exception as e:
        logger.warning(f"构建嵌入缓存失败: {e}")


app = FastAPI(title="LookForBest 推荐服务", lifespan=lifespan)


# ============================================================
# 请求/响应模型
# ============================================================
class SimilarRequest(BaseModel):
    robotId: int
    size: int = 6
    strategy: str = "text"   # text | collab | popular | hybrid


class RecommendItem(BaseModel):
    robotId: int
    name: str
    nameEn: Optional[str] = None
    score: float
    strategy: str


class RecommendResponse(BaseModel):
    items: list[RecommendItem]
    strategy: str


# ============================================================
# 推荐策略实现
# ============================================================
def _text_similar(robot_id: int, size: int) -> list[RecommendItem]:
    """基于文本嵌入的余弦相似度推荐"""
    if _model is None or _embeddings is None or robot_id not in _robot_cache:
        return []
    if robot_id not in _robot_ids:
        return []
    idx = _robot_ids.index(robot_id)
    query_vec = _embeddings[idx].reshape(1, -1)
    sims = cosine_similarity(query_vec, _embeddings)[0]
    # 排除自身，取 top-N
    ranked = sorted(
        [(i, s) for i, s in enumerate(sims) if _robot_ids[i] != robot_id],
        key=lambda x: x[1], reverse=True
    )[:size]
    return [
        RecommendItem(
            robotId=_robot_ids[i],
            name=_robot_cache[_robot_ids[i]]["name"],
            nameEn=_robot_cache[_robot_ids[i]]["nameEn"],
            score=round(float(s), 4),
            strategy="text"
        )
        for i, s in ranked
    ]


def _collab_filter(robot_id: int, size: int) -> list[RecommendItem]:
    """基于用户行为日志的协同过滤：
    找出浏览过 robot_id 的 session，再找这些 session 中浏览最多的其他机器人"""
    if _engine is None:
        return []
    try:
        with _engine.connect() as conn:
            # 找出浏览过该机器人的会话
            sessions = conn.execute(text(
                "SELECT DISTINCT session_id FROM user_action_logs "
                "WHERE target_type='robot' AND action_type='view' AND target_id=:rid "
                "LIMIT 500"
            ), {"rid": robot_id}).scalars().all()

            if not sessions:
                return []

            # 找这些会话浏览的其他机器人
            result = conn.execute(text(
                "SELECT target_id, COUNT(*) AS cnt "
                "FROM user_action_logs "
                "WHERE session_id IN :sessions AND target_type='robot' "
                "AND action_type='view' AND target_id != :rid "
                "GROUP BY target_id ORDER BY cnt DESC LIMIT :size"
            ), {"sessions": tuple(sessions), "rid": robot_id, "size": size}).fetchall()

        items = []
        for row in result:
            rid2, cnt = row[0], row[1]
            if rid2 in _robot_cache:
                total = max(cnt, 1)
                items.append(RecommendItem(
                    robotId=rid2,
                    name=_robot_cache[rid2]["name"],
                    nameEn=_robot_cache[rid2]["nameEn"],
                    score=round(min(float(cnt) / 100, 1.0), 4),
                    strategy="collab"
                ))
        return items
    except Exception as e:
        logger.warning(f"协同过滤失败: {e}")
        return []


def _popular_robots(robot_id: int, size: int) -> list[RecommendItem]:
    """基于频率统计的热门机器人（同分类，按浏览量）"""
    if robot_id not in _robot_cache:
        return []
    cat_id = _robot_cache[robot_id]["categoryId"]
    try:
        with _engine.connect() as conn:
            rows = conn.execute(text(
                "SELECT id, name, name_en, view_count FROM robots "
                "WHERE category_id=:cat AND id!=:rid AND status='active' "
                "ORDER BY view_count DESC LIMIT :size"
            ), {"cat": cat_id, "rid": robot_id, "size": size}).fetchall()
        max_views = max((row[3] or 0 for row in rows), default=1)
        return [
            RecommendItem(
                robotId=row[0], name=row[1], nameEn=row[2],
                score=round((row[3] or 0) / max(max_views, 1), 4),
                strategy="popular"
            )
            for row in rows
        ]
    except Exception as e:
        logger.warning(f"热门推荐失败: {e}")
        return []


def _hybrid(robot_id: int, size: int) -> list[RecommendItem]:
    """混合策略：综合文本相似度 + 协同过滤 + 热门度"""
    text_items = _text_similar(robot_id, size * 2)
    collab_items = _collab_filter(robot_id, size * 2)
    popular_items = _popular_robots(robot_id, size * 2)

    scores: dict[int, float] = {}
    names: dict[int, tuple] = {}

    def add(items: list[RecommendItem], weight: float):
        for it in items:
            scores[it.robotId] = scores.get(it.robotId, 0) + it.score * weight
            names[it.robotId] = (it.name, it.nameEn)

    add(text_items, 0.5)
    add(collab_items, 0.35)
    add(popular_items, 0.15)

    ranked = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:size]
    return [
        RecommendItem(
            robotId=rid, name=names[rid][0], nameEn=names[rid][1],
            score=round(s, 4), strategy="hybrid"
        )
        for rid, s in ranked
    ]


# ============================================================
# API 端点
# ============================================================
@app.get("/health")
async def health():
    return {"status": "ok", "model": settings.embedding_model, "robots": len(_robot_cache)}


@app.post("/recommend/similar", response_model=RecommendResponse)
async def recommend_similar(req: SimilarRequest):
    """获取相似机器人推荐"""
    strategy = req.strategy
    if strategy == "text":
        items = _text_similar(req.robotId, req.size)
        if not items:
            # 降级到热门
            items = _popular_robots(req.robotId, req.size)
            strategy = "popular"
    elif strategy == "collab":
        items = _collab_filter(req.robotId, req.size)
        if not items:
            items = _popular_robots(req.robotId, req.size)
            strategy = "popular"
    elif strategy == "popular":
        items = _popular_robots(req.robotId, req.size)
    else:
        items = _hybrid(req.robotId, req.size)
        strategy = "hybrid"

    return RecommendResponse(items=items, strategy=strategy)


@app.get("/recommend/popular")
async def popular_global(size: int = 10, days: int = 7):
    """全站热门机器人（近N天行为日志）"""
    if _engine is None:
        raise HTTPException(503, "数据库不可用")
    try:
        from datetime import datetime, timedelta
        since = datetime.now() - timedelta(days=days)
        with _engine.connect() as conn:
            rows = conn.execute(text(
                "SELECT l.target_id, r.name, r.name_en, COUNT(*) AS cnt "
                "FROM user_action_logs l "
                "JOIN robots r ON r.id = l.target_id "
                "WHERE l.target_type='robot' AND l.action_type='view' "
                "AND l.created_at >= :since "
                "GROUP BY l.target_id, r.name, r.name_en "
                "ORDER BY cnt DESC LIMIT :size"
            ), {"since": since, "size": size}).fetchall()
        return {
            "items": [
                {"robotId": row[0], "name": row[1], "nameEn": row[2], "viewCount": row[3]}
                for row in rows
            ]
        }
    except Exception as e:
        raise HTTPException(500, str(e))


@app.post("/cache/rebuild")
async def rebuild_cache():
    """手动重建嵌入缓存（管理员调用）"""
    _build_embedding_cache()
    return {"status": "ok", "robots": len(_robot_cache)}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=settings.port, reload=False)
