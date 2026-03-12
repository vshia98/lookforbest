"""MinIO 图片下载与上传工具"""
import os
import io
import hashlib
import requests
from minio import Minio
from minio.error import S3Error
from dotenv import load_dotenv

load_dotenv()

_client = None


def _get_client() -> Minio:
    global _client
    if _client is None:
        endpoint = os.getenv("MINIO_ENDPOINT", "http://localhost:9000").replace("http://", "").replace("https://", "")
        secure = os.getenv("MINIO_ENDPOINT", "").startswith("https")
        _client = Minio(
            endpoint,
            access_key=os.getenv("MINIO_ACCESS_KEY", "minioadmin"),
            secret_key=os.getenv("MINIO_SECRET_KEY", "minioadmin"),
            secure=secure,
        )
        _ensure_bucket(_client)
    return _client


def _ensure_bucket(client: Minio):
    bucket = os.getenv("MINIO_BUCKET", "lookforbest")
    if not client.bucket_exists(bucket):
        client.make_bucket(bucket)
        policy = f"""{{
            "Version":"2012-10-17",
            "Statement":[{{"Effect":"Allow","Principal":{{"AWS":["*"]}},
            "Action":["s3:GetObject"],"Resource":["arn:aws:s3:::{bucket}/*"]}}]
        }}"""
        client.set_bucket_policy(bucket, policy)


def download_and_upload(image_url: str, folder: str = "images") -> str | None:
    """
    从远端下载图片并上传到 MinIO。
    返回 MinIO 公开访问 URL，失败返回 None。
    """
    if not image_url:
        return None
    try:
        resp = requests.get(image_url, timeout=int(os.getenv("SPIDER_TIMEOUT", 30)), stream=True)
        resp.raise_for_status()
        content = resp.content

        # 用 URL hash 做对象名，避免重复上传
        ext = image_url.split("?")[0].rsplit(".", 1)[-1].lower()
        if ext not in ("jpg", "jpeg", "png", "webp", "gif"):
            ext = "jpg"
        obj_name = f"{folder}/{hashlib.md5(image_url.encode()).hexdigest()}.{ext}"
        content_type = resp.headers.get("Content-Type", "image/jpeg").split(";")[0]

        client = _get_client()
        bucket = os.getenv("MINIO_BUCKET", "lookforbest")
        client.put_object(
            bucket, obj_name, io.BytesIO(content), len(content), content_type=content_type
        )
        endpoint = os.getenv("MINIO_ENDPOINT", "http://localhost:9000")
        return f"{endpoint}/{bucket}/{obj_name}"
    except Exception as e:
        print(f"[storage] 图片上传失败 {image_url}: {e}")
        return None
