#!/bin/bash
# MCP Server Stdio Mode Startup Script
# 
# This script starts the Spring Agent application in MCP Server Stdio mode.
# In this mode, the application communicates via standard input/output,
# making it suitable for integration with AI IDEs like Claude Desktop or Cursor.
#
# Usage:
#   ./scripts/mcp-server-stdio.sh
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

echo "Starting MCP Server in Stdio mode..."
echo "JAR: $JAR_FILE"
echo "Profile: mcp-stdio"
echo ""
echo "The server will communicate via stdin/stdout"
echo "Press Ctrl+C to stop"
echo ""

# Start the application in stdio mode
exec java -jar "$JAR_FILE" \
    --spring.profiles.active=mcp-stdio \
    --logging.level.root=ERROR \
    --logging.level.dev.jackelyj.spring_agent=INFO \
    --logging.level.org.springframework.ai.mcp=DEBUG

