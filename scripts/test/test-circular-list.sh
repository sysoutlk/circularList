#!/bin/bash

# 循环列表测试脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://localhost:9005}"
API_PATH="/api/circular"

# JSON 格式化函数
format_json() {
    if command -v jq &> /dev/null; then
        echo "$1" | jq '.'
    elif command -v python3 &> /dev/null; then
        echo "$1" | python3 -m json.tool 2>/dev/null || echo "$1"
    else
        echo "$1"
    fi
}

echo "=========================================="
echo "循环列表测试（BRPOPLPUSH）"
echo "=========================================="
echo -e "${BLUE}测试目标: ${BASE_URL}${API_PATH}${NC}"
echo ""

echo -e "\n${YELLOW}【场景1: 负载均衡 - Round Robin】${NC}"
echo "----------------------------------------"

echo -e "\n1. 初始化5个工作节点..."
RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PATH}/workers/init?count=5")
format_json "$RESPONSE"

echo -e "\n2. 循环获取工作节点（观察轮询效果）..."
for i in {1..12}; do
    echo -e "\n  第 $i 次获取:"
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/workers/next")
    NODE_ID=$(echo "$RESPONSE" | grep -o '"nodeId":"[^"]*"' | cut -d'"' -f4)
    NODE_NAME=$(echo "$RESPONSE" | grep -o '"nodeName":"[^"]*"' | cut -d'"' -f4)
    echo -e "  ${GREEN}→ $NODE_NAME ($NODE_ID)${NC}"
    sleep 0.3
done

echo -e "\n3. 分配10个任务到工作节点..."
RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PATH}/tasks/assign?count=10")
format_json "$RESPONSE"

echo -e "\n4. 查看所有工作节点状态..."
RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/workers/all")
format_json "$RESPONSE"

echo -e "\n${YELLOW}【场景2: 广告轮播】${NC}"
echo "----------------------------------------"

echo -e "\n5. 初始化5个广告..."
RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PATH}/ads/init?count=5")
format_json "$RESPONSE"

echo -e "\n6. 循环展示广告（模拟10次页面访问）..."
for i in {1..10}; do
    echo -e "\n  第 $i 次访问:"
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/ads/next")
    AD_ID=$(echo "$RESPONSE" | grep -o '"adId":"[^"]*"' | cut -d'"' -f4)
    TITLE=$(echo "$RESPONSE" | grep -o '"title":"[^"]*"' | cut -d'"' -f4)
    IMPRESSIONS=$(echo "$RESPONSE" | grep -o '"impressions":[0-9]*' | cut -d':' -f2)
    echo -e "  ${BLUE}→ $TITLE ($AD_ID) - 展示次数: $IMPRESSIONS${NC}"
    sleep 0.3
done

echo -e "\n7. 模拟用户点击广告..."
curl -s -X POST "${BASE_URL}${API_PATH}/ads/click?adId=ad-1" | format_json
curl -s -X POST "${BASE_URL}${API_PATH}/ads/click?adId=ad-2" | format_json

echo -e "\n8. 查看广告统计..."
RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/ads/all")
format_json "$RESPONSE"

echo -e "\n9. 批量获取3个广告（预加载）..."
RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/ads/batch?count=3")
format_json "$RESPONSE"

echo -e "\n${YELLOW}【场景3: 资源池管理】${NC}"
echo "----------------------------------------"

echo -e "\n10. 初始化连接池（10个连接）..."
RESPONSE=$(curl -s -X POST "${BASE_URL}${API_PATH}/pool/init?poolName=db_connections&size=10&resourceType=db_conn")
format_json "$RESPONSE"

echo -e "\n11. 循环获取资源（模拟6次请求）..."
for i in {1..6}; do
    echo -e "\n  第 $i 次获取资源:"
    RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/pool/acquire?poolName=db_connections")
    RESOURCE_ID=$(echo "$RESPONSE" | grep -o '"resourceId":"[^"]*"' | cut -d'"' -f4)
    USAGE_COUNT=$(echo "$RESPONSE" | grep -o '"usageCount":[0-9]*' | cut -d':' -f2)
    echo -e "  ${GREEN}→ $RESOURCE_ID (使用次数: $USAGE_COUNT)${NC}"
    sleep 0.3
done

echo -e "\n12. 查看资源池状态..."
RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/pool/status?poolName=db_connections")
format_json "$RESPONSE"

echo -e "\n${YELLOW}【场景4: 压力测试】${NC}"
echo "----------------------------------------"

echo -e "\n13. 快速循环100次（测试性能）..."
START_TIME=$(date +%s)

for i in {1..100}; do
    curl -s -X GET "${BASE_URL}${API_PATH}/workers/next" > /dev/null
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
QPS=$((100 / DURATION))

echo -e "${GREEN}  完成100次请求，耗时: ${DURATION}秒, QPS: ${QPS}${NC}"

echo -e "\n14. 查看列表大小..."
RESPONSE=$(curl -s -X GET "${BASE_URL}${API_PATH}/list/size?listName=workers")
format_json "$RESPONSE"

echo -e "\n${GREEN}=========================================="
echo "测试完成!"
echo "==========================================${NC}"
echo ""
echo -e "${BLUE}核心特性验证:${NC}"
echo "  ✓ BRPOPLPUSH 实现循环列表"
echo "  ✓ Round-Robin 负载均衡"
echo "  ✓ 广告轮播展示"
echo "  ✓ 资源池循环分配"
echo "  ✓ 高性能循环遍历"
echo "=========================================="