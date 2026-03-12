#!/usr/bin/env python3
"""
机器人数据爬虫主入口
用法：
  python main.py              # 运行全部爬虫
  python main.py --dry-run    # 只爬取，不写入数据库
  python main.py --seed-only  # 仅写入静态种子数据
  python main.py --validate   # 爬取后打印数据质量报告，不写入
"""
import argparse
import logging
import re
import sys
import os

from dotenv import load_dotenv

load_dotenv()

from spiders.boston_dynamics import BostonDynamicsSpider
from spiders.unitree import UnitreeSpider
from spiders.agility import AgilitySpider
from spiders.dji import DJISpider
from spiders.fanuc import FANUCSpider
from spiders.abb import ABBSpider
from spiders.kuka import KUKASpider
from spiders.irobot import iRobotSpider
from spiders.softbank_robotics import SoftBankRoboticsSpider
from spiders.additional_seeds import AdditionalSeedsSpider
import db
import storage
from validate import validate_batch, print_validation_report

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    datefmt="%H:%M:%S",
)


def slugify(text: str) -> str:
    text = text.lower().strip()
    text = re.sub(r"[^\w\s-]", "", text)
    text = re.sub(r"[\s_]+", "-", text)
    return text[:280]


def run_spider(spider_instance, conn, dry_run: bool = False, validate: bool = False):
    print(f"\n=== 运行爬虫: {spider_instance.name} ===")
    robots = spider_instance.crawl()
    print(f"  采集到 {len(robots)} 条机器人数据（校验前）")

    # 数据质量校验
    valid_robots, errors = validate_batch(robots)
    print_validation_report(spider_instance.name, len(robots), len(valid_robots), errors)
    robots = valid_robots

    if dry_run or validate:
        for r in robots:
            print(f"  [dry-run] {r.get('name_en', r.get('name'))}")
        return

    if not robots:
        return

    mfr_info = spider_instance.manufacturer_info()
    mfr_id = db.upsert_manufacturer(conn, mfr_info)
    print(f"  厂商 ID: {mfr_id} ({mfr_info['name_en']})")

    for r in robots:
        cat_id = db.get_or_create_category(conn, r["category"], r.get("category_en"))
        slug = slugify(r.get("name_en") or r["name"])
        # 避免 slug 冲突
        base_slug = slug
        for i in range(1, 20):
            import pymysql
            with conn.cursor() as cur:
                cur.execute("SELECT id FROM robots WHERE slug = %s", (slug,))
                if cur.fetchone() is None:
                    break
            slug = f"{base_slug}-{i}"

        # 下载封面图并上传到 MinIO
        cover_url = r.get("cover_image_url_raw")
        if cover_url and cover_url.startswith("http"):
            minio_url = storage.download_and_upload(cover_url, "images")
            if minio_url:
                cover_url = minio_url

        robot_record = {
            "manufacturer_id": mfr_id,
            "category_id": cat_id,
            "name": r["name"],
            "name_en": r.get("name_en", r["name"]),
            "slug": slug,
            "description": r.get("description"),
            "cover_image_url": cover_url,
            "price_range": r.get("price_range", "inquiry"),
            "status": r.get("status", "active"),
        }
        robot_id = db.upsert_robot(conn, robot_record)
        print(f"  + Robot: [{robot_id}] {robot_record['name_en']} (slug={slug})")

        # 写入图片
        if cover_url:
            db.insert_robot_image(conn, robot_id, cover_url, is_primary=True)

        # 写入视频
        for video_url in r.get("video_urls", []):
            db.insert_robot_video(conn, robot_id, video_url)


def run_additional_seeds(conn, dry_run: bool = False, validate: bool = False):
    print("\n=== 补充种子数据 ===")
    total_written = 0
    for mfr_info, products in AdditionalSeedsSpider.SEED_DATA:
        # 数据质量校验
        valid_products, errors = validate_batch(products)
        if errors:
            for e in errors:
                print(f"    ⚠️  {e}")

        if dry_run or validate:
            print(f"  [dry-run] 厂商: {mfr_info['name_en']}, {len(valid_products)} 个产品")
            continue

        mfr_id = db.upsert_manufacturer(conn, mfr_info)
        for r in valid_products:
            cat_id = db.get_or_create_category(conn, r["category"], r.get("category_en"))
            slug = slugify(r.get("name_en") or r["name"])
            robot_record = {
                "manufacturer_id": mfr_id,
                "category_id": cat_id,
                "name": r["name"],
                "name_en": r.get("name_en", r["name"]),
                "slug": slug,
                "description": r.get("description"),
                "cover_image_url": None,
                "price_range": r.get("price_range", "inquiry"),
                "status": r.get("status", "active"),
            }
            robot_id = db.upsert_robot(conn, robot_record)
            print(f"  + Seed: [{robot_id}] {robot_record['name_en']}")
            total_written += 1

    if not dry_run and not validate:
        print(f"\n  种子数据写入完成，共 {total_written} 条")


def main():
    parser = argparse.ArgumentParser(description="机器人数据爬虫")
    parser.add_argument("--dry-run", action="store_true", help="只爬取，不写入数据库")
    parser.add_argument("--seed-only", action="store_true", help="仅写入静态种子数据")
    parser.add_argument("--validate", action="store_true", help="数据质量校验模式（不写入数据库）")
    args = parser.parse_args()

    skip_db = args.dry_run or args.validate
    conn = None if skip_db else db.get_connection()

    spiders = [
        BostonDynamicsSpider(),
        UnitreeSpider(),
        AgilitySpider(),
        DJISpider(),
        FANUCSpider(),
        ABBSpider(),
        KUKASpider(),
        iRobotSpider(),
        SoftBankRoboticsSpider(),
    ]

    if not args.seed_only:
        for spider in spiders:
            run_spider(spider, conn, dry_run=args.dry_run, validate=args.validate)

    run_additional_seeds(conn, dry_run=args.dry_run, validate=args.validate)

    if conn:
        conn.close()

    print("\n爬虫任务完成。")


if __name__ == "__main__":
    main()
