"""数据库连接与写入工具"""
import os
import pymysql
from dotenv import load_dotenv

load_dotenv()


def get_connection():
    return pymysql.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", 3306)),
        db=os.getenv("DB_NAME", "lookforbest"),
        user=os.getenv("DB_USER", "lookforbest"),
        password=os.getenv("DB_PASSWORD", "lookforbest"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=False,
    )


def upsert_manufacturer(conn, m: dict) -> int:
    """获取已有厂商或插入新厂商，返回 id"""
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM manufacturers WHERE name = %s", (m["name"],))
        row = cur.fetchone()
        if row:
            return row["id"]
        cur.execute(
            """
            INSERT INTO manufacturers (name, name_en, country, country_code, website_url, description, is_verified)
            VALUES (%(name)s, %(name_en)s, %(country)s, %(country_code)s, %(website_url)s, %(description)s, 0)
            """,
            m,
        )
        conn.commit()
        return cur.lastrowid


def get_or_create_category(conn, name: str, name_en: str = None) -> int:
    """获取或创建机器人分类，返回 id"""
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM robot_categories WHERE name = %s", (name,))
        row = cur.fetchone()
        if row:
            return row["id"]
        slug = name_en.lower().replace(" ", "-") if name_en else name.lower().replace(" ", "-")
        cur.execute(
            "INSERT INTO robot_categories (name, name_en, slug) VALUES (%s, %s, %s)",
            (name, name_en or name, slug),
        )
        conn.commit()
        return cur.lastrowid


def upsert_robot(conn, robot: dict) -> int:
    """插入或更新机器人记录，返回 id"""
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO robots (
                manufacturer_id, category_id, name, name_en, slug, description,
                cover_image_url, price_range, status, is_verified
            ) VALUES (
                %(manufacturer_id)s, %(category_id)s, %(name)s, %(name_en)s, %(slug)s, %(description)s,
                %(cover_image_url)s, %(price_range)s, %(status)s, 0
            )
            ON DUPLICATE KEY UPDATE
                name_en          = VALUES(name_en),
                description      = VALUES(description),
                cover_image_url  = VALUES(cover_image_url),
                price_range      = VALUES(price_range),
                status           = VALUES(status)
            """,
            robot,
        )
        conn.commit()
        if cur.lastrowid:
            return cur.lastrowid
        cur.execute("SELECT id FROM robots WHERE slug = %s", (robot["slug"],))
        return cur.fetchone()["id"]


def insert_robot_image(conn, robot_id: int, url: str, is_primary: bool = False):
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT IGNORE INTO robot_images (robot_id, url)
            VALUES (%s, %s)
            """,
            (robot_id, url),
        )
        conn.commit()


def insert_robot_video(conn, robot_id: int, url: str, title: str = None):
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT IGNORE INTO robot_videos (robot_id, url, title)
            VALUES (%s, %s, %s)
            """,
            (robot_id, url, title),
        )
        conn.commit()
