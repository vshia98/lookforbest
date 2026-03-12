"""
KUKA 库卡 爬虫
目标: https://www.kuka.com/en-de/products/robot-systems/industrial-robots
覆盖: 工业机械臂、协作机器人 iiwa、移动机器人 KMR 等
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class KUKASpider(BaseSpider):
    name = "kuka"
    manufacturer_name = "库卡"
    manufacturer_name_en = "KUKA"
    manufacturer_country = "德国"
    manufacturer_country_code = "DE"
    manufacturer_website = "https://www.kuka.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.kuka.com/en-de/products/robot-systems/industrial-robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".product-teaser, .robot-card, article, [class*='product']")
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
            print(f"[kuka] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "KUKA KR Agilus",
                "name_en": "KUKA KR Agilus",
                "description": "KUKA 小型高速工业机器人，有效载荷 6kg，速度极快，适用于电子、医疗与光学行业的精密装配",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "KUKA KR Quantec",
                "name_en": "KUKA KR Quantec",
                "description": "KUKA 中大型工业机器人系列，有效载荷 90–300kg，适用于汽车制造、铸造与重型搬运",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "KUKA LBR iiwa",
                "name_en": "KUKA LBR iiwa",
                "description": "KUKA 协作机器人，7 轴冗余设计，每个关节内置扭矩传感器，可感知碰撞并立即停止，适用于人机协作",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=80pDVJhFSXA"],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "KUKA LBR iisy",
                "name_en": "KUKA LBR iisy",
                "description": "KUKA 新一代协作机器人，有效载荷 3/11kg，即插即用设计，无需专业编程知识，适合中小型企业部署",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "KUKA KMR iiwa",
                "name_en": "KUKA KMR iiwa",
                "description": "KUKA 自主移动机器人，集成移动底盘与 LBR iiwa 机械臂，可在工厂内自由移动并执行装配任务",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "移动机器人",
                "category_en": "Mobile Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "KUKA KR 1000 Titan",
                "name_en": "KUKA KR 1000 Titan",
                "description": "KUKA 超重型工业机器人，有效载荷 1000kg，工作范围 3200mm，是世界上负载能力最强的机器人之一",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
        ]
