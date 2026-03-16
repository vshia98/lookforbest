"""
配置驱动的爬取引擎

config JSON 结构:
{
  "manufacturer": {
    "name": "Boston Dynamics",
    "name_en": "Boston Dynamics",
    "country": "美国",
    "country_code": "US",
    "website_url": "https://bostondynamics.com"
  },
  "list_page": {
    "selector": "a.product-card",       // 列表页中指向详情页的链接选择器
    "url_attr": "href",                  // 默认 href
    "next_page_selector": "a.next",     // 翻页按钮（可选）
    "next_page_attr": "href"
  },
  "detail_fields": {
    "name":            {"selector": "h1.title"},
    "name_en":         {"selector": "h1.title"},
    "description":     {"selector": "div.desc"},
    "cover_image_url": {"selector": "img.hero", "attr": "src"},
    "category":        {"value": "工业机器人"},
    "category_en":     {"value": "Industrial Robot"},
    "payload_kg":      {"selector": "td.payload", "regex": "([\\d.]+)"},
    "reach_mm":        {"selector": "td.reach",   "regex": "([\\d]+)"},
    "dof":             {"selector": "td.dof",     "regex": "([\\d]+)"},
    "price_range":     {"value": "inquiry"}
  }
}
"""
import json
import logging
import re
import time
from typing import Optional
from datetime import datetime

from .db import (
    get_connection, get_source, update_source_status,
    create_run_log, finish_run_log,
    upsert_manufacturer, get_or_create_category, upsert_robot,
    get_item_hash, upsert_item_hash, compute_hash,
)
from .fetcher import fetch_page, fetch_pages
from .extractor import extract_item_urls, extract_next_page, extract_item

logger = logging.getLogger(__name__)


def slugify(text: str) -> str:
    text = re.sub(r"[^\w\s-]", "", text.lower())
    return re.sub(r"[\s_]+", "-", text).strip("-")[:280]


def ensure_unique_slug(conn, slug: str) -> str:
    base = slug
    i = 1
    while True:
        import pymysql
        with conn.cursor() as cur:
            cur.execute("SELECT id FROM robots WHERE slug = %s", (slug,))
            if cur.fetchone() is None:
                return slug
        slug = f"{base}-{i}"
        i += 1


class CrawlResult:
    def __init__(self):
        self.pages_crawled = 0
        self.items_crawled = 0
        self.items_upserted = 0
        self.items_skipped = 0
        self.errors: list[str] = []


def run_source(source_id: int, triggered_by: str = "scheduler") -> CrawlResult:
    """运行单个数据源的爬取任务"""
    conn = get_connection()
    result = CrawlResult()
    log_id = create_run_log(conn, source_id, triggered_by)
    update_source_status(conn, source_id, "running")

    try:
        source = get_source(conn, source_id)
        if not source:
            raise ValueError(f"数据源 {source_id} 不存在")

        config = source["config"] if isinstance(source["config"], dict) else json.loads(source["config"])
        start_urls = [u.strip() for u in source["url_pattern"].split(",") if u.strip()]
        max_pages = source["max_pages"] or 10
        delay_ms = source["delay_ms"] or 1000

        manufacturer_cfg = config.get("manufacturer", {})
        list_page_cfg = config.get("list_page", {})
        detail_fields = config.get("detail_fields", {})

        # 确保厂商存在
        mfr_id = upsert_manufacturer(conn, {
            "name": manufacturer_cfg.get("name", "Unknown"),
            "name_en": manufacturer_cfg.get("name_en", "Unknown"),
            "country": manufacturer_cfg.get("country", ""),
            "country_code": manufacturer_cfg.get("country_code", ""),
            "website_url": manufacturer_cfg.get("website_url", ""),
            "description": manufacturer_cfg.get("description", ""),
        })

        # 收集详情页 URL
        detail_urls: list[str] = []

        if list_page_cfg.get("selector"):
            # 多层：先爬列表页获取详情页 URL
            pages_to_crawl = list(start_urls)
            pages_visited = 0
            while pages_to_crawl and pages_visited < max_pages:
                url = pages_to_crawl.pop(0)
                if pages_visited > 0:
                    time.sleep(delay_ms / 1000)
                try:
                    html = fetch_page(url)
                    result.pages_crawled += 1
                    pages_visited += 1
                    found = extract_item_urls(
                        html,
                        list_page_cfg["selector"],
                        url_attr=list_page_cfg.get("url_attr", "href"),
                        base_url=url,
                    )
                    detail_urls.extend(found)

                    # 翻页
                    if list_page_cfg.get("next_page_selector"):
                        next_url = extract_next_page(
                            html,
                            list_page_cfg["next_page_selector"],
                            attr=list_page_cfg.get("next_page_attr", "href"),
                            base_url=url,
                        )
                        if next_url and next_url not in pages_to_crawl:
                            pages_to_crawl.append(next_url)
                except Exception as e:
                    result.errors.append(f"列表页 {url}: {e}")
                    logger.warning("列表页爬取失败 %s: %s", url, e)
        else:
            # 直接爬取起始 URL 作为详情页
            detail_urls = list(start_urls)

        detail_urls = list(dict.fromkeys(detail_urls))  # dedup
        logger.info("[%s] 共发现 %d 个详情页", source["name"], len(detail_urls))

        # 爬取详情页
        for i, detail_url in enumerate(detail_urls):
            if i > 0:
                time.sleep(delay_ms / 1000)
            try:
                html = fetch_page(detail_url)
                result.pages_crawled += 1
                result.items_crawled += 1

                # 增量检测
                content_hash = compute_hash(html)
                existing_hash = get_item_hash(conn, source_id, detail_url)
                if existing_hash == content_hash:
                    result.items_skipped += 1
                    logger.debug("内容未变，跳过 %s", detail_url)
                    continue

                # 提取字段
                item = extract_item(html, detail_fields)

                name = item.get("name_en") or item.get("name", "")
                if not name:
                    logger.debug("无法提取名称，跳过 %s", detail_url)
                    continue

                category_name = item.get("category", "其他")
                category_en = item.get("category_en", category_name)
                cat_id = get_or_create_category(conn, category_name, category_en)

                slug = slugify(f"{manufacturer_cfg.get('name_en', 'robot')}-{name}")
                slug = ensure_unique_slug(conn, slug) if existing_hash is None else slug

                def parse_float(v) -> Optional[float]:
                    try:
                        return float(re.sub(r"[^\d.]", "", str(v))) if v else None
                    except (ValueError, TypeError):
                        return None

                def parse_int(v) -> Optional[int]:
                    try:
                        return int(re.sub(r"[^\d]", "", str(v))) if v else None
                    except (ValueError, TypeError):
                        return None

                robot_record = {
                    "manufacturer_id": mfr_id,
                    "category_id": cat_id,
                    "name": item.get("name") or name,
                    "name_en": name,
                    "slug": slug,
                    "description": item.get("description", "")[:5000],
                    "cover_image_url": item.get("cover_image_url") or None,
                    "source_url": detail_url,
                    "price_range": item.get("price_range", "inquiry"),
                    "status": item.get("status", "active"),
                    "payload_kg": parse_float(item.get("payload_kg")),
                    "reach_mm": parse_int(item.get("reach_mm")),
                    "dof": parse_int(item.get("dof")),
                }

                robot_id = upsert_robot(conn, robot_record)
                upsert_item_hash(conn, source_id, detail_url, content_hash, robot_id)
                result.items_upserted += 1
                logger.info("写入机器人: [%d] %s", robot_id, name)

            except Exception as e:
                result.errors.append(f"详情页 {detail_url}: {e}")
                logger.warning("详情页爬取失败 %s: %s", detail_url, e)

        status = "success" if not result.errors or result.items_upserted > 0 else "failed"
        finish_run_log(
            conn, log_id, status,
            result.pages_crawled, result.items_crawled,
            result.items_upserted, result.items_skipped,
            "; ".join(result.errors[:5]) if result.errors else None,
        )
        update_source_status(
            conn, source_id, status,
            last_crawl_at=datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            total_crawled=result.items_upserted,
        )
        logger.info(
            "[%s] 爬取完成: pages=%d items=%d upserted=%d skipped=%d errors=%d",
            source["name"], result.pages_crawled, result.items_crawled,
            result.items_upserted, result.items_skipped, len(result.errors),
        )

    except Exception as e:
        logger.error("数据源 %d 爬取异常: %s", source_id, e, exc_info=True)
        result.errors.append(str(e))
        try:
            finish_run_log(conn, log_id, "failed", 0, 0, 0, 0, str(e))
            update_source_status(conn, source_id, "failed")
        except Exception:
            pass
    finally:
        conn.close()

    return result
