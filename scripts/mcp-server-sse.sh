#!/bin/bash
# MCP Server SSE/HTTP Mode Startup Script
# 
# This script starts the Spring Agent application in MCP Server SSE mode.
# In this mode, the application provides an HTTP endpoint for Server-Sent Events,
# making it suitable for remote MCP client connections over HTTP.
#
# Usage:
#   ./scripts/mcp-server-sse.sh
#
# Prerequisites:
#   - Java 21 or higher
#   - Application JAR file built (run: ./gradlew bootJar)
#   - Ollama running on http://localhost:11434

set -e

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# JAR file path
JAR_FILE="${PROJECT_ROOT}/build/libs/spring-agent-0.0.1-SNAPSHOT.jar"

# Check if JAR exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please build the project first: ./gradlew bootJar"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "Error: Java 21 or higher is required (current: Java $JAVA_VERSION)"
    exit 1
fi

# Server port (can be overridden via environment variable)
SERVER_PORT=${SERVER_PORT:-8080}

echo "Starting MCP Server in SSE/HTTP mode..."
echo "JAR: $JAR_FILE"
echo "Port: $SERVER_PORT"
echo "MCP Endpoint: http://localhost:$SERVER_PORT/mcp/message"
echo ""
echo "Press Ctrl+C to stop"
echo ""

# Start the application in SSE mode
java -jar "$JAR_FILE" \
    --spring.profiles.active=default \
    --server.port=$SERVER_PORT \
    --spring.ai.mcp.server.enabled=true \
    --spring.ai.mcp.server.sse-message-endpoint=/mcp/message \
    --logging.level.org.springframework.ai.mcp=DEBUG

