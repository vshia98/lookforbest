"""
iRobot 爬虫
目标: https://www.irobot.com/en_US/roomba-robot-vacuums/c/roomba-category
覆盖: Roomba 扫地机器人、Braava 拖地机器人等
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class iRobotSpider(BaseSpider):
    name = "irobot"
    manufacturer_name = "iRobot"
    manufacturer_name_en = "iRobot"
    manufacturer_country = "美国"
    manufacturer_country_code = "US"
    manufacturer_website = "https://www.irobot.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.irobot.com/en_US/roomba-robot-vacuums/c/roomba-category")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".product-tile, .product-card, [class*='product']")
            for card in cards:
                name_el = card.select_one("h2, h3, .product-name, [class*='name']")
                desc_el = card.select_one("p, .product-desc, [class*='desc']")
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
                    "category": "家用机器人",
                    "category_en": "Home Robot",
                    "price_range": "low",
                    "status": "active",
                })
            if robots:
                return robots
        except Exception as e:
            print(f"[irobot] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "Roomba j9+",
                "name_en": "iRobot Roomba j9+",
                "description": "iRobot 旗舰扫地机器人，搭载 Clean Base 自动集尘底座，支持 AI 障碍物识别，可识别宠物粪便等 80 种障碍物",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "家用机器人",
                "category_en": "Home Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "Roomba j7+",
                "name_en": "iRobot Roomba j7+",
                "description": "iRobot 智能扫地机器人，AI 障碍物识别，支持 Imprint 智能绘图，与 Braava jet m6 联动清洁",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "家用机器人",
                "category_en": "Home Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "Roomba s9+",
                "name_en": "iRobot Roomba s9+",
                "description": "iRobot 高端扫地机器人，D 形设计更贴边，Perfect Edge 技术深度清洁角落，自动清空集尘盒",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "家用机器人",
                "category_en": "Home Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "Roomba Combo j9+",
                "name_en": "iRobot Roomba Combo j9+",
                "description": "iRobot 扫拖一体机器人，智能抬起拖布模块，遇到地毯自动抬起，支持自动清空与自动补水",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "家用机器人",
                "category_en": "Home Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "Braava jet m6",
                "name_en": "iRobot Braava jet m6",
                "description": "iRobot 智能拖地机器人，支持精准喷水与高效湿拖，可与 Roomba 扫地机器人联动，完成扫拖全流程",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "家用机器人",
                "category_en": "Home Robot",
                "price_range": "low",
                "status": "active",
            },
            {
                "name": "iRobot Terra t7",
                "name_en": "iRobot Terra t7",
                "description": "iRobot 草坪割草机器人，智能导航无需埋线，自动划区修剪，可通过 App 控制割草模式与时间",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "户外机器人",
                "category_en": "Outdoor Robot",
                "price_range": "medium",
                "status": "active",
            },
        ]
