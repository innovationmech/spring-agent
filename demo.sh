#!/bin/bash

echo "=== Spring AI Tool Calling & Vector Store Demo ==="
echo "请确保以下服务正在运行："
echo "1. Ollama服务 (http://localhost:11434)"
echo "2. Spring Boot应用 (http://localhost:8080)"
echo "3. (可选) PostgreSQL + pgvector (http://localhost:5432)"
echo ""

# 检查应用是否运行
echo "检查应用状态..."
if curl -s http://localhost:8080/api/v1/health/service > /dev/null; then
    echo "✅ Spring Boot应用正在运行"
else
    echo "❌ Spring Boot应用未运行，请先启动应用"
    exit 1
fi

# 检查服务健康状态
echo ""
echo "检查服务健康状态..."
curl -s http://localhost:8080/api/v1/health/service | jq '.'

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
echo "=== 演示对话记忆功能 ==="

echo ""
echo "5. 获取所有会话列表："
curl -s http://localhost:8080/api/v1/conversations | jq '.'

echo ""
echo "6. 获取特定会话消息数量："
curl -s http://localhost:8080/api/v1/conversations/demo-time/count | jq '.'

echo ""
echo "=== 演示文档向量存储功能 (需要PostgreSQL + pgvector) ==="

echo ""
echo "7. 上传文档到向量存储："
curl -X POST http://localhost:8080/api/v1/documents \
  -H "Content-Type: application/json" \
  -d '{
    "texts": [
      "Spring AI 是一个用于构建 AI 应用的 Spring 框架扩展。",
      "PostgreSQL 是一个强大的开源关系型数据库。",
      "pgvector 是 PostgreSQL 的向量相似度搜索扩展。",
      "Docker 可以轻松部署 PostgreSQL 和 pgvector。"
    ],
    "metadata": {
      "source": "demo",
      "category": "technology"
    }
  }' | jq '.'

echo ""
echo "8. 搜索相似文档："
curl -X POST http://localhost:8080/api/v1/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "数据库",
    "topK": 3,
    "threshold": 0.0
  }' | jq '.'

echo ""
echo "=== Demo completed ==="
echo ""
echo "提示："
echo "- 查看 CLAUDE.md 了解完整的使用说明"
echo "- 使用 docker-compose up -d 启动 PostgreSQL + pgvector"
echo "- 切换到 postgres profile: ./gradlew bootRun --args='--spring.profiles.active=postgres'"
echo "- 查看 request.http 获取更多 API 示例"