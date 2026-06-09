#!/bin/bash

set -e

echo "=========================================="
echo "  数码配件经验交流社区 - 启动脚本"
echo "=========================================="
echo ""

# 加载 .env 文件
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
    echo "✓ 已加载环境变量配置"
else
    echo "✗ 未找到 .env 文件"
    exit 1
fi

# 检查端口是否被占用
echo ""
echo "🔍 检查端口占用情况..."
check_port() {
    local port=$1
    local name=$2
    if lsof -nP -iTCP:${port} -sTCP:LISTEN > /dev/null 2>&1; then
        local pid=$(lsof -nP -iTCP:${port} -sTCP:LISTEN -t | head -1)
        local process=$(ps -p ${pid} -o comm= 2>/dev/null || echo "unknown")
        echo "✗ 端口 ${port} (${name}) 被占用，PID: ${pid}, 进程: ${process}"
        return 1
    else
        echo "✓ 端口 ${port} (${name}) 可用"
        return 0
    fi
}

PORTS_OK=true
check_port ${FRONTEND_PORT} "前端" || PORTS_OK=false
check_port ${BACKEND_PORT} "后端" || PORTS_OK=false
check_port ${MYSQL_PORT} "MySQL" || PORTS_OK=false
check_port ${REDIS_PORT} "Redis" || PORTS_OK=false

if [ "$PORTS_OK" = false ]; then
    echo ""
    echo "❌ 端口检查失败，请先释放被占用的端口"
    exit 1
fi

# 开始构建和启动
echo ""
echo "🚀 开始构建并启动服务..."
echo ""

docker compose up --build -d

echo ""
echo "⏳ 等待服务完全启动..."
sleep 5

# 检查容器状态
echo ""
echo "📦 容器运行状态:"
docker compose ps

echo ""
echo "=========================================="
echo "  ✅ 项目启动成功！"
echo "=========================================="
echo ""
echo "🌐 前端访问地址:  http://localhost:${FRONTEND_PORT}"
echo "🔗 直连地址:      http://127.0.0.1:${FRONTEND_PORT}"
echo "🔌 后端API:       http://127.0.0.1:${BACKEND_PORT}/api"
echo "🗄️  MySQL:        127.0.0.1:${MYSQL_PORT}"
echo "⚡ Redis:        127.0.0.1:${REDIS_PORT}"
echo ""
echo "💡 测试账号:"
echo "   用户名: test001  密码: 123456"
echo "   用户名: test002  密码: 123456"
echo ""
echo "📝 常用命令:"
echo "   查看日志:  docker compose logs -f"
echo "   停止服务:  docker compose down"
echo "   重启服务:  docker compose restart"
echo ""
