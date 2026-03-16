#!/usr/bin/env python3
"""
爬取 aixzd.com 机器人数据并写入 lookforbest 数据库
用法: python scripts/crawl_aixzd.py [--dry-run] [--pages 6] [--delay 1.5]
"""
import argparse
import hashlib
import json
import logging
import re
import sys
import time
from urllib.parse import urljoin, unquote

import os
from urllib.parse import urlparse

import httpx
from selectolax.parser import HTMLParser

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%H:%M:%S",
)
log = logging.getLogger("aixzd")

BASE_URL = "https://aixzd.com"
LIST_URL = "https://aixzd.com/robots"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                  "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36",
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
}

# ──────────────────────────── HTTP ────────────────────────────────

def fetch(url: str, retries: int = 3) -> str:
    for i in range(retries):
        try:
            with httpx.Client(headers=HEADERS, follow_redirects=True, timeout=15) as c:
                r = c.get(url)
                r.raise_for_status()
                return r.text
        except Exception as e:
            log.warning("抓取失败 %s (第%d次): %s", url, i + 1, e)
            if i < retries - 1:
                time.sleep(2 ** i)
    return ""


# ──────────────────────────── MinIO 图片上传 ────────────────────────

_minio_client = None


def get_minio_client():
    global _minio_client
    if _minio_client is not None:
        return _minio_client
    try:
        from minio import Minio
        endpoint = os.getenv("MINIO_ENDPOINT", "http://localhost:9000").replace("http://", "").replace("https://", "")
        _minio_client = Minio(
            endpoint,
            access_key=os.getenv("MINIO_ACCESS_KEY", "minioadmin"),
            secret_key=os.getenv("MINIO_SECRET_KEY", "minioadmin"),
            secure=False,
        )
        bucket = os.getenv("MINIO_BUCKET", "lookforbest")
        if not _minio_client.bucket_exists(bucket):
            _minio_client.make_bucket(bucket)
        return _minio_client
    except Exception as e:
        log.warning("MinIO 初始化失败，图片将保持原始URL: %s", e)
        return None


def download_and_upload_image(image_url: str, prefix: str = "robots") -> str:
    """下载图片并上传到 MinIO，返回本地 MinIO URL；失败则返回原始 URL"""
    if not image_url:
        return ""
    client = get_minio_client()
    if client is None:
        return image_url

    bucket = os.getenv("MINIO_BUCKET", "lookforbest")
    try:
        # 用 URL hash 作为文件名，保留原始扩展名
        parsed = urlparse(image_url)
        ext = os.path.splitext(parsed.path)[1] or ".webp"
        url_hash = hashlib.md5(image_url.encode()).hexdigest()
        object_name = f"{prefix}/{url_hash}{ext}"

        # 检查是否已存在
        try:
            client.stat_object(bucket, object_name)
            return f"/{bucket}/{object_name}"
        except Exception:
            pass

        # 下载
        with httpx.Client(headers={**HEADERS, "Referer": "https://aixzd.com/"}, follow_redirects=True, timeout=15) as c:
            resp = c.get(image_url)
            resp.raise_for_status()
            data = resp.content
            content_type = resp.headers.get("content-type", "image/webp")

        # 上传
        import io
        client.put_object(bucket, object_name, io.BytesIO(data), len(data), content_type=content_type)
        return f"/{bucket}/{object_name}"
    except Exception as e:
        log.warning("图片上传失败 %s: %s", image_url[:60], e)
        return image_url


# ──────────────────────────── 列表页 ────────────────────────────────

def parse_list_page(html: str) -> list[dict]:
    """从列表页提取机器人基础信息（URL、分类、厂商名）"""
    tree = HTMLParser(html)
    items = []
    for card in tree.css(".tax-robot-item"):
        a = card.css_first("a.item-wrap[href]")
        if not a:
            continue
        href = a.attributes.get("href", "")
        if "/robot/" not in href:
            continue
        url = href if href.startswith("http") else urljoin(BASE_URL, href)

        look = card.css_first(".item-look .txt")
        category = look.text(strip=True) if look else ""

        title = card.css_first(".p-title")
        name = title.text(strip=True) if title else ""

        subtitle = card.css_first(".subtitle")
        company = subtitle.text(strip=True) if subtitle else ""

        items.append({"url": url, "category": category, "company": company, "name": name})
    return items


def get_all_robots_from_list(max_pages: int = 6, delay: float = 1.5) -> list[dict]:
    """遍历分页获取所有机器人基础信息"""
    all_items = []
    seen_urls = set()
    for page in range(1, max_pages + 1):
        url = LIST_URL if page == 1 else f"{LIST_URL}/page/{page}"
        log.info("抓取列表页 %d: %s", page, url)
        html = fetch(url)
        if not html:
            log.warning("列表页 %d 抓取失败，停止翻页", page)
            break
        items = parse_list_page(html)
        if not items:
            log.info("列表页 %d 无数据，停止翻页", page)
            break
        for item in items:
            if item["url"] not in seen_urls:
                seen_urls.add(item["url"])
                all_items.append(item)
        log.info("  找到 %d 个机器人", len(items))
        if page < max_pages:
            time.sleep(delay)
    log.info("共发现 %d 个机器人", len(all_items))
    return all_items


# ──────────────────────────── 详情页 ────────────────────────────────

def text(tree_or_node, selector: str) -> str:
    """安全提取文本"""
    node = tree_or_node.css_first(selector)
    if node is None:
        return ""
    return (node.text(deep=True, separator=" ", strip=True) or "").strip()


def attr(tree_or_node, selector: str, attribute: str) -> str:
    """安全提取属性"""
    node = tree_or_node.css_first(selector)
    if node is None:
        return ""
    return (node.attributes.get(attribute, "") or "").strip()


def parse_specs_simple(tree) -> dict:
    """解析顶部概览参数卡片 (.spec-item)"""
    specs = {}
    for item in tree.css("div.spec-item"):
        texts = [t.strip() for t in (item.text(deep=True, separator="|") or "").split("|") if t.strip()]
        if len(texts) >= 2:
            value, label = texts[0], texts[-1]
            specs[label] = value
    return specs


def parse_specs_table(tree) -> list[dict]:
    """解析详细规格表格 (table)，返回 [{参数, 规格, 说明}, ...]"""
    rows = []
    for table in tree.css("table"):
        headers = []
        for tr in table.css("tr"):
            cells = [td.text(deep=True, strip=True) for td in tr.css("td, th")]
            if not cells:
                continue
            # 首行作为表头
            if not headers:
                headers = cells
                continue
            row = {}
            for i, val in enumerate(cells):
                key = headers[i] if i < len(headers) else f"col_{i}"
                row[key] = val
            if any(row.values()):
                rows.append(row)
    return rows


def parse_entry_parts(tree) -> dict:
    """解析所有 entry-part 区块（介绍、技术规格、应用场景、优势等）"""
    parts = {}
    for part in tree.css(".entry-part"):
        title_node = part.css_first(".e-title, .part-title, h2, h3")
        title = title_node.text(strip=True) if title_node else ""
        if not title:
            continue
        # 提取段落文本
        paragraphs = []
        for p in part.css("p"):
            txt = p.text(deep=True, strip=True)
            if txt:
                paragraphs.append(txt)
        # 提取图片
        images = []
        for img in part.css("img"):
            src = img.attributes.get("src", "") or img.attributes.get("data-src", "")
            alt = img.attributes.get("alt", "")
            if src and "favicon" not in src and "logo" not in src.lower():
                images.append({"src": src, "alt": alt})
        # 提取视频
        videos = []
        for v in part.css("video[src], video source[src], iframe[src]"):
            src = v.attributes.get("src", "")
            if src:
                videos.append(src)

        if paragraphs or images or videos:
            parts[title] = {
                "text": "\n".join(paragraphs),
                "images": images,
                "videos": videos,
            }
    return parts


def parse_detail_page(html: str, list_info: dict) -> dict | None:
    """解析详情页全部机器人数据，合并列表页信息"""
    tree = HTMLParser(html)

    name = text(tree, "h1") or list_info.get("name", "")
    if not name:
        return None

    subtitle = text(tree, "h3.subtitle")

    # ── 厂商 ──
    company_name = text(tree, ".company-name") or list_info.get("company", "")
    company_logo = attr(tree, ".post-logo img.img", "src")
    if not company_logo:
        company_logo = attr(tree, "div.post-company img.img", "src")
    if not company_logo:
        company_logo = attr(tree, "div.company-logo img.img", "src")
    company_intro = text(tree, "div.block-intro")
    # 官网链接（解码跳转链接）
    company_website = ""
    for a in tree.css("a.label[href*='link.aixzd.com']"):
        href = a.attributes.get("href", "")
        m = re.search(r'redirect=([^&]+)', href)
        if m:
            from urllib.parse import unquote as uq
            company_website = uq(m.group(1))
            break
    if not company_website:
        for a in tree.css("div.company-block a[href]"):
            href = a.attributes.get("href", "")
            if href and "aixzd.com" not in href and href.startswith("http"):
                company_website = href
                break

    # ── 描述 ──
    description = text(tree, "div.item-excerpt")

    # ── 封面图 ──
    cover = attr(tree, "div.cover-in img.img", "src")
    if not cover:
        cover = attr(tree, "div.item-cover img.img", "src")

    # ── 价格 ──
    price_txt = text(tree, "i.price-txt").strip()
    price_unit = text(tree, "i.price-unit").strip()
    price = ""
    if price_txt:
        if price_unit and not price_txt.endswith(price_unit):
            price = f"{price_txt}{price_unit}"
        else:
            price = price_txt

    # ── 分类（列表页最准）──
    category = list_info.get("category", "")

    # ── 元数据 ──
    meta = {}
    for node in tree.css(".meta-item"):
        t = node.text(deep=True, strip=True) or ""
        for key in ["所属公司", "产品属地", "是否开源", "收录时间"]:
            if t.startswith(key):
                meta[key] = t[len(key):]

    # ── 概览参数（顶部卡片）──
    specs_overview = parse_specs_simple(tree)

    # ── 详细规格表（表格形式，含说明）──
    specs_table = parse_specs_table(tree)

    # ── 产品标签 (robot-tag) ──
    tags = []
    for a in tree.css("a[href*='robot-tag']"):
        t = a.text(strip=True)
        if t:
            tags.append(t)
    tags = list(dict.fromkeys(tags))  # 去重保序

    # ── 内容区块（介绍、规格、场景、优势）──
    content_parts = parse_entry_parts(tree)

    # ── 拼装介绍正文 ──
    intro_text = ""
    for key in content_parts:
        if "介绍" in key:
            intro_text = content_parts[key]["text"]
            break

    # ── 应用场景 ──
    scenarios_text = ""
    for key in content_parts:
        if "场景" in key or "应用" in key:
            scenarios_text = content_parts[key]["text"]
            break

    # ── 优势/亮点 ──
    advantages_text = ""
    for key in content_parts:
        if "优势" in key or "亮点" in key or "特点" in key:
            advantages_text = content_parts[key]["text"]
            break

    # ── 内容区所有产品图片 ──
    content_images = []
    for part_data in content_parts.values():
        content_images.extend(part_data.get("images", []))

    # ── 视频 ──
    videos = []
    for part_data in content_parts.values():
        videos.extend(part_data.get("videos", []))
    # 也检查页面级视频
    for v in tree.css("video[src], video source[src]"):
        src = v.attributes.get("src", "")
        if src and src not in videos:
            videos.append(src)

    return {
        "name": name,
        "subtitle": subtitle,
        "description": description,
        "introduction": intro_text,
        "application_scenarios": scenarios_text,
        "advantages": advantages_text,
        "cover_image_url": cover,
        "content_images": content_images,
        "videos": videos,
        "category": category,
        "tags": tags,
        "country": meta.get("产品属地", ""),
        "is_open_source": meta.get("是否开源", ""),
        "listed_date": meta.get("收录时间", ""),
        "price_range": price or "inquiry",
        "specs_overview": specs_overview,
        "specs_table": specs_table,
        "source_url": list_info["url"],
        "manufacturer": {
            "name": company_name,
            "logo_url": company_logo,
            "description": company_intro,
            "website_url": company_website,
        },
    }


# ──────────────────────────── 写入数据库 ────────────────────────────

def get_db_connection():
    import pymysql
    from pymysql.cursors import DictCursor
    import os
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


def upsert_manufacturer(conn, mfr: dict, country: str = "") -> int | None:
    """写入或更新厂商，返回 ID"""
    if not mfr.get("name"):
        return None
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM manufacturers WHERE name = %s", (mfr["name"],))
        row = cur.fetchone()
        if row:
            cur.execute(
                "UPDATE manufacturers SET "
                "logo_url=COALESCE(NULLIF(%s,''),logo_url), "
                "description=COALESCE(NULLIF(%s,''),description), "
                "website_url=COALESCE(NULLIF(%s,''),website_url), "
                "country=COALESCE(NULLIF(%s,''),country) "
                "WHERE id=%s",
                (mfr.get("logo_url", ""), mfr.get("description", ""),
                 mfr.get("website_url", ""), country, row["id"])
            )
            conn.commit()
            return row["id"]
        cur.execute(
            "INSERT INTO manufacturers (name, logo_url, description, website_url, country) "
            "VALUES (%s, %s, %s, %s, %s)",
            (mfr["name"], mfr.get("logo_url", ""), mfr.get("description", ""),
             mfr.get("website_url", ""), country)
        )
        conn.commit()
        return cur.lastrowid


# 将 aixzd 的细分类映射到标准分类（9 大类）
CATEGORY_MAP = {
    # 人形机器人
    "全尺寸人形机器人": "人形机器人",
    "双足腿式机器人": "人形机器人",
    "人形机器人": "人形机器人",
    # 轮式机器人
    "轮式人形机器人": "轮式机器人",
    "多轮承载式机器人": "轮式机器人",
    "轮式机器人": "轮式机器人",
    "三轮稳定式机器人": "轮式机器人",
    "两轮平衡式机器人": "轮式机器人",
    # 四足机器人
    "仿生狗机器人": "四足机器人",
    "四足腿式机器人": "四足机器人",
    "四足多足": "四足机器人",
    # 迷你/桌面机器人
    "迷你人形机器人": "迷你/桌面机器人",
    "半身人形机器人": "迷你/桌面机器人",
    "功能模块机器人": "迷你/桌面机器人",
    # 仿生机器人
    "仿生猫机器人": "仿生机器人",
    "其它仿生动物": "仿生机器人",
    "仿生动物机器人": "仿生机器人",
    "多足腿式机器人": "仿生机器人",
    # 工业机械臂
    "机械臂": "工业机械臂",
    "专用自动化设备": "工业机械臂",
    # 灵巧手/末端执行器
    "灵巧手": "灵巧手/末端执行器",
    "辅助配件": "灵巧手/末端执行器",
    # 特种机器人
    "穿戴设备": "特种机器人",
    "智能仿生腿": "特种机器人",
}


def ensure_category(conn, name: str) -> int | None:
    if not name:
        return None
    # 映射到已有大分类
    mapped = CATEGORY_MAP.get(name, name)
    with conn.cursor() as cur:
        # 先找映射后的名称
        cur.execute("SELECT id FROM robot_categories WHERE name = %s", (mapped,))
        row = cur.fetchone()
        if row:
            return row["id"]
        # 再找原始名称
        if mapped != name:
            cur.execute("SELECT id FROM robot_categories WHERE name = %s", (name,))
            row = cur.fetchone()
            if row:
                return row["id"]
        # 都没有则创建（用映射后的名称）
        slug = re.sub(r'[^\w\u4e00-\u9fff]+', '-', mapped.lower()).strip('-')
        if not slug:
            slug = f"cat-{hashlib.md5(mapped.encode()).hexdigest()[:8]}"
        cur.execute("SELECT id FROM robot_categories WHERE slug = %s", (slug,))
        if cur.fetchone():
            slug = f"{slug}-{hashlib.md5(mapped.encode()).hexdigest()[:6]}"
        cur.execute("INSERT INTO robot_categories (name, slug) VALUES (%s, %s)", (mapped, slug))
        conn.commit()
        return cur.lastrowid


def ensure_tag(conn, name: str) -> int:
    """查找或创建标签，返回 tag_id"""
    with conn.cursor() as cur:
        cur.execute("SELECT id FROM robot_tags WHERE name = %s", (name,))
        row = cur.fetchone()
        if row:
            return row["id"]
        slug = re.sub(r'[^\w\u4e00-\u9fff]+', '-', name.lower()).strip('-')
        if not slug:
            slug = f"tag-{hashlib.md5(name.encode()).hexdigest()[:8]}"
        cur.execute("SELECT id FROM robot_tags WHERE slug = %s", (slug,))
        if cur.fetchone():
            slug = f"{slug}-{hashlib.md5(name.encode()).hexdigest()[:6]}"
        cur.execute("INSERT INTO robot_tags (name, slug) VALUES (%s, %s)", (name, slug))
        conn.commit()
        return cur.lastrowid


def sync_robot_tags(conn, robot_id: int, tag_names: list[str]):
    """同步机器人的标签关联（全量替换）"""
    if not tag_names:
        return
    tag_ids = [ensure_tag(conn, name) for name in tag_names if name.strip()]
    with conn.cursor() as cur:
        cur.execute("DELETE FROM robot_tag_mappings WHERE robot_id = %s", (robot_id,))
        for tag_id in tag_ids:
            cur.execute(
                "INSERT IGNORE INTO robot_tag_mappings (robot_id, tag_id) VALUES (%s, %s)",
                (robot_id, tag_id)
            )
        # 刷新引用计数
        cur.execute(
            "UPDATE robot_tags SET usage_count = "
            "(SELECT COUNT(*) FROM robot_tag_mappings WHERE tag_id = robot_tags.id) "
            "WHERE id IN (%s)" % ",".join(["%s"] * len(tag_ids)),
            tag_ids
        )
        conn.commit()


def _parse_num(val: str) -> float | None:
    m = re.search(r'[\d.]+', val or "")
    return float(m.group()) if m else None


def _cm_to_mm(val: str) -> int | None:
    """解析 cm 值并转为 mm"""
    n = _parse_num(val)
    if n is None:
        return None
    if "mm" in (val or ""):
        return int(n)
    return int(n * 10)  # cm → mm


def _parse_price(price_str: str) -> tuple[str, float | None]:
    """解析价格字符串，返回 (price_range 文本, price_usd_from 数值)"""
    if not price_str or price_str == "inquiry":
        return "inquiry", None
    num = _parse_num(price_str)
    # 简单判断：纯数字可能是人民币
    return price_str, num


def upsert_robot(conn, data: dict, manufacturer_id: int | None, category_id: int | None) -> int:
    """写入或更新机器人（全字段）"""
    # slug: 厂商名-产品名，保证唯一性
    mfr_name = data.get("manufacturer", {}).get("name", "")
    slug_base = f"{mfr_name}-{data['name']}" if mfr_name else data["name"]
    slug = re.sub(r'[^a-z0-9\u4e00-\u9fff]+', '-', slug_base.lower()).strip('-')

    specs = data.get("specs_overview", {})
    specs_table = data.get("specs_table", [])

    # 合并 specs_overview + specs_table 为完整的 extra_specs
    extra_specs = {
        "overview": specs,
        "details": specs_table,
    }

    # 解析价格
    price_range, price_usd_from = _parse_price(data.get("price_range", "inquiry"))

    # 解析 listed_date
    listed_date = None
    if data.get("listed_date"):
        try:
            listed_date = data["listed_date"]  # 格式已经是 YYYY-MM-DD
        except Exception:
            pass

    fields = {
        "name": data["name"],
        "slug": slug,
        "subtitle": data.get("subtitle", ""),
        "description": data.get("description", ""),
        "introduction": data.get("introduction", ""),
        "application_scenarios": data.get("application_scenarios", ""),
        "advantages": data.get("advantages", ""),
        "cover_image_url": data.get("cover_image_url", ""),
        "content_images": json.dumps(data.get("content_images", []), ensure_ascii=False) or None,
        "video_urls": json.dumps(data.get("videos", []), ensure_ascii=False) or None,
        # tags 写入关联表，不存 robots 表
        "is_open_source": data.get("is_open_source", ""),
        "listed_date": listed_date,
        "manufacturer_id": manufacturer_id,
        "category_id": category_id,
        "price_range": price_range,
        "price_usd_from": price_usd_from,
        "has_video": 1 if data.get("videos") else 0,
        "source_url": data.get("source_url", ""),
        "extra_specs": json.dumps(extra_specs, ensure_ascii=False),
        # 结构化数值字段
        "weight_kg": _parse_num(specs.get("整机重量", specs.get("重量", specs.get("带电池重量", "")))),
        "payload_kg": _parse_num(specs.get("负载能力", specs.get("负载", ""))),
        "dof": _parse_num(specs.get("自由度", specs.get("DOF", ""))),
        "reach_mm": _cm_to_mm(specs.get("手臂长", specs.get("臂展", ""))),
        "height_mm": _cm_to_mm(specs.get("高度", specs.get("站立高度", ""))),
        "max_speed_m_s": _parse_num(specs.get("峰值速度", specs.get("最高速度", ""))),
        "battery_life_h": _parse_num(specs.get("运行时长", specs.get("工作时长", specs.get("续航", "")))),
    }

    with conn.cursor() as cur:
        # 查找已有记录（优先 source_url）
        cur.execute("SELECT id FROM robots WHERE source_url = %s", (fields["source_url"],))
        row = cur.fetchone()
        if not row:
            cur.execute("SELECT id FROM robots WHERE name = %s AND manufacturer_id = %s",
                        (fields["name"], manufacturer_id))
            row = cur.fetchone()

        if row:
            # 更新（不改 slug，避免破坏已有 URL）
            update_fields = {k: v for k, v in fields.items() if k != "slug"}
            set_clause = ", ".join(f"{k}=%s" for k in update_fields)
            vals = list(update_fields.values())
            vals.append(row["id"])
            cur.execute(f"UPDATE robots SET {set_clause} WHERE id=%s", vals)
            conn.commit()
            return row["id"]

        # 确保 slug 唯一
        base_slug = fields["slug"]
        suffix = 1
        while True:
            cur.execute("SELECT id FROM robots WHERE slug = %s", (fields["slug"],))
            if not cur.fetchone():
                break
            fields["slug"] = f"{base_slug}-{suffix}"
            suffix += 1

        cols = ", ".join(fields.keys())
        placeholders = ", ".join(["%s"] * len(fields))
        cur.execute(f"INSERT INTO robots ({cols}) VALUES ({placeholders})", list(fields.values()))
        conn.commit()
        return cur.lastrowid


# ──────────────────────────── 主流程 ────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="爬取 aixzd.com 机器人数据")
    parser.add_argument("--dry-run", action="store_true", help="只抓取不写库，输出 JSON")
    parser.add_argument("--pages", type=int, default=6, help="最大翻页数 (默认6)")
    parser.add_argument("--delay", type=float, default=1.5, help="请求间隔秒数 (默认1.5)")
    parser.add_argument("--limit", type=int, default=0, help="限制爬取数量 (0=不限)")
    parser.add_argument("--no-images", action="store_true", help="不下载图片到 MinIO，保留原始 URL")
    args = parser.parse_args()

    # 1. 从列表页获取所有机器人基础信息
    list_items = get_all_robots_from_list(max_pages=args.pages, delay=args.delay)
    if args.limit > 0:
        list_items = list_items[:args.limit]

    # 2. 逐个爬取详情页
    robots = []
    for i, item in enumerate(list_items):
        log.info("[%d/%d] 爬取: %s (%s)", i + 1, len(list_items), item["name"], unquote(item["url"]))
        html = fetch(item["url"])
        if not html:
            log.warning("  跳过 (抓取失败)")
            continue
        data = parse_detail_page(html, item)
        if not data:
            log.warning("  跳过 (解析失败)")
            continue
        # 下载图片到 MinIO
        if not args.no_images and not args.dry_run:
            if data.get("cover_image_url"):
                new_url = download_and_upload_image(data["cover_image_url"])
                if new_url != data["cover_image_url"]:
                    log.info("  封面图已上传: %s", new_url.split("/")[-1])
                data["cover_image_url"] = new_url
            if data.get("manufacturer", {}).get("logo_url"):
                data["manufacturer"]["logo_url"] = download_and_upload_image(
                    data["manufacturer"]["logo_url"], prefix="manufacturers"
                )
            for img in data.get("content_images", []):
                if img.get("src"):
                    img["src"] = download_and_upload_image(img["src"])

        robots.append(data)
        log.info("  OK: %s | %s | %s", data["name"], data["manufacturer"]["name"], data["category"])
        if i < len(list_items) - 1:
            time.sleep(args.delay)

    log.info("成功解析 %d / %d 个机器人", len(robots), len(list_items))

    # 3. 写入数据库或输出 JSON
    if args.dry_run:
        print(json.dumps(robots, ensure_ascii=False, indent=2))
        return

    conn = get_db_connection()
    inserted, updated = 0, 0
    try:
        for data in robots:
            mfr_id = upsert_manufacturer(conn, data["manufacturer"], country=data.get("country", ""))
            cat_id = ensure_category(conn, data["category"])
            robot_id = upsert_robot(conn, data, mfr_id, cat_id)
            sync_robot_tags(conn, robot_id, data.get("tags", []))
            log.info("  写入: %s (robot_id=%d, mfr=%s, cat=%s, tags=%d)",
                     data["name"], robot_id, mfr_id, cat_id, len(data.get("tags", [])))
    except Exception as e:
        log.error("写入数据库失败: %s", e)
        conn.rollback()
        raise
    finally:
        conn.close()

    log.info("共写入 %d 个机器人", len(robots))

    # 4. 补抓缺失的厂商 logo（从官网 favicon）
    if not args.no_images:
        fetch_missing_manufacturer_logos(conn if not conn.open else get_db_connection())


def fetch_missing_manufacturer_logos(conn=None):
    """从厂商官网抓取 favicon 作为 logo，上传到 MinIO"""
    if conn is None:
        conn = get_db_connection()

    with conn.cursor() as cur:
        cur.execute(
            "SELECT id, name, website_url FROM manufacturers "
            "WHERE (logo_url IS NULL OR logo_url = '') "
            "AND website_url IS NOT NULL AND website_url != ''"
        )
        rows = cur.fetchall()

    if not rows:
        log.info("所有厂商都已有 logo，跳过")
        return

    log.info("开始抓取 %d 个厂商缺失的 logo...", len(rows))
    ok = 0
    for i, row in enumerate(rows):
        logo = _fetch_logo_from_website(row["website_url"], row["name"])
        if logo:
            with conn.cursor() as cur:
                cur.execute("UPDATE manufacturers SET logo_url = %s WHERE id = %s", (logo, row["id"]))
                conn.commit()
            ok += 1
            log.info("  [%d/%d] %s: %s", i + 1, len(rows), row["name"], logo.split("/")[-1])
        else:
            log.info("  [%d/%d] %s: 未找到 logo", i + 1, len(rows), row["name"])
        time.sleep(0.3)

    log.info("厂商 logo 抓取完成: %d/%d", ok, len(rows))
    conn.close()


def _fetch_logo_from_website(website_url: str, name: str) -> str:
    """尝试从官网抓取 favicon/logo 并上传到 MinIO"""
    if not website_url:
        return ""
    parsed = urlparse(website_url)
    base = f"{parsed.scheme}://{parsed.netloc}"

    # 1. 从首页 HTML 解析 <link rel="icon"> 标签
    try:
        with httpx.Client(headers=HEADERS, follow_redirects=True, timeout=10) as c:
            r = c.get(website_url)
            if r.status_code == 200:
                import re as _re
                icons = _re.findall(
                    r'<link[^>]*rel=["\'](?:icon|shortcut icon|apple-touch-icon)["\'][^>]*href=["\']([^"\']+)["\']',
                    r.text[:20000], _re.I
                )
                icons += _re.findall(
                    r'<link[^>]*href=["\']([^"\']+)["\'][^>]*rel=["\'](?:icon|shortcut icon|apple-touch-icon)["\']',
                    r.text[:20000], _re.I
                )
                for icon_href in icons:
                    from urllib.parse import urljoin as _urljoin
                    result = _try_download_logo(_urljoin(website_url, icon_href), name)
                    if result:
                        return result
    except Exception:
        pass

    # 2. 猜常见路径
    for path in ["/favicon.ico", "/favicon.png", "/apple-touch-icon.png"]:
        result = _try_download_logo(base + path, name)
        if result:
            return result

    # 3. Google favicon API 兜底
    result = _try_download_logo(f"https://www.google.com/s2/favicons?domain={parsed.netloc}&sz=128", name)
    if result:
        return result

    return ""


def _try_download_logo(url: str, name: str) -> str:
    """下载图片并上传到 MinIO，返回相对路径"""
    client = get_minio_client()
    if client is None:
        return ""
    bucket = os.getenv("MINIO_BUCKET", "lookforbest")
    try:
        with httpx.Client(headers=HEADERS, follow_redirects=True, timeout=8) as c:
            r = c.get(url)
            if r.status_code != 200 or len(r.content) < 50:
                return ""
            ct = r.headers.get("content-type", "")
            if not any(t in ct for t in ["image", "icon", "svg", "octet"]):
                if not url.endswith(('.ico', '.png', '.svg', '.webp', '.jpg')):
                    return ""
            ext = ".png"
            if "svg" in ct or url.endswith(".svg"):
                ext = ".svg"
            elif "ico" in ct or url.endswith(".ico"):
                ext = ".ico"
            obj = f"manufacturers/{hashlib.md5(name.encode()).hexdigest()}{ext}"
            try:
                client.stat_object(bucket, obj)
                return f"/{bucket}/{obj}"
            except Exception:
                pass
            if not ct or "text" in ct:
                ct = "image/png"
            import io
            client.put_object(bucket, obj, io.BytesIO(r.content), len(r.content), content_type=ct)
            return f"/{bucket}/{obj}"
    except Exception:
        return ""


if __name__ == "__main__":
    main()
