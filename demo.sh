#!/bin/bash

echo "=== Spring AI Tool Calling Demo ==="
echo "请确保以下服务正在运行："
echo "1. Ollama服务 (http://localhost:11434)"
echo "2. Spring Boot应用 (http://localhost:8080)"
echo ""

# 检查应用是否运行
echo "检查应用状态..."
if curl -s http://localhost:8080/api/v1/health/service > /dev/null; then
    echo "✅ Spring Boot应用正在运行"
else
    echo "❌ Spring Boot应用未运行，请先启动应用"
    exit 1
fi

echo ""
echo "=== 演示工具调用功能 ==="

echo ""
echo "1. 测试时间工具："
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "现在几点了？",
    "conversationId": "demo-time"
  }' | jq '.response'

echo ""
echo "2. 测试计算器工具："
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "计算 123 + 456 等于多少？",
    "conversationId": "demo-calc"
  }' | jq '.response'

echo ""
echo "3. 测试系统信息工具："
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "检查系统内存使用情况",
    "conversationId": "demo-system"
  }' | jq '.response'

echo ""
echo "4. 测试混合工具使用："
curl -X POST http://localhost:8080/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "请告诉我现在的时间，然后计算圆周率乘以100，最后检查系统健康状态",
    "conversationId": "demo-mixed"
  }' | jq '.response'

echo ""
echo "=== Demo completed ==="
echo "查看 TOOL_CALLING_GUIDE.md 获取更多使用说明"