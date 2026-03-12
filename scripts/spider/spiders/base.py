"""爬虫基类"""
import time
import os
import logging
import requests
from fake_useragent import UserAgent
from dotenv import load_dotenv

load_dotenv()

_ua = UserAgent()
logger = logging.getLogger(__name__)


def make_session() -> requests.Session:
    s = requests.Session()
    s.headers.update({"User-Agent": _ua.random, "Accept-Language": "en-US,en;q=0.9"})
    return s


def polite_get(session: requests.Session, url: str, **kwargs) -> requests.Response:
    """带礼貌延迟与指数退避的 GET 请求"""
    delay = float(os.getenv("SPIDER_DELAY", 2.0))
    timeout = int(os.getenv("SPIDER_TIMEOUT", 30))
    retries = int(os.getenv("SPIDER_MAX_RETRIES", 3))

    last_exc: Exception | None = None
    for attempt in range(retries):
        try:
            resp = session.get(url, timeout=timeout, **kwargs)
            resp.raise_for_status()
            time.sleep(delay)
            return resp
        except requests.exceptions.HTTPError as e:
            status = e.response.status_code if e.response is not None else 0
            # 4xx 客户端错误不重试（目标页面不存在或被拒绝）
            if status and 400 <= status < 500:
                logger.warning("[http] %s 返回 %s，不重试", url, status)
                raise
            last_exc = e
            logger.warning("[http] 第%d次请求失败 %s: %s", attempt + 1, url, e)
        except requests.exceptions.ConnectionError as e:
            last_exc = e
            logger.warning("[http] 连接错误 第%d次 %s: %s", attempt + 1, url, e)
        except requests.exceptions.Timeout as e:
            last_exc = e
            logger.warning("[http] 超时 第%d次 %s: %s", attempt + 1, url, e)
        except Exception as e:
            last_exc = e
            logger.warning("[http] 未知错误 第%d次 %s: %s", attempt + 1, url, e)

        wait = delay * (2 ** attempt)  # 指数退避
        logger.info("[http] 等待 %.1f 秒后重试...", wait)
        time.sleep(wait)

    raise RuntimeError(f"请求失败（已重试{retries}次）: {url}") from last_exc


class BaseSpider:
    """
    每个厂商爬虫继承此类，实现 crawl() 方法。
    crawl() 返回 list[dict]，每个 dict 包含机器人数据。
    """

    name: str = "base"
    manufacturer_name: str = ""
    manufacturer_name_en: str = ""
    manufacturer_country: str = ""
    manufacturer_country_code: str = ""
    manufacturer_website: str = ""

    def __init__(self):
        self.session = make_session()

    def crawl(self) -> list[dict]:
        raise NotImplementedError

    def manufacturer_info(self) -> dict:
        return {
            "name": self.manufacturer_name,
            "name_en": self.manufacturer_name_en,
            "country": self.manufacturer_country,
            "country_code": self.manufacturer_country_code,
            "website_url": self.manufacturer_website,
            "description": None,
        }
