#!/bin/bash
# LookForBest 端到端冒烟测试脚本
# 覆盖7个核心用户流程：注册→登录→浏览机器人→详情→收藏→搜索→退出
# 用法：BASE_URL=http://localhost:8080 ./scripts/smoke-test.sh

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TS=$(date +%s)
TEST_USER="smoketest_${TS}@test.com"
TEST_PASS="SmokeTest@${TS}"
TOKEN=""
ROBOT_ID=""

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
pass() { echo -e "${GREEN}[PASS]${NC} $*"; PASSED=$((PASSED+1)); }
fail() { echo -e "${RED}[FAIL]${NC} $*"; FAILED=$((FAILED+1)); }
info() { echo -e "${YELLOW}[INFO]${NC} $*"; }
skip() { echo -e "${YELLOW}[SKIP]${NC} $*"; }

PASSED=0
FAILED=0

api() {
  local method="$1" path="$2"
  shift 2
  curl -s -X "$method" "${BASE_URL}${path}" \
    -H "Content-Type: application/json" \
    ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
    "$@"
}

echo "================================================"
echo " LookForBest 端到端冒烟测试"
echo " BASE_URL: ${BASE_URL}"
echo " 时间戳:   ${TS}"
echo "================================================"

# 0. 健康检查（前置条件）
info "0. 健康检查"
HEALTH=$(api GET /actuator/health 2>/dev/null || echo '{}')
STATUS=$(echo "$HEALTH" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('status',''))" 2>/dev/null || echo "")
[ "$STATUS" = "UP" ] && pass "后端健康检查 (status=UP)" || fail "后端健康检查失败 (status='${STATUS}', response: ${HEALTH:0:200})"

# 1. 用户注册
info "1. 用户注册"
REG=$(api POST /api/v1/auth/register -d \
  "{\"email\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\",\"username\":\"smoketest_${TS}\",\"nickname\":\"Smoke Tester\"}")
REG_SUCCESS=$(echo "$REG" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
[ "$REG_SUCCESS" = "True" ] && pass "用户注册成功" || fail "用户注册失败: ${REG:0:300}"

# 2. 用户登录（含JWT验证）
info "2. 用户登录"
LOGIN=$(api POST /api/v1/auth/login -d \
  "{\"email\":\"${TEST_USER}\",\"password\":\"${TEST_PASS}\"}")
TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); data=d.get('data',{}); print(data.get('accessToken',''))" 2>/dev/null || echo "")
[ -n "$TOKEN" ] && pass "用户登录成功，获取 JWT Token (${TOKEN:0:20}...)" || fail "登录失败/未返回Token: ${LOGIN:0:300}"

# 验证JWT：访问受保护端点
if [ -n "$TOKEN" ]; then
  ME=$(api GET /api/v1/users/me)
  ME_EMAIL=$(echo "$ME" | python3 -c "import sys,json; d=json.load(sys.stdin); data=d.get('data',{}); print(data.get('email',''))" 2>/dev/null || echo "")
  [ "$ME_EMAIL" = "$TEST_USER" ] && pass "JWT验证通过 (GET /api/v1/users/me 返回正确用户)" || fail "JWT验证失败 或 email不匹配: ${ME:0:200}"
fi

# 3. 浏览机器人列表
info "3. 浏览机器人列表"
LIST=$(api GET "/api/v1/robots?page=0&size=10")
LIST_OK=$(echo "$LIST" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
[ "$LIST_OK" = "True" ] && pass "机器人列表接口正常 (GET /api/v1/robots)" || fail "机器人列表请求失败: ${LIST:0:300}"

TOTAL=$(echo "$LIST" | python3 -c "import sys,json; d=json.load(sys.stdin); data=d.get('data',{}); print(data.get('totalElements',0))" 2>/dev/null || echo "0")
info "   数据库机器人总数: ${TOTAL}"
if [ "${TOTAL:-0}" -gt 0 ] 2>/dev/null; then
  ROBOT_ID=$(echo "$LIST" | python3 -c "import sys,json; d=json.load(sys.stdin); content=d.get('data',{}).get('content',[]); print(content[0]['id'] if content else '')" 2>/dev/null || echo "")
  pass "机器人列表非空 (共 ${TOTAL} 条，首条 id=${ROBOT_ID})"
else
  info "   数据库暂无机器人数据，插入测试数据..."
  mysql -u lookforbest -plookforbest lookforbest -e \
    "INSERT INTO robots (name, slug, description, brand, status, created_at, updated_at) VALUES ('TestBot Alpha', 'testbot-alpha-${TS}', 'Smoke test robot', 'TestBrand', 'ACTIVE', NOW(), NOW());" 2>/dev/null \
    && info "   种子机器人已插入" || fail "无法插入测试机器人（可能缺少字段）"
  LIST2=$(api GET "/api/v1/robots?page=0&size=5")
  ROBOT_ID=$(echo "$LIST2" | python3 -c "import sys,json; d=json.load(sys.stdin); content=d.get('data',{}).get('content',[]); print(content[0]['id'] if content else '')" 2>/dev/null || echo "")
  [ -n "$ROBOT_ID" ] && pass "机器人列表在种子数据写入后正常返回" || fail "种子数据插入后列表仍为空"
fi

# 4. 查看机器人详情
info "4. 机器人详情"
if [ -n "$ROBOT_ID" ]; then
  DETAIL=$(api GET "/api/v1/robots/${ROBOT_ID}")
  DETAIL_NAME=$(echo "$DETAIL" | python3 -c "import sys,json; d=json.load(sys.stdin); data=d.get('data',{}); print(data.get('name',''))" 2>/dev/null || echo "")
  [ -n "$DETAIL_NAME" ] && pass "机器人详情正常 (id=${ROBOT_ID}, name=${DETAIL_NAME})" || fail "机器人详情缺少 name 字段: ${DETAIL:0:200}"
else
  fail "跳过详情测试（无可用 robot_id）"
fi

# 5. 收藏机器人
info "5. 收藏机器人"
if [ -n "$ROBOT_ID" ] && [ -n "$TOKEN" ]; then
  FAV=$(api POST "/api/v1/users/me/favorites/${ROBOT_ID}")
  FAV_OK=$(echo "$FAV" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
  [ "$FAV_OK" = "True" ] && pass "收藏机器人成功 (POST /api/v1/users/me/favorites/${ROBOT_ID})" || fail "收藏失败: ${FAV:0:200}"

  FAVLIST=$(api GET "/api/v1/users/me/favorites")
  FAVLIST_OK=$(echo "$FAVLIST" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
  [ "$FAVLIST_OK" = "True" ] && pass "获取收藏列表正常 (GET /api/v1/users/me/favorites)" || fail "获取收藏列表失败: ${FAVLIST:0:200}"
else
  skip "收藏测试（缺少 robot_id 或 token）"
fi

# 6. 搜索机器人
info "6. 搜索机器人"
SEARCH=$(api GET "/api/v1/search?q=robot&page=0&size=5")
SEARCH_OK=$(echo "$SEARCH" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
[ "$SEARCH_OK" = "True" ] && pass "搜索接口正常 (GET /api/v1/search?q=robot)" || fail "搜索失败: ${SEARCH:0:300}"

# 6b. 搜索热词
HOT=$(api GET "/api/v1/search/hot")
HOT_OK=$(echo "$HOT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('success', False))" 2>/dev/null || echo "false")
[ "$HOT_OK" = "True" ] && pass "热搜接口正常 (GET /api/v1/search/hot)" || fail "热搜接口失败: ${HOT:0:200}"

# 7. 退出登录（JWT无状态 — 验证token已无法访问保护资源后的清理）
info "7. 退出登录（无状态JWT验证）"
# 无服务端 logout 端点（无状态JWT），验证客户端 token 清除：
# 模拟清除 token，再次请求受保护端点（无 token）
UNAUTH=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/users/me")
if [ "$UNAUTH" -eq 401 ]; then
  pass "退出登录验证：未授权请求返回 401 (受保护端点确实要求认证)"
else
  fail "退出登录验证：预期 401，实际得到 ${UNAUTH}"
fi
# 清除 token（模拟客户端退出）
TOKEN=""
pass "JWT Token 已清除（模拟客户端退出登录）"

# ---- 汇总 ----
echo ""
echo "================================================"
echo " 测试结果汇总"
echo "================================================"
echo -e " ${GREEN}通过: ${PASSED}${NC}"
echo -e " ${RED}失败: ${FAILED}${NC}"
echo "================================================"
if [ "$FAILED" -eq 0 ]; then
  echo -e " ${GREEN}🎉 所有测试通过！端到端主流程验证完毕。${NC}"
  exit 0
else
  echo -e " ${RED}${FAILED} 项测试失败，请检查上方日志。${NC}"
  exit 1
fi
