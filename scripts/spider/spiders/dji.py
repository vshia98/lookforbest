"""
大疆创新 (DJI) 爬虫
目标: https://www.dji.com/products/educational-robots
覆盖: RoboMaster 教育机器人系列
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class DJISpider(BaseSpider):
    name = "dji"
    manufacturer_name = "大疆创新"
    manufacturer_name_en = "DJI"
    manufacturer_country = "中国"
    manufacturer_country_code = "CN"
    manufacturer_website = "https://www.dji.com"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://www.dji.com/products/educational-robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".product-card, .item-card, [class*='product']")
            for card in cards:
                name_el = card.select_one("h2, h3, .name, [class*='title']")
                desc_el = card.select_one("p, .desc, [class*='desc']")
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
                    "category": "教育机器人",
                    "category_en": "Educational Robot",
                    "price_range": "medium",
                    "status": "active",
                })
            if robots:
                return robots
        except Exception as e:
            print(f"[dji] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "RoboMaster EP Core",
                "name_en": "RoboMaster EP Core",
                "description": "大疆教育级机器人，支持 Python/Scratch 编程，具备底盘、机械臂与发射器模块，适用于 STEM 教育与机器人竞赛",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=Sca5KaL0j5U"],
                "category": "教育机器人",
                "category_en": "Educational Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "RoboMaster S1",
                "name_en": "RoboMaster S1",
                "description": "大疆消费级教育机器人，搭载 FPV 摄像头与智能弹珠发射器，支持多人对战与 AI 视觉识别挑战",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=ePLDWdZdBhs"],
                "category": "教育机器人",
                "category_en": "Educational Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "RoboMaster TT",
                "name_en": "RoboMaster TT (Tello Talent)",
                "description": "大疆 Tello 教育无人机，可通过 SDK 编程控制，支持 Scratch/Python，适合无人机编程课程",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "无人机",
                "category_en": "Drone",
                "price_range": "low",
                "status": "active",
            },
            {
                "name": "Manifold 2",
                "name_en": "Manifold 2",
                "description": "大疆机器人计算平台，搭载 NVIDIA Jetson 处理器，适用于无人机与机器人的 AI 推理和边缘计算",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "机器人平台",
                "category_en": "Robot Platform",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "Agras T50",
                "name_en": "Agras T50",
                "description": "大疆农业无人机，50kg 载重，支持 AI 障碍感知与精准变量施药，覆盖作业效率超 200 亩/小时",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "农业机器人",
                "category_en": "Agricultural Robot",
                "price_range": "high",
                "status": "active",
            },
        ]
