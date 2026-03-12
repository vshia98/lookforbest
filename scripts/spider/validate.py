"""数据质量校验模块"""
import re
import requests
import logging
from typing import Any

logger = logging.getLogger(__name__)

# 必填字段
REQUIRED_FIELDS = ["name", "category"]

# 有效的 price_range 枚举
VALID_PRICE_RANGES = {"low", "medium", "high", "inquiry"}

# 有效的 status 枚举
VALID_STATUSES = {"active", "upcoming", "discontinued"}

# 图片格式白名单
VALID_IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".webp", ".gif"}

# URL 基础格式
_URL_RE = re.compile(r"^https?://", re.IGNORECASE)


def validate_robot(robot: dict) -> list[str]:
    """
    校验单条机器人数据，返回错误信息列表。
    列表为空表示校验通过。
    """
    errors: list[str] = []
    name = robot.get("name_en") or robot.get("name", "")

    # 1. 必填字段
    for field in REQUIRED_FIELDS:
        if not robot.get(field):
            errors.append(f"[{name}] 缺少必填字段: {field}")

    # 2. name 长度
    if robot.get("name") and len(robot["name"].strip()) < 2:
        errors.append(f"[{name}] name 长度过短: {robot['name']!r}")

    # 3. price_range 枚举
    pr = robot.get("price_range")
    if pr and pr not in VALID_PRICE_RANGES:
        errors.append(f"[{name}] 无效的 price_range: {pr!r}，应为 {VALID_PRICE_RANGES}")

    # 4. status 枚举
    st = robot.get("status")
    if st and st not in VALID_STATUSES:
        errors.append(f"[{name}] 无效的 status: {st!r}，应为 {VALID_STATUSES}")

    # 5. 封面图 URL 格式
    img = robot.get("cover_image_url_raw")
    if img:
        if not _URL_RE.match(img):
            errors.append(f"[{name}] 封面图 URL 格式无效: {img!r}")
        else:
            ext = "." + img.split("?")[0].rsplit(".", 1)[-1].lower() if "." in img else ""
            if ext and ext not in VALID_IMAGE_EXTS:
                errors.append(f"[{name}] 封面图扩展名不受支持: {ext!r}")

    # 6. video_urls 格式
    for v in robot.get("video_urls", []):
        if not _URL_RE.match(v):
            errors.append(f"[{name}] 视频 URL 格式无效: {v!r}")

    return errors


def check_image_accessible(image_url: str, timeout: int = 10) -> bool:
    """
    HEAD 请求检测图片是否可访问。
    返回 True 表示可访问，False 表示不可访问。
    """
    try:
        resp = requests.head(image_url, timeout=timeout, allow_redirects=True)
        return resp.status_code == 200
    except Exception as e:
        logger.debug("[validate] 图片访问失败 %s: %s", image_url, e)
        return False


def validate_batch(robots: list[dict], check_images: bool = False) -> tuple[list[dict], list[str]]:
    """
    批量校验机器人数据，过滤掉有错误的条目。

    Args:
        robots: 待校验的机器人列表
        check_images: 是否在线检测封面图可访问性（较慢，默认关闭）

    Returns:
        (valid_robots, all_errors): 通过校验的列表 和 所有错误信息列表
    """
    valid: list[dict] = []
    all_errors: list[str] = []

    for robot in robots:
        errors = validate_robot(robot)

        if check_images:
            img = robot.get("cover_image_url_raw")
            if img and _URL_RE.match(img):
                if not check_image_accessible(img):
                    errors.append(f"[{robot.get('name_en', robot.get('name'))}] 封面图不可访问: {img}")

        if errors:
            all_errors.extend(errors)
            logger.warning("数据质量问题（已跳过）: %s", "; ".join(errors))
        else:
            valid.append(robot)

    return valid, all_errors


def print_validation_report(spider_name: str, total: int, valid: int, errors: list[str]) -> None:
    """打印数据质量校验报告"""
    print(f"\n  [质检] {spider_name}: 总计 {total} 条，通过 {valid} 条，丢弃 {total - valid} 条")
    if errors:
        for e in errors:
            print(f"    ⚠️  {e}")
