#!/bin/bash
# LookForBest 全流程冒烟测试脚本
# 覆盖：注册→登录→浏览→搜索→对比→评论
# 用法：BASE_URL=https://yourdomain.com ./scripts/smoke-test.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
TS=$(date +%s)
TEST_USER="smoketest_${TS}@test.com"
TEST_PASS="SmokeTest@${TS}"
TOKEN=""
ROBOT_ID=""

# ---- 颜色输出 ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
pass() { echo -e "${GREEN}[PASS]${NC} $*"; }
fail() { echo -e "${RED}[FAIL]${NC} $*"; FAILED=$((FAILED+1)); }
info() { echo -e "${YELLOW}[INFO]${NC} $*"; }

FAILED=0

# ---- 辅助函数 ----
api() {
  local method="$1" path="$2"
  shift 2
  curl -sk -X "$method" "${BASE_URL}${path}" \
    -H "Content-Type: application/json" \
    ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
    "$@"
}

check_field() {
  local label="$1" json="$2" field="$3"
  val=$(echo "$json" | grep -o "\"${field}\":[^,}]*" | head -1 | cut -d: -f2 | tr -d ' "')
  [ -n "$val" ] && pass "$label (${field}=${val})" || fail "$label — 字段 ${field} 缺失"
  echo "$val"
}

echo "================================================"
echo " LookForBest 冒烟测试"
echo " BASE_URL: ${BASE_URL}"
echo " 时间戳:   ${TS}"
echo "================================================"

# 1. 健康检查
info "1. 健康检查"
HEALTH=$(api GET /actuator/health 2>/dev/null || echo '{}')
STATUS=$(echo "$HEALTH" | grep -o '"status":"[^"]*"' | head -1 | cut -d'"' -f4)
[ "$STATUS" = "UP" ] && pass "后端健康检查" || fail "后端健康检查 (status=${STATUS})"

# 2. 注册
info "2. 用户注册"
REG=$(api POST /api/v1/auth/register -d "{\"email\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\",\"username\":\"smoketest_${TS}\"}")
SUCCESS=$(echo "$REG" | grep -o '"success":[^,}]*' | head -1 | cut -d: -f2 | tr -d ' ')
[ "$SUCCESS" = "true" ] && pass "注册成功" || fail "注册失败: ${REG}"

# 3. 登录
info "3. 用户登录"
LOGIN=$(api POST /api/v1/auth/login -d "{\"email\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\"}")
TOKEN=$(echo "$LOGIN" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4)
[ -n "$TOKEN" ] && pass "登录成功，获取 Token" || fail "登录失败: ${LOGIN}"

# 4. 浏览机器人列表
info "4. 浏览机器人列表"
LIST=$(api GET "/api/v1/robots?page=0&size=5")
TOTAL=$(echo "$LIST" | grep -o '"totalElements":[0-9]*' | head -1 | cut -d: -f2)
if [ "${TOTAL:-0}" -gt 0 ] 2>/dev/null; then
  pass "机器人列表（共 ${TOTAL} 条）"
  ROBOT_ID=$(echo "$LIST" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
else
  fail "机器人列表为空或请求失败: ${LIST}"
fi

# 5. 搜索
info "5. 搜索功能"
SEARCH=$(api GET "/api/v1/robots?q=robot&page=0&size=3")
SEARCH_TOTAL=$(echo "$SEARCH" | grep -o '"totalElements":[0-9]*' | head -1 | cut -d: -f2)
[ "${SEARCH_TOTAL:-0}" -ge 0 ] 2>/dev/null && pass "搜索功能正常（结果数：${SEARCH_TOTAL}）" || fail "搜索失败: ${SEARCH}"

# 6. 机器人详情
info "6. 机器人详情"
if [ -n "${ROBOT_ID}" ]; then
  DETAIL=$(api GET "/api/v1/robots/${ROBOT_ID}")
  NAME=$(echo "$DETAIL" | grep -o '"name":"[^"]*"' | head -1 | cut -d'"' -f4)
  [ -n "$NAME" ] && pass "机器人详情（id=${ROBOT_ID}, name=${NAME}）" || fail "机器人详情缺少 name 字段"
else
  fail "跳过详情测试（无可用 robot_id）"
fi

# 7. 对比功能
info "7. 对比功能"
if [ -n "${ROBOT_ID}" ]; then
  COMPARE=$(api GET "/api/v1/robots/compare?ids=${ROBOT_ID}")
  COMPARE_OK=$(echo "$COMPARE" | grep -o '"success":[^,}]*' | head -1 | cut -d: -f2 | tr -d ' ')
  [ "$COMPARE_OK" = "true" ] && pass "对比功能正常" || fail "对比请求失败: ${COMPARE}"
else
  fail "跳过对比测试（无可用 robot_id）"
fi

# 8. 发布评论
info "8. 发布评论"
if [ -n "${ROBOT_ID}" ] && [ -n "$TOKEN" ]; then
  COMMENT=$(api POST "/api/v1/robots/${ROBOT_ID}/reviews" \
    -d "{\"rating\":5,\"content\":\"自动化冒烟测试评论 ${TS}\"}")
  COMMENT_OK=$(echo "$COMMENT" | grep -o '"success":[^,}]*' | head -1 | cut -d: -f2 | tr -d ' ')
  [ "$COMMENT_OK" = "true" ] && pass "评论发布成功" || fail "评论发布失败: ${COMMENT}"
else
  fail "跳过评论测试（缺少 robot_id 或 token）"
fi

# 9. 推荐接口
info "9. AI 推荐接口"
if [ -n "${ROBOT_ID}" ]; then
  REC=$(api GET "/api/v1/robots/${ROBOT_ID}/recommendations")
  REC_OK=$(echo "$REC" | grep -o '"success":[^,}]*' | head -1 | cut -d: -f2 | tr -d ' ')
  [ "$REC_OK" = "true" ] && pass "推荐接口正常" || fail "推荐接口失败: ${REC}"
fi

# ---- 结果汇总 ----
echo "================================================"
if [ "$FAILED" -eq 0 ]; then
  echo -e "${GREEN} 全部测试通过！${NC}"
else
  echo -e "${RED} ${FAILED} 项测试失败，请检查上方日志${NC}"
  exit 1
fi
echo "================================================"
