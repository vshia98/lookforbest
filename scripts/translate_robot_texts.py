#!/usr/bin/env python3
"""
Batch-translate robot long-text fields (description / introduction / scenarios / advantages)
from Chinese to English and write them into *_en columns in the `robots` table.

Usage (on the server, from /deploy/lookforbest):

  1) Install dependencies (once):
       pip install --upgrade openai pymysql

  2) Export your OpenAI API key:
       export OPENAI_API_KEY="sk-..."

     Optionally choose model (default: gpt-4.1-mini):
       export OPENAI_MODEL="gpt-4.1-mini"

  3) Run the script:
       cd /deploy/lookforbest
       python3 scripts/translate_robot_texts.py

The script:
  - Reads DB config from .env (DB_HOST / DB_PORT / DB_USERNAME / DB_PASSWORD / DB_DATABASE),
    falling back to MySQL service defaults if missing.
  - Iterates robots where any of description_en / introduction_en / application_scenarios_en /
    advantages_en is NULL/empty.
  - For each robot, sends existing Chinese texts to OpenAI and receives English JSON.
  - Uses parameterized UPDATEs, only filling *_en fields that are currently empty.

It is safe to re-run; already-filled *_en fields are left untouched.
"""

import json
import os
import sys
import time
from typing import Any, Dict, Optional, Tuple

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


BATCH_SIZE = 10  # robots per loop; adjust if needed
SLEEP_BETWEEN_CALLS = 1.0  # seconds, to avoid hitting rate limits too hard


def load_env_from_dotenv(path: str) -> Dict[str, str]:
    """Minimal .env loader (no external dependency)."""
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
            # strip surrounding quotes if any
            value = value.strip().strip("'").strip('"')
            env[key] = value
    return env


def get_db_config() -> Dict[str, Any]:
    """Resolve DB config from environment and .env file."""
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


def translate_robot_texts(
    client: OpenAI,
    model: str,
    zh_description: str,
    zh_introduction: str,
    zh_scenarios: str,
    zh_advantages: str,
) -> Tuple[Optional[str], Optional[str], Optional[str], Optional[str]]:
    """
    Call OpenAI to translate the four Chinese text fields into English.
    Returns a tuple of (description_en, introduction_en, scenarios_en, advantages_en).
    If a Chinese field is empty, the corresponding English may be empty as well.
    """

    # Build a compact JSON-friendly payload. We explicitly ask for JSON output.
    payload = {
        "description": zh_description or "",
        "introduction": zh_introduction or "",
        "application_scenarios": zh_scenarios or "",
        "advantages": zh_advantages or "",
    }

    system_msg = (
        "You are a professional technical translator. "
        "Translate the given Chinese robot description fields into natural, concise English. "
        "Preserve all factual details. "
        "Return a JSON object with the following string fields: "
        "description_en, introduction_en, application_scenarios_en, advantages_en. "
        "If a source field is empty, use an empty string for the corresponding English field. "
        "Do not include any extra keys or commentary."
    )

    user_msg = (
        "Here is a JSON object with Chinese text for a robot:\n\n"
        f"{json.dumps(payload, ensure_ascii=False)}\n\n"
        "Translate each field into English and return only the JSON object with *_en fields."
    )

    resp = client.chat.completions.create(
        model=model,
        temperature=0.2,
        messages=[
            {"role": "system", "content": system_msg},
            {"role": "user", "content": user_msg},
        ],
        response_format={"type": "json_object"},
    )

    content = resp.choices[0].message.content or ""

    # Some providers (or proxy endpoints) may prepend internal reasoning blocks
    # like "<think>...</think>" before the actual JSON. That breaks json.loads()
    # because the string no longer starts with "{". To be robust, locate the
    # first "{" and parse from there.
    json_start = content.find("{")
    if json_start != -1:
        json_str = content[json_start:]
    else:
        json_str = content

    try:
        data = json.loads(json_str)
    except Exception as e:
        print("[WARN] Failed to parse JSON from OpenAI response, returning empty strings.")
        print("       Error:", e)
        print("       Raw content:", content[:500])
        return "", "", "", ""

    def norm(key: str) -> str:
        v = data.get(key, "")
        if v is None:
            return ""
        # strip dangerous control chars, keep it simple
        return str(v).strip()

    return (
        norm("description_en"),
        norm("introduction_en"),
        norm("application_scenarios_en"),
        norm("advantages_en"),
    )


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
                # Fetch a batch of robots that are missing any English content fields
                cur.execute(
                    """
                    SELECT id, name,
                           IFNULL(description, '') AS description,
                           IFNULL(introduction, '') AS introduction,
                           IFNULL(application_scenarios, '') AS application_scenarios,
                           IFNULL(advantages, '') AS advantages,
                           IFNULL(description_en, '') AS description_en,
                           IFNULL(introduction_en, '') AS introduction_en,
                           IFNULL(application_scenarios_en, '') AS application_scenarios_en,
                           IFNULL(advantages_en, '') AS advantages_en
                    FROM robots
                    WHERE (description_en IS NULL OR description_en = '')
                       OR (introduction_en IS NULL OR introduction_en = '')
                       OR (application_scenarios_en IS NULL OR application_scenarios_en = '')
                       OR (advantages_en IS NULL OR advantages_en = '')
                    ORDER BY id
                    LIMIT %s
                    """,
                    (BATCH_SIZE,),
                )
                rows = cur.fetchall()
                if not rows:
                    print("[INFO] No more robots needing English content. Done.")
                    break

                print(f"[INFO] Processing batch of {len(rows)} robots...")

                for row in rows:
                    rid = row["id"]
                    name = row["name"]

                    zh_desc = row["description"]
                    zh_intro = row["introduction"]
                    zh_scen = row["application_scenarios"]
                    zh_adv = row["advantages"]

                    # Skip robots that have no Chinese text at all
                    if not (zh_desc or zh_intro or zh_scen or zh_adv):
                        print(f"[INFO] Robot id={rid} name={name!r} has no Chinese content, skipping.")
                        continue

                    print(f"[INFO] Translating robot id={rid} name={name!r} ...")

                    desc_en, intro_en, scen_en, adv_en = translate_robot_texts(
                        client, model, zh_desc, zh_intro, zh_scen, zh_adv
                    )

                    # Only update fields that are currently empty in DB
                    updates = []
                    params: list = []

                    if row["description_en"] == "" and desc_en:
                        updates.append("description_en = %s")
                        params.append(desc_en)
                    if row["introduction_en"] == "" and intro_en:
                        updates.append("introduction_en = %s")
                        params.append(intro_en)
                    if row["application_scenarios_en"] == "" and scen_en:
                        updates.append("application_scenarios_en = %s")
                        params.append(scen_en)
                    if row["advantages_en"] == "" and adv_en:
                        updates.append("advantages_en = %s")
                        params.append(adv_en)

                    if updates:
                        params.append(rid)
                        sql = f"UPDATE robots SET {', '.join(updates)} WHERE id = %s"
                        cur.execute(sql, tuple(params))
                        conn.commit()
                        print(f"[INFO] Updated robot id={rid}")
                    else:
                        print(f"[INFO] Robot id={rid} already has English content, skipped update.")

                    time.sleep(SLEEP_BETWEEN_CALLS)

            # Final count for sanity
            cur.execute(
                """
                SELECT COUNT(*) AS remaining
                FROM robots
                WHERE (description_en IS NULL OR description_en = '')
                   OR (introduction_en IS NULL OR introduction_en = '')
                   OR (application_scenarios_en IS NULL OR application_scenarios_en = '')
                   OR (advantages_en IS NULL OR advantages_en = '')
                """
            )
            remaining = cur.fetchone()["remaining"]
            print(f"[INFO] Remaining robots with missing English content fields: {remaining}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
