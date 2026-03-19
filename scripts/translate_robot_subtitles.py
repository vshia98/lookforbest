#!/usr/bin/env python3
"""
Batch-translate robot subtitle (one-line tagline) from Chinese to English and
write them into subtitle_en in the `robots` table.

Usage (on the server, from /deploy/lookforbest):

  1) Install dependencies (once):
       pip install --upgrade openai pymysql

  2) Export your OpenAI-compatible API config (here we use minimax via
     OpenAI SDK-compatible interface):

       export OPENAI_API_KEY="..."
       export OPENAI_BASE_URL="https://api.minimaxi.com/v1"
       export OPENAI_MODEL="MiniMax-M2.5"

  3) Run the script:

       cd /deploy/lookforbest
       python3 scripts/translate_robot_subtitles.py

The script:
  - Reads DB config from .env (DB_HOST / DB_PORT / DB_USERNAME / DB_PASSWORD / DB_DATABASE)
    with sensible defaults.
  - Iterates robots where subtitle is non-empty and subtitle_en is NULL/empty.
  - For each subtitle, calls the model to generate a concise English tagline.
  - Uses parameterized UPDATEs to set subtitle_en only when it is currently empty.

It is safe to re-run; existing subtitle_en values are left untouched.
"""

import os
import sys
import time
from typing import Any, Dict, Optional

try:
    import pymysql
except ImportError:
    print("[ERROR] Missing dependency: pymysql")
    print("        Please install it with: pip install pymysql")
    sys.exit(1)

try:
    from openai import OpenAI
except ImportError:
    print("[ERROR] Missing dependency: openai")
    print("        Please install it with: pip install openai")
    sys.exit(1)


BATCH_SIZE = 50
SLEEP_BETWEEN_CALLS = 0.3  # seconds


def load_env_from_dotenv(path: str) -> Dict[str, str]:
    env: Dict[str, str] = {}
    if not os.path.exists(path):
        return env
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip("'").strip('"')
            env[key] = value
    return env


def get_db_config() -> Dict[str, Any]:
    cwd = os.getcwd()
    dotenv_path = os.path.join(cwd, ".env")
    dotenv = load_env_from_dotenv(dotenv_path)

    def cfg(key: str, default: Optional[str] = None) -> Optional[str]:
        return os.getenv(key) or dotenv.get(key) or default

    host = cfg("DB_HOST", "mysql")
    port_str = cfg("DB_PORT", "3306")
    user = cfg("DB_USERNAME") or cfg("DB_USER") or "root"
    password = cfg("DB_PASSWORD") or cfg("DB_ROOT_PASSWORD") or ""
    database = cfg("DB_DATABASE") or cfg("DB_NAME") or "lookforbest"

    try:
        port = int(port_str) if port_str is not None else 3306
    except ValueError:
        port = 3306

    return {
        "host": host,
        "port": port,
        "user": user,
        "password": password,
        "database": database,
        "charset": "utf8mb4",
        "cursorclass": pymysql.cursors.DictCursor,
    }


def get_openai_client() -> OpenAI:
    api_key = os.getenv("OPENAI_API_KEY")
    if not api_key:
        print("[ERROR] OPENAI_API_KEY is not set in environment.")
        print("        Please export your API key before running this script.")
        sys.exit(1)
    return OpenAI(api_key=api_key)


def translate_subtitle(client: OpenAI, model: str, zh_subtitle: str) -> str:
    """Translate a single Chinese subtitle into a short English tagline."""
    system_msg = (
        "You are a professional marketing copywriter and translator. "
        "Translate the given Chinese one-line robot subtitle into a natural, concise English tagline. "
        "Keep it short (ideally 3-12 words), suitable for a product card. "
        "Return ONLY the English tagline as plain text, no quotes, no explanation."
    )

    user_msg = f"Chinese subtitle: {zh_subtitle}"

    resp = client.chat.completions.create(
        model=model,
        temperature=0.4,
        messages=[
            {"role": "system", "content": system_msg},
            {"role": "user", "content": user_msg},
        ],
    )

    content = resp.choices[0].message.content or ""

    # 兼容可能出现的 <think>...</think> 包裹，保留后半段
    end_tag = content.rfind("</think>")
    if end_tag != -1:
        content = content[end_tag + len("</think>") :]

    return content.strip().strip('"').strip("'")


def main() -> None:
    db_cfg = get_db_config()
    client = get_openai_client()
    model = os.getenv("OPENAI_MODEL", "gpt-4.1-mini")

    print("[INFO] Using DB config:", {k: db_cfg[k] for k in ("host", "port", "database", "user")})
    print("[INFO] Using model:", model)

    conn = pymysql.connect(**db_cfg)
    try:
        with conn.cursor() as cur:
            while True:
                cur.execute(
                    """
                    SELECT id, name, subtitle, IFNULL(subtitle_en, '') AS subtitle_en
                    FROM robots
                    WHERE subtitle IS NOT NULL AND subtitle <> ''
                      AND (subtitle_en IS NULL OR subtitle_en = '')
                    ORDER BY id
                    LIMIT %s
                    """,
                    (BATCH_SIZE,),
                )
                rows = cur.fetchall()
                if not rows:
                    print("[INFO] No more robots needing subtitle_en. Done.")
                    break

                print(f"[INFO] Processing batch of {len(rows)} subtitles...")

                for row in rows:
                    rid = row["id"]
                    name = row["name"]
                    zh_sub = row["subtitle"]

                    if not zh_sub:
                        print(f"[INFO] Robot id={rid} has empty subtitle, skipping.")
                        continue

                    print(f"[INFO] Translating subtitle for robot id={rid} name={name!r}: {zh_sub!r}")
                    try:
                        en = translate_subtitle(client, model, zh_sub)
                    except Exception as e:
                        print(f"[WARN] LLM error for robot id={rid}: {e}")
                        continue

                    if not en:
                        print(f"[WARN] Empty translation for robot id={rid}, skipping update.")
                        continue

                    cur.execute(
                        "UPDATE robots SET subtitle_en = %s WHERE id = %s AND (subtitle_en IS NULL OR subtitle_en = '')",
                        (en, rid),
                    )
                    conn.commit()
                    print(f"[INFO] Updated robot id={rid} with subtitle_en={en!r}")

                    time.sleep(SLEEP_BETWEEN_CALLS)

            # Final count
            cur.execute(
                "SELECT COUNT(*) AS remaining FROM robots WHERE subtitle IS NOT NULL AND subtitle <> '' AND (subtitle_en IS NULL OR subtitle_en = '')"
            )
            remaining = cur.fetchone()["remaining"]
            print(f"[INFO] Remaining robots without subtitle_en: {remaining}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
