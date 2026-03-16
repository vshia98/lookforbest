"""数据库连接与操作工具"""
import os
import hashlib
import logging
from typing import Optional, Any
import pymysql
from pymysql.cursors import DictCursor

logger = logging.getLogger(__name__)


def get_connection():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", 3306)),
        database=os.getenv("DB_NAME", "lookforbest"),
        user=os.getenv("DB_USER", "lookforbest"),
        password=os.getenv("DB_PASSWORD", "lookforbest"),
        charset="utf8mb4",
        cursorclass=DictCursor,
        autocommit=False,
    )


# ───────────────────────── crawler_sources ──────────────────────────

def list_sources(conn, active_only: bool = False) -> list[dict]:
    with conn.cursor() as cur:
        sql = "SELECT * FROM crawler_sources"
        if active_only:
            sql += " WHERE is_active = 1"
        sql += " ORDER BY id"
        cur.execute(sql)
        return cur.fetchall()


def get_source(conn, source_id: int) -> Optional[dict]:
    with conn.cursor() as cur:
        cur.execute("SELECT * FROM crawler_sources WHERE id = %s", (source_id,))
        return cur.fetchone()


def create_source(conn, data: dict) -> int:
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO crawler_sources
                (name, description, url_pattern, config, is_active, cron_expr, max_pages, delay_ms)
            VALUES (%(name)s, %(description)s, %(url_pattern)s, %(config)s,
                    %(is_active)s, %(cron_expr)s, %(max_pages)s, %(delay_ms)s)
        """, data)
        conn.commit()
        return cur.lastrowid


def update_source(conn, source_id: int, data: dict) -> None:
    cols = ", ".join(f"{k} = %({k})s" for k in data)
    data["_id"] = source_id
    with conn.cursor() as cur:
        cur.execute(f"UPDATE crawler_sources SET {cols} WHERE id = %(_id)s", data)
        conn.commit()


def delete_source(conn, source_id: int) -> None:
    with conn.cursor() as cur:
        cur.execute("DELETE FROM crawler_sources WHERE id = %s", (source_id,))
        conn.commit()


def update_source_status(conn, source_id: int, status: str, last_crawl_at=None, total_crawled: int = 0) -> None:
    with conn.cursor() as cur:
        cur.execute("""
            UPDATE crawler_sources
            SET last_crawl_status = %s,
                last_crawl_at = IFNULL(%s, last_crawl_at),
                total_crawled = total_crawled + %s
            WHERE id = %s
        """, (status, last_crawl_at, total_crawled, source_id))
        conn.commit()


# ───────────────────────── crawler_run_logs ──────────────────────────

def create_run_log(conn, source_id: int, triggered_by: str = "scheduler") -> int:
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO crawler_run_logs (source_id, triggered_by)
            VALUES (%s, %s)
        """, (source_id, triggered_by))
        conn.commit()
        return cur.lastrowid


def finish_run_log(conn, log_id: int, status: str, pages: int, items: int,
                   upserted: int, skipped: int, error_msg: str = None) -> None:
    with conn.cursor() as cur:
        cur.execute("""
            UPDATE crawler_run_logs
            SET finished_at = NOW(), status = %s,
                pages_crawled = %s, items_crawled = %s,
                items_upserted = %s, items_skipped = %s, error_msg = %s
            WHERE id = %s
        """, (status, pages, items, upserted, skipped, error_msg, log_id))
        conn.commit()


def list_run_logs(conn, source_id: Optional[int] = None, limit: int = 50) -> list[dict]:
    with conn.cursor() as cur:
        if source_id:
            cur.execute("""
                SELECT l.*, s.name as source_name
                FROM crawler_run_logs l
                JOIN crawler_sources s ON l.source_id = s.id
                WHERE l.source_id = %s
                ORDER BY l.started_at DESC LIMIT %s
            """, (source_id, limit))
        else:
            cur.execute("""
                SELECT l.*, s.name as source_name
                FROM crawler_run_logs l
                JOIN crawler_sources s ON l.source_id = s.id
                ORDER BY l.started_at DESC LIMIT %s
            """, (limit,))
        return cur.fetchall()


# ───────────────────────── crawler_item_hashes ──────────────────────────

def get_item_hash(conn, source_id: int, item_url: str) -> Optional[str]:
    with conn.cursor() as cur:
        cur.execute("""
            SELECT content_hash FROM crawler_item_hashes
            WHERE source_id = %s AND item_url = %s
        """, (source_id, item_url[:1000]))
        row = cur.fetchone()
        return row["content_hash"] if row else None


def upsert_item_hash(conn, source_id: int, item_url: str, content_hash: str, robot_id: Optional[int] = None) -> None:
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO crawler_item_hashes (source_id, item_url, content_hash, robot_id, last_crawled_at)
            VALUES (%s, %s, %s, %s, NOW())
            ON DUPLICATE KEY UPDATE
                content_hash = VALUES(content_hash),
                robot_id = IFNULL(VALUES(robot_id), robot_id),
                last_crawled_at = NOW()
        """, (source_id, item_url[:1000], content_hash, robot_id))
        conn.commit()


def compute_hash(content: str) -> str:
    return hashlib.sha256(content.encode("utf-8", errors="replace")).hexdigest()


# ───────────────────────── robots upsert ──────────────────────────

def upsert_manufacturer(conn, m: dict) -> int:
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM manufacturers WHERE name = %s", (m["name"],))
        row = cur.fetchone()
        if row:
            return row["id"]
        cur.execute("""
            INSERT INTO manufacturers (name, name_en, country, country_code, website_url, description, is_verified)
            VALUES (%(name)s, %(name_en)s, %(country)s, %(country_code)s, %(website_url)s, %(description)s, 0)
        """, m)
        conn.commit()
        return cur.lastrowid


def get_or_create_category(conn, name: str, name_en: str = None) -> int:
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM robot_categories WHERE name = %s", (name,))
        row = cur.fetchone()
        if row:
            return row["id"]
        import re
        slug = re.sub(r"[^\w-]", "-", (name_en or name).lower())
        cur.execute(
            "INSERT INTO robot_categories (name, name_en, slug) VALUES (%s, %s, %s)",
            (name, name_en or name, slug),
        )
        conn.commit()
        return cur.lastrowid


def upsert_robot(conn, robot: dict) -> int:
    with conn.cursor() as cur:
        cur.execute("""
            INSERT INTO robots (
                manufacturer_id, category_id, name, name_en, slug, description,
                cover_image_url, source_url, price_range, status, is_verified,
                payload_kg, reach_mm, dof
            ) VALUES (
                %(manufacturer_id)s, %(category_id)s, %(name)s, %(name_en)s, %(slug)s, %(description)s,
                %(cover_image_url)s, %(source_url)s, %(price_range)s, %(status)s, 0,
                %(payload_kg)s, %(reach_mm)s, %(dof)s
            )
            ON DUPLICATE KEY UPDATE
                name_en         = VALUES(name_en),
                description     = VALUES(description),
                cover_image_url = IFNULL(VALUES(cover_image_url), cover_image_url),
                source_url      = IFNULL(VALUES(source_url), source_url),
                price_range     = VALUES(price_range),
                status          = VALUES(status),
                payload_kg      = IFNULL(VALUES(payload_kg), payload_kg),
                reach_mm        = IFNULL(VALUES(reach_mm), reach_mm),
                dof             = IFNULL(VALUES(dof), dof),
                updated_at      = NOW()
        """, robot)
        conn.commit()
        if cur.lastrowid:
            return cur.lastrowid
        cur.execute("SELECT id FROM robots WHERE slug = %s", (robot["slug"],))
        row = cur.fetchone()
        return row["id"] if row else 0
