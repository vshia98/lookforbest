"""
LookForBest 配置化爬虫服务 (FastAPI)
提供爬虫管理 REST API
"""
import json
import logging
import os
from contextlib import asynccontextmanager
from concurrent.futures import ThreadPoolExecutor
from typing import Optional

from dotenv import load_dotenv
load_dotenv()

from fastapi import FastAPI, HTTPException, BackgroundTasks, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field

from core.db import (
    get_connection, list_sources, get_source,
    create_source, update_source, delete_source,
    list_run_logs,
)
from core.engine import run_source
from core.scheduler import refresh_jobs, shutdown

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%H:%M:%S",
)
logger = logging.getLogger(__name__)

_executor = ThreadPoolExecutor(max_workers=4)
_running_jobs: dict[int, str] = {}  # source_id -> "running"


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("爬虫服务启动，加载调度任务...")
    refresh_jobs()
    yield
    logger.info("爬虫服务关闭")
    shutdown()
    _executor.shutdown(wait=False)


app = FastAPI(title="LookForBest Crawler Service", version="1.0.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_db():
    conn = get_connection()
    try:
        yield conn
    finally:
        conn.close()


# ─────────────────────────── Pydantic Models ─────────────────────────────

class SourceCreateRequest(BaseModel):
    name: str = Field(..., max_length=200)
    description: Optional[str] = None
    url_pattern: str = Field(..., max_length=2000, description="起始URL，多个用逗号分隔")
    config: dict = Field(..., description="爬取规则配置 JSON")
    is_active: bool = True
    cron_expr: str = "0 0 3 * * ?"
    max_pages: int = Field(default=10, ge=1, le=200)
    delay_ms: int = Field(default=1000, ge=100, le=30000)


class SourceUpdateRequest(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    url_pattern: Optional[str] = None
    config: Optional[dict] = None
    is_active: Optional[bool] = None
    cron_expr: Optional[str] = None
    max_pages: Optional[int] = None
    delay_ms: Optional[int] = None


# ──────────────────────────── Routes ───────────────────────────────────

@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/api/sources")
def list_crawler_sources(conn=Depends(get_db)):
    """获取所有爬虫数据源"""
    sources = list_sources(conn)
    for s in sources:
        if isinstance(s.get("config"), str):
            try:
                s["config"] = json.loads(s["config"])
            except Exception:
                pass
        if s.get("last_crawl_at"):
            s["last_crawl_at"] = str(s["last_crawl_at"])
        if s.get("created_at"):
            s["created_at"] = str(s["created_at"])
        if s.get("updated_at"):
            s["updated_at"] = str(s["updated_at"])
        s["is_running"] = _running_jobs.get(s["id"]) == "running"
    return {"data": sources}


@app.get("/api/sources/{source_id}")
def get_crawler_source(source_id: int, conn=Depends(get_db)):
    source = get_source(conn, source_id)
    if not source:
        raise HTTPException(404, "数据源不存在")
    if isinstance(source.get("config"), str):
        try:
            source["config"] = json.loads(source["config"])
        except Exception:
            pass
    for k in ["last_crawl_at", "created_at", "updated_at"]:
        if source.get(k):
            source[k] = str(source[k])
    return {"data": source}


@app.post("/api/sources", status_code=201)
def create_crawler_source(req: SourceCreateRequest, conn=Depends(get_db)):
    source_id = create_source(conn, {
        "name": req.name,
        "description": req.description,
        "url_pattern": req.url_pattern,
        "config": json.dumps(req.config, ensure_ascii=False),
        "is_active": 1 if req.is_active else 0,
        "cron_expr": req.cron_expr,
        "max_pages": req.max_pages,
        "delay_ms": req.delay_ms,
    })
    refresh_jobs()
    return {"data": {"id": source_id}, "message": "数据源创建成功"}


@app.put("/api/sources/{source_id}")
def update_crawler_source(source_id: int, req: SourceUpdateRequest, conn=Depends(get_db)):
    if not get_source(conn, source_id):
        raise HTTPException(404, "数据源不存在")
    updates = req.model_dump(exclude_none=True)
    if "config" in updates:
        updates["config"] = json.dumps(updates["config"], ensure_ascii=False)
    if "is_active" in updates:
        updates["is_active"] = 1 if updates["is_active"] else 0
    if not updates:
        return {"message": "无需更新"}
    update_source(conn, source_id, updates)
    refresh_jobs()
    return {"message": "更新成功"}


@app.delete("/api/sources/{source_id}")
def delete_crawler_source(source_id: int, conn=Depends(get_db)):
    if not get_source(conn, source_id):
        raise HTTPException(404, "数据源不存在")
    delete_source(conn, source_id)
    refresh_jobs()
    return {"message": "删除成功"}


@app.post("/api/sources/{source_id}/run")
def trigger_crawl(source_id: int, background_tasks: BackgroundTasks, conn=Depends(get_db)):
    """手动触发单个数据源爬取"""
    if not get_source(conn, source_id):
        raise HTTPException(404, "数据源不存在")
    if _running_jobs.get(source_id) == "running":
        raise HTTPException(409, "该数据源正在运行中")

    def _run():
        _running_jobs[source_id] = "running"
        try:
            run_source(source_id, triggered_by="manual")
        finally:
            _running_jobs.pop(source_id, None)

    background_tasks.add_task(_run)
    return {"message": "爬取任务已提交"}


@app.post("/api/run-all")
def trigger_all_crawl(background_tasks: BackgroundTasks, conn=Depends(get_db)):
    """触发所有活跃数据源爬取"""
    sources = list_sources(conn, active_only=True)
    triggered = []
    for s in sources:
        if _running_jobs.get(s["id"]) != "running":
            def _run(sid=s["id"]):
                _running_jobs[sid] = "running"
                try:
                    run_source(sid, triggered_by="manual")
                finally:
                    _running_jobs.pop(sid, None)
            background_tasks.add_task(_run)
            triggered.append(s["id"])
    return {"message": f"已触发 {len(triggered)} 个数据源", "triggered": triggered}


@app.get("/api/logs")
def get_run_logs(source_id: Optional[int] = None, limit: int = 50, conn=Depends(get_db)):
    """获取爬取运行日志"""
    logs = list_run_logs(conn, source_id=source_id, limit=min(limit, 200))
    for log in logs:
        for k in ["started_at", "finished_at", "created_at"]:
            if log.get(k):
                log[k] = str(log[k])
    return {"data": logs}


@app.get("/api/status")
def get_status():
    """获取当前运行状态"""
    return {
        "running_jobs": list(_running_jobs.keys()),
        "running_count": len(_running_jobs),
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=int(os.getenv("PORT", 8002)), reload=False)
