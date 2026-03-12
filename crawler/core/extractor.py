"""基于 selectolax 的 CSS 选择器提取器"""
import logging
import re
from typing import Any, Optional

from selectolax.parser import HTMLParser

logger = logging.getLogger(__name__)


def _text(node, strip: bool = True) -> str:
    if node is None:
        return ""
    t = node.text(deep=True, separator=" ", strip=strip)
    return t or ""


def _attr(node, attr: str) -> str:
    if node is None:
        return ""
    return node.attributes.get(attr, "") or ""


def extract_field(tree: HTMLParser, selector: str, attr: str = None, regex: str = None) -> str:
    """从 HTML 树中提取单个字段值"""
    try:
        node = tree.css_first(selector)
        if node is None:
            return ""
        value = _attr(node, attr) if attr else _text(node)
        if regex and value:
            m = re.search(regex, value)
            value = m.group(1).strip() if m else ""
        return value.strip()
    except Exception as e:
        logger.debug("extract_field error selector=%s: %s", selector, e)
        return ""


def extract_list(tree: HTMLParser, selector: str, attr: str = None, regex: str = None) -> list[str]:
    """从 HTML 树中提取多个节点的值列表"""
    try:
        nodes = tree.css(selector)
        results = []
        for node in nodes:
            value = _attr(node, attr) if attr else _text(node)
            if regex and value:
                m = re.search(regex, value)
                value = m.group(1).strip() if m else ""
            if value.strip():
                results.append(value.strip())
        return results
    except Exception as e:
        logger.debug("extract_list error selector=%s: %s", selector, e)
        return []


def extract_item(html: str, field_rules: dict) -> dict:
    """
    根据字段规则从 HTML 中提取机器人数据。
    field_rules 格式:
    {
      "name":        {"selector": "h1.product-title", "attr": null},
      "name_en":     {"selector": "h1.product-title"},
      "description": {"selector": "div.product-desc"},
      "cover_image_url": {"selector": "img.main-image", "attr": "src"},
      "price_range": {"selector": "span.price", "regex": "(\\d+)"},
      "payload_kg":  {"selector": "td.payload", "regex": "([\\d.]+)"},
      "category":    {"value": "工业机器人"},     # 静态值
    }
    """
    tree = HTMLParser(html)
    result = {}
    for field, rule in field_rules.items():
        if "value" in rule:
            result[field] = rule["value"]
        elif "selector" in rule:
            result[field] = extract_field(
                tree,
                rule["selector"],
                attr=rule.get("attr"),
                regex=rule.get("regex"),
            )
        else:
            result[field] = ""
    return result


def extract_item_urls(html: str, list_selector: str, url_attr: str = "href", base_url: str = "") -> list[str]:
    """从列表页提取子页面 URL"""
    tree = HTMLParser(html)
    raw_urls = extract_list(tree, list_selector, attr=url_attr)
    urls = []
    for u in raw_urls:
        if not u:
            continue
        if u.startswith("http"):
            urls.append(u)
        elif u.startswith("/"):
            # 拼接 base_url
            from urllib.parse import urlparse
            parsed = urlparse(base_url)
            urls.append(f"{parsed.scheme}://{parsed.netloc}{u}")
        elif base_url:
            urls.append(base_url.rstrip("/") + "/" + u.lstrip("/"))
    return list(dict.fromkeys(urls))  # dedup, preserve order


def extract_next_page(html: str, next_page_selector: str, attr: str = "href", base_url: str = "") -> Optional[str]:
    """提取下一页 URL"""
    tree = HTMLParser(html)
    value = extract_field(tree, next_page_selector, attr=attr)
    if not value:
        return None
    if value.startswith("http"):
        return value
    if value.startswith("/"):
        from urllib.parse import urlparse
        parsed = urlparse(base_url)
        return f"{parsed.scheme}://{parsed.netloc}{value}"
    return None
