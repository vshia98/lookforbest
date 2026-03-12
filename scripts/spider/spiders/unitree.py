"""
宇树科技 (Unitree Robotics) 爬虫
目标: https://www.unitree.com/robots/
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class UnitreeSpider(BaseSpider):
    name = "unitree"
    manufacturer_name = "宇树科技"
    manufacturer_name_en = "Unitree Robotics"
    manufacturer_country = "中国"
    manufacturer_country_code = "CN"
    manufacturer_website = "https://www.unitree.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.unitree.com/robots/")
            soup = BeautifulSoup(resp.text, "lxml")

            cards = soup.select(".robot-item, .product-item, [class*='robot-card']")
            for card in cards:
                name_el = card.select_one("h2, h3, .name, [class*='title']")
                desc_el = card.select_one("p, .desc")
                img_el = card.select_one("img")
                name = name_el.get_text(strip=True) if name_el else None
                if not name:
                    continue
                robots.append({
                    "name": name,
                    "name_en": name,
                    "description": desc_el.get_text(strip=True) if desc_el else None,
                    "cover_image_url_raw": img_el.get("src") or img_el.get("data-src") if img_el else None,
                    "video_urls": [],
                    "category": "四足机器人",
                    "category_en": "Quadruped Robot",
                    "price_range": "medium",
                    "status": "active",
                })
        except Exception as e:
            print(f"[unitree] 爬取失败: {e}")
            robots = self._seed_data()
        return robots

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "宇树 H1",
                "name_en": "Unitree H1",
                "description": "通用人形机器人，身高1.8m，具备全身运动控制能力，续航时间约2小时",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "人形机器人",
                "category_en": "Humanoid Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "宇树 G1",
                "name_en": "Unitree G1",
                "description": "高性价比人形机器人，起售价9.9万元，具备灵巧手与全身运动能力",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "人形机器人",
                "category_en": "Humanoid Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "宇树 B2",
                "name_en": "Unitree B2",
                "description": "工业级四足机器人，负载超40kg，适用于电力巡检、矿山作业等场景",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "四足机器人",
                "category_en": "Quadruped Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "宇树 Go2",
                "name_en": "Unitree Go2",
                "description": "消费级四足机器人，搭载3D激光雷达，支持语音交互，续航约2-3小时",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "四足机器人",
                "category_en": "Quadruped Robot",
                "price_range": "low",
                "status": "active",
            },
        ]
