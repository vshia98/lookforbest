"""
Agility Robotics 爬虫
目标: https://agilityrobotics.com/robots
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class AgilitySpider(BaseSpider):
    name = "agility"
    manufacturer_name = "Agility Robotics"
    manufacturer_name_en = "Agility Robotics"
    manufacturer_country = "美国"
    manufacturer_country_code = "US"
    manufacturer_website = "https://agilityrobotics.com"

    def crawl(self) -> list[dict]:
        try:
            resp = polite_get(self.session, "https://agilityrobotics.com/robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".robot-section, article, [class*='product']")
            robots = []
            for card in cards:
                name_el = card.select_one("h1, h2, h3")
                desc_el = card.select_one("p")
                img_el = card.select_one("img")
                name = name_el.get_text(strip=True) if name_el else None
                if not name or len(name) < 2:
                    continue
                robots.append({
                    "name": name,
                    "name_en": name,
                    "description": desc_el.get_text(strip=True) if desc_el else None,
                    "cover_image_url_raw": img_el.get("src") if img_el else None,
                    "video_urls": [],
                    "category": "人形机器人",
                    "category_en": "Humanoid Robot",
                    "price_range": "inquiry",
                    "status": "active",
                })
            if robots:
                return robots
        except Exception as e:
            print(f"[agility] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "Digit",
                "name_en": "Digit",
                "description": "双足人形机器人，专为仓储物流末端配送设计，具备搬运包裹与爬梯能力",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=tpGGa_ZJcL4"],
                "category": "人形机器人",
                "category_en": "Humanoid Robot",
                "price_range": "inquiry",
                "status": "active",
            }
        ]
