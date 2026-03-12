"""
ABB 机器人 爬虫
目标: https://new.abb.com/products/robotics/industrial-robots
覆盖: 工业机械臂、协作机器人 YuMi、GoFa 等
"""
from bs4 import BeautifulSoup
from .base import BaseSpider, polite_get


class ABBSpider(BaseSpider):
    name = "abb"
    manufacturer_name = "ABB"
    manufacturer_name_en = "ABB"
    manufacturer_country = "瑞士"
    manufacturer_country_code = "CH"
    manufacturer_website = "https://new.abb.com/products/robotics"

    def crawl(self) -> list[dict]:
        robots = []
        try:
            resp = polite_get(self.session, "https://new.abb.com/products/robotics/industrial-robots")
            soup = BeautifulSoup(resp.text, "lxml")
            cards = soup.select(".product-card, .robot-item, article, [class*='product']")
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
            print(f"[abb] 爬取失败: {e}")
        return self._seed_data()

    def _seed_data(self) -> list[dict]:
        return [
            {
                "name": "ABB IRB 120",
                "name_en": "ABB IRB 120",
                "description": "ABB 小型工业机械臂，有效载荷 3kg，工作范围 580mm，最快的小型机器人之一，适用于电子装配",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "ABB IRB 1200",
                "name_en": "ABB IRB 1200",
                "description": "ABB 紧凑型机械臂，有效载荷 7kg，工作范围 703mm，适用于机器管理与装配，结构紧凑",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "medium",
                "status": "active",
            },
            {
                "name": "ABB IRB 4600",
                "name_en": "ABB IRB 4600",
                "description": "ABB 中大型工业机械臂，有效载荷 45kg，工作范围 2050mm，适用于弧焊、喷涂与物料搬运",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "ABB YuMi",
                "name_en": "ABB YuMi",
                "description": "ABB 双臂协作机器人，全球首款真正双臂协作机器人，7+7 轴，有效载荷 0.5kg/臂，适用于小零件精密装配",
                "cover_image_url_raw": None,
                "video_urls": ["https://www.youtube.com/watch?v=kB-s0kXD8Ks"],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "ABB GoFa CRB 15000",
                "name_en": "ABB GoFa CRB 15000",
                "description": "ABB 协作机器人 GoFa，有效载荷 5kg，工作范围 950mm，速度快、安全性高，无需安全围栏",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "ABB SWIFTI CRB 1100",
                "name_en": "ABB SWIFTI CRB 1100",
                "description": "ABB 高速协作机器人，有效载荷 4kg，速度比同类协作机器人快 3 倍，适用于电子与轻工业装配",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "协作机器人",
                "category_en": "Collaborative Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "ABB IRB 6700",
                "name_en": "ABB IRB 6700",
                "description": "ABB 大型工业机械臂，有效载荷最高 300kg，工作范围 3200mm，为重型搬运与点焊而设计",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "工业机器人",
                "category_en": "Industrial Robot",
                "price_range": "high",
                "status": "active",
            },
            {
                "name": "ABB OmniCore",
                "name_en": "ABB OmniCore Controller",
                "description": "ABB 新一代机器人控制器，支持 AI 加速运算，能耗降低 20%，适配 ABB 全系列工业与协作机器人",
                "cover_image_url_raw": None,
                "video_urls": [],
                "category": "机器人平台",
                "category_en": "Robot Platform",
                "price_range": "high",
                "status": "active",
            },
        ]
