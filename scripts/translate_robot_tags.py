#!/usr/bin/env python3
"""
Batch-translate robot tag names from Chinese to English and write them into
robot_tags.name_en.

Usage (on the server, from /deploy/lookforbest):

  1) Install dependencies (once):
       pip install --upgrade openai pymysql

  2) Export your OpenAI API key:
       export OPENAI_API_KEY="sk-..."

     Optionally choose model (default: gpt-4.1-mini):
       export OPENAI_MODEL="gpt-4.1-mini"

  3) Run the script:
       cd /deploy/lookforbest
       python3 scripts/translate_robot_tags.py

The script:
  - Reads DB config from .env (DB_HOST / DB_PORT / DB_USERNAME / DB_PASSWORD / DB_DATABASE),
    with sensible defaults.
  - Iterates robot_tags where name_en is NULL or empty.
  - For each tag, calls OpenAI to generate a concise English tag name.
  - Uses parameterized UPDATEs to set name_en, only when it is currently empty.

It is safe to re-run; tags with non-empty name_en are skipped.
"""

import json
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


def translate_tag_name(client: OpenAI, model: str, zh_name: str) -> str:
    """
    Translate a single Chinese tag name into a concise English tag.
    Returns a short phrase suitable for display and search (no quotes).
    """
    system_msg = (
        "You are a professional technical translator. "
        "Translate short Chinese robot tags into concise English tags. "
        "Return ONLY the English tag as plain text, no quotes, no explanation. "
        "Keep it as a short phrase (1-4 words) suitable for UI labels and search facets."
    )

    user_msg = f"Chinese tag: {zh_name}"

    resp = client.chat.completions.create(
        model=model,
        temperature=0.2,
        messages=[
            {"role": "system", "content": system_msg},
            {"role": "user", "content": user_msg},
        ],
    )

    content = resp.choices[0].message.content or ""

    # Some providers may wrap the actual answer inside `<think>...</think>`
    # reasoning blocks. In that case, keep only the portion after the closing tag.
    end_tag = content.rfind("</think>")
    if end_tag != -1:
        content = content[end_tag + len("</think>") :]

    return content.strip().strip('"').strip("'")


def main() -> None:
    db_cfg = get_db_config()
    client = get_openai_client()
    model = os.getenv("OPENAI_MODEL", "gpt-4.1-mini")

    print("[INFO] Using DB config:", {k: db_cfg[k] for k in ("host", "port", "database", "user")})
    print("[INFO] Using OpenAI model:", model)

    conn = pymysql.connect(**db_cfg)
    try:
        with conn.cursor() as cur:
            while True:
                cur.execute(
                    """
                    SELECT id, name, IFNULL(name_en, '') AS name_en
                    FROM robot_tags
                    WHERE name_en IS NULL OR name_en = ''
                    ORDER BY id
                    LIMIT %s
                    """,
                    (BATCH_SIZE,),
                )
                rows = cur.fetchall()
                if not rows:
                    print("[INFO] No more tags needing English names. Done.")
                    break

                print(f"[INFO] Processing batch of {len(rows)} tags...")

                for row in rows:
                    tid = row["id"]
                    zh_name = row["name"]

                    if not zh_name:
                        print(f"[INFO] Tag id={tid} has empty name, skipping.")
                        continue

                    print(f"[INFO] Translating tag id={tid} name={zh_name!r} ...")
                    try:
                        en = translate_tag_name(client, model, zh_name)
                    except Exception as e:
                        print(f"[WARN] OpenAI error for tag id={tid}: {e}")
                        continue

                    if not en:
                        print(f"[WARN] Empty translation for tag id={tid}, skipping update.")
                        continue

                    cur.execute(
                        "UPDATE robot_tags SET name_en = %s WHERE id = %s AND (name_en IS NULL OR name_en = '')",
                        (en, tid),
                    )
                    conn.commit()
                    print(f"[INFO] Updated tag id={tid} with name_en={en!r}")

                    time.sleep(SLEEP_BETWEEN_CALLS)

            # Final count
            cur.execute(
                "SELECT COUNT(*) AS remaining FROM robot_tags WHERE name_en IS NULL OR name_en = ''"
            )
            remaining = cur.fetchone()["remaining"]
            print(f"[INFO] Remaining tags without English name_en: {remaining}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
