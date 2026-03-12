"""HTTP 抓取器，支持重试、速率限制"""
import logging
import time
from typing import Optional

import httpx
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

logger = logging.getLogger(__name__)

HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/122.0.0.0 Safari/537.36"
    ),
    "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
}


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type((httpx.HTTPError, httpx.TimeoutException)),
    reraise=True,
)
def fetch_page(url: str, timeout: int = 15) -> Optional[str]:
    """同步 HTTP GET，返回响应文本；失败自动重试 3 次"""
    with httpx.Client(headers=HEADERS, follow_redirects=True, timeout=timeout) as client:
        resp = client.get(url)
        resp.raise_for_status()
        return resp.text


def fetch_pages(urls: list[str], delay_ms: int = 1000) -> list[tuple[str, Optional[str]]]:
    """批量抓取，每次请求间隔 delay_ms 毫秒，返回 [(url, html), ...]"""
    results = []
    for i, url in enumerate(urls):
        if i > 0:
            time.sleep(delay_ms / 1000)
        try:
            html = fetch_page(url)
            results.append((url, html))
        except Exception as e:
            logger.warning("抓取失败 %s: %s", url, e)
            results.append((url, None))
    return results
