"""
FANUC 发那科 爬虫
目标: https://www.fanuc.eu/uk/en/robots
覆盖: 工业机械臂、协作机器人、SCARA 等
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class FANUCSpider(BaseSpider):
    name = "fanuc"
    manufacturer_name = "发那科"
    manufacturer_name_en = "FANUC"
    manufacturer_country = "日本"
    manufacturer_country_code = "JP"
    manufacturer_website = "https://www.fanuc.eu"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.fanuc.eu/uk/en/robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".product-tile, .robot-card, article, [class*='product']")
            for card in cards:
                name_el = card.select_one("h2, h3, h4, .title, [class*='name']")
                desc_el = card.select_one("p, .description, [class*='desc']")
                img_el = card.select_one("img")
                name = name_el.get_text(strip=True) if name_el else None
                if not name or len(name) < 2:
                    continue
                robots.append({
                    "name": name,
                    "name_en": name,
                    "description": desc_el.get_text(strip=True) if desc_el else None,
                    "cover_image_url_raw": img_el.get("src") or img_el.get("data-src") if img_el else None,
                    "video_urls": [],
                    "category": "工业机器人",
                    "category_en": "Industrial Robot",
                    "price_range": "high",
                    "status": "active",
                })
            if robots:
                return robots
        except Exception as e:
            print(f"[fanuc] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "FANUC R-2000iC",
                "name_en": "FANUC R-2000iC",
                "description": "FANUC 重型工业机械臂，有效载荷最高 270kg，工作范围 2655mm，适用于汽车焊接与搬运",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC M-10iD",
                "name_en": "FANUC M-10iD",
                "description": "中型工业机械臂，有效载荷 12kg，工作范围 1441mm，适用于机器管理、装配与物料搬运",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC M-20iD",
                "name_en": "FANUC M-20iD",
                "description": "高性能工业机械臂，有效载荷 20kg，具备高速度与高精度，适用于弧焊、点焊与装配",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC LR Mate 200iD",
                "name_en": "FANUC LR Mate 200iD",
                "description": "小型工业机械臂（桌面型），有效载荷 7kg，高速轻巧，适用于电子装配与精密加工",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "FANUC CR-35iA",
                "name_en": "FANUC CR-35iA",
                "description": "FANUC 协作机器人，有效载荷 35kg，全球负载最大的协作机器人之一，支持人机直接协作",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC CR-7iA",
                "name_en": "FANUC CR-7iA",
                "description": "FANUC 协作机器人，有效载荷 7kg，可与人安全协同工作，内置力矩传感器，无需安全围栏",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC M-1iA",
                "name_en": "FANUC M-1iA",
                "description": "FANUC Delta 并联机器人，速度极快，每分钟拾放次数超 200 次，适用于食品与电子行业高速分拣",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "并联机器人",
                "category_en": "Delta Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "FANUC SCARA SR-6iA",
                "name_en": "FANUC SCARA SR-6iA",
                "description": "FANUC SCARA 水平关节机器人，有效载荷 6kg，重复定位精度 ±0.01mm，适用于装配与精密上下料",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "SCARA机器人",
                "category_en": "SCARA Robot",
                "price_range": "medium",
                "status": "active",
            },
        ]
