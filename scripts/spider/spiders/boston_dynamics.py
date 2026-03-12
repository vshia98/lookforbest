"""
Boston Dynamics 官网爬虫
目标: https://bostondynamics.com/products/
采集: Spot、Atlas、Stretch、Sparky 等产品
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class BostonDynamicsSpider(BaseSpider):
    name = "boston_dynamics"
    manufacturer_name = "波士顿动力"
    manufacturer_name_en = "Boston Dynamics"
    manufacturer_country = "美国"
    manufacturer_country_code = "US"
    manufacturer_website = "https://bostondynamics.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://bostondynamics.com/products/")
            soup = BeautifulSoup(resp.text, "lxml")

            # 产品卡片选择器（结构可能随官网更新而变化）
            cards = soup.select("div.product-card, article.product, div[class*='product']")

            for card in cards:
                name_el = card.select_one("h2, h3, .product-title, [class*='name']")
                desc_el = card.select_one("p, .product-desc, [class*='description']")
                img_el = card.select_one("img")
                link_el = card.select_one("a[href]")

                name = name_el.get_text(strip=True) if name_el else None
                if not name:
                    continue

                robots.append({
                    "name": name,
                    "name_en": name,
                    "description": desc_el.get_text(strip=True) if desc_el else None,
                    "cover_image_url_raw": img_el.get("src") or img_el.get("data-src") if img_el else None,
                    "video_urls": [],
                    "category": "工业机器人",
                    "category_en": "Industrial Robot",
                    "price_range": "inquiry",
                    "status": "active",
                })

        except Exception as e:
            print(f"[boston_dynamics] 爬取失败: {e}")
            # 降级到硬编码种子数据（确保有基础数据可用）
            robots = self._seed_data()

        return robots

    def _seed_data(self) -> list[dict]:
        """硬编码种子数据，当网络访问失败时使用"""
        return [
            {
                "name": "Spot",
                "name_en": "Spot",
                "description": "四足机器人，适用于工业巡检、数据采集与危险环境作业",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=wlkCQXHEgjA"],
                "category": "四足机器人",
                "category_en": "Quadruped Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "Atlas",
                "name_en": "Atlas",
                "description": "世界上最先进的人形机器人，具备动态运动与复杂操作能力",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=tF4DML7FIWk"],
                "category": "人形机器人",
                "category_en": "Humanoid Robot",
                "price_range": "inquiry",
                "status": "active",
            },
            {
                "name": "Stretch",
                "name_en": "Stretch",
                "description": "移动机器人，专为仓储物流箱体搬运设计",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "物流机器人",
                "category_en": "Logistics Robot",
                "price_range": "high",
                "status": "active",
            },
        ]
