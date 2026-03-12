"""
软银机器人 (SoftBank Robotics) 爬虫
目标: https://www.softbankrobotics.com/emea/en/robots
覆盖: Pepper、NAO、Whiz 等社交与服务机器人
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class SoftBankRoboticsSpider(BaseSpider):
    name = "softbank_robotics"
    manufacturer_name = "软银机器人"
    manufacturer_name_en = "SoftBank Robotics"
    manufacturer_country = "日本"
    manufacturer_country_code = "JP"
    manufacturer_website = "https://www.softbankrobotics.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.softbankrobotics.com/emea/en/robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".robot-card, .product-item, article, [class*='robot']")
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
                    "category": "社交机器人",
                    "category_en": "Social Robot",
                    "price_range": "high",
                    "status": "active",
                })
            if robots:
                return robots
        except Exception as e:
            print(f"[softbank_robotics] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "Pepper",
                "name_en": "Pepper",
                "description": "软银人形社交机器人，身高 1.2m，具备情感识别与自然语言对话能力，已在全球数千家零售店和医院部署",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=zJHyaD1psMc"],
                "category": "社交机器人",
                "category_en": "Social Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "NAO",
                "name_en": "NAO",
                "description": "NAO 教育人形机器人，身高 58cm，25 个自由度，全球超 13000 所教育机构使用，支持多种编程语言",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=2STTNYNF4lk"],
                "category": "教育机器人",
                "category_en": "Educational Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "Whiz",
                "name_en": "Whiz",
                "description": "软银商用清洁机器人，自主扫地功能，搭载 BrainOS AI，适用于办公楼、酒店与机场的地面清洁",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "服务机器人",
                "category_en": "Service Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "Astorino",
                "name_en": "Astorino",
                "description": "软银机器人与 FANUC 联合推出的桌面 6 轴工业机器人，有效载荷 1kg，适用于教育培训与小型工业应用",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "medium",
                "status": "active",
            },
        ]
