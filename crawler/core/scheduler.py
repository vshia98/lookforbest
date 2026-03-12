"""基于 APScheduler 的爬虫调度器"""
import logging
from threading import Lock

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger

from .db import get_connection, list_sources
from .engine import run_source

logger = logging.getLogger(__name__)

_scheduler: BackgroundScheduler = None
_lock = Lock()


def get_scheduler() -> BackgroundScheduler:
    global _scheduler
    if _scheduler is None:
        _scheduler = BackgroundScheduler(timezone="Asia/Shanghai")
        _scheduler.start()
    return _scheduler


def _make_job_id(source_id: int) -> str:
    return f"crawler_source_{source_id}"


def _crawl_job(source_id: int):
    logger.info("调度器触发爬虫: source_id=%d", source_id)
    try:
        run_source(source_id, triggered_by="scheduler")
    except Exception as e:
        logger.error("调度爬取失败 source_id=%d: %s", source_id, e)


def refresh_jobs():
    """从数据库重新加载所有活跃数据源的调度任务"""
    with _lock:
        scheduler = get_scheduler()
        conn = get_connection()
        try:
            sources = list_sources(conn, active_only=True)
        finally:
            conn.close()

        # 移除所有旧爬虫任务
        for job in scheduler.get_jobs():
            if job.id.startswith("crawler_source_"):
                scheduler.remove_job(job.id)

        # 添加新任务
        for source in sources:
            cron_expr = source.get("cron_expr") or "0 0 3 * * ?"
            parts = cron_expr.strip().split()
            try:
                if len(parts) == 6:
                    # second minute hour dom month dow
                    trigger = CronTrigger(
                        second=parts[0], minute=parts[1], hour=parts[2],
                        day=parts[3], month=parts[4], day_of_week=parts[5],
                    )
                elif len(parts) == 5:
                    trigger = CronTrigger(
                        minute=parts[0], hour=parts[1],
                        day=parts[2], month=parts[3], day_of_week=parts[4],
                    )
                else:
                    raise ValueError(f"无法解析 cron 表达式: {cron_expr}")

                scheduler.add_job(
                    _crawl_job,
                    trigger=trigger,
                    id=_make_job_id(source["id"]),
                    args=[source["id"]],
                    replace_existing=True,
                    misfire_grace_time=3600,
                )
                logger.info("已注册爬虫调度: [%d] %s @ %s", source["id"], source["name"], cron_expr)
            except Exception as e:
                logger.warning("调度任务注册失败 source_id=%d: %s", source["id"], e)


def shutdown():
    global _scheduler
    if _scheduler and _scheduler.running:
        _scheduler.shutdown(wait=False)
