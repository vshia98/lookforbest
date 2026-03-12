#!/usr/bin/env bash
# MySQL 自动备份脚本
# 用法：./backup-db.sh [保留天数，默认 7]
# 建议通过 cron 每日执行，例如：
#   0 2 * * * /opt/lookforbest/scripts/backup-db.sh >> /var/log/lookforbest/backup.log 2>&1

set -euo pipefail

# ==========================
# 配置（优先读取环境变量）
# ==========================
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-lookforbest}"
DB_USER="${DB_USERNAME:-lookforbest}"
DB_PASS="${DB_PASSWORD:-lookforbest}"
BACKUP_DIR="${BACKUP_DIR:-/var/backups/lookforbest/mysql}"
RETAIN_DAYS="${1:-7}"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql.gz"

# ==========================
# 初始化备份目录
# ==========================
mkdir -p "$BACKUP_DIR"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] 开始备份数据库 ${DB_NAME} ..."

# ==========================
# 执行 mysqldump + 压缩
# ==========================
mysqldump \
    --host="$DB_HOST" \
    --port="$DB_PORT" \
    --user="$DB_USER" \
    --password="$DB_PASS" \
    --single-transaction \
    --routines \
    --triggers \
    --events \
    --set-gtid-purged=OFF \
    "$DB_NAME" | gzip > "$BACKUP_FILE"

SIZE=$(du -sh "$BACKUP_FILE" | cut -f1)
echo "[$(date '+%Y-%m-%d %H:%M:%S')] 备份完成：${BACKUP_FILE} (${SIZE})"

# ==========================
# 清理过期备份
# ==========================
DELETED=$(find "$BACKUP_DIR" -name "${DB_NAME}_*.sql.gz" -mtime +"$RETAIN_DAYS" -print -delete | wc -l)
echo "[$(date '+%Y-%m-%d %H:%M:%S')] 已清理 ${DELETED} 个超过 ${RETAIN_DAYS} 天的旧备份"

# ==========================
# 可选：上传到对象存储（MinIO/S3）
# ==========================
if command -v mc &>/dev/null && [ -n "${MINIO_ENDPOINT:-}" ]; then
    BUCKET="lookforbest-backups"
    mc cp "$BACKUP_FILE" "myminio/${BUCKET}/mysql/$(basename "$BACKUP_FILE")" && \
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] 已上传备份到 MinIO: ${BUCKET}/mysql/"
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] 备份流程完成"
