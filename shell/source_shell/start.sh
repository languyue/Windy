#!/bin/bash

# 检查传入参数的数量
if [ "$#" -lt 3 ]; then
  echo "脚本执行错误,缺失必要参数,请使用下面格式执行"
  echo sh start.sh 数据库IP:数据库端口 数据库用户 数据库用户密码
  exit 1
fi

# 获取传入的参数
DB_HOST=$1
DB_USERNAME=$2
DB_PASSWORD=$3
MAVEN_PATH=${4:-'/opt/windy-client/maven'}
EUREKA_ZONE=${5:-'http://localhost:9888/eureka'}  # 默认 Eureka 地址
TOKEN_SECRET=${6:-$TOKEN_SECRET}  # 可选,第6个参数或同名环境变量,未提供则自动生成密钥
SCRIPT_DIR=$(cd -P "$(dirname "$0")" && pwd)

# 封装复制 maven 目录的逻辑
copy_maven_directory() {
  local SOURCE_MAVEN_DIR="$1"
  local TARGET_MAVEN_DIR="$2"

  if [ ! -d "$TARGET_MAVEN_DIR" ]; then
    mkdir -p "$TARGET_MAVEN_DIR"   
    # 检查目录是否成功创建
    if [ $? -ne 0 ]; then
      echo "创建目录 $TARGET_MAVEN_DIR 失败，请检查权限或路径问题。"
      return 1
    fi
    cp -r "$SOURCE_MAVEN_DIR" "$TARGET_MAVEN_DIR"
  fi
}

# 使用 $SCRIPT_DIR 来拼接完整路径
SOURCE_MAVEN_DIR="$SCRIPT_DIR/client/maven"
TARGET_MAVEN_DIR="/opt/windy-client/maven"

# 调用复制 maven 目录的函数
copy_maven_directory "$SOURCE_MAVEN_DIR" "$TARGET_MAVEN_DIR"

# 获取当前主机的 IP 地址（适配 macOS 和 Linux）
if [[ "$OSTYPE" == "darwin"* ]]; then
  # macOS: 使用 ifconfig 过滤出 IPv4 地址
  HOST_IP=$(ifconfig en0 | grep inet | awk '$1=="inet" {print $2}' | head -n 1)
else
  # Linux: 使用 hostname -I 获取 IPv4 地址
  HOST_IP=$(hostname -I | awk '{print $1}')
  # 确保排除 IPv6 地址
  HOST_IP=$(echo $HOST_IP | grep -oP '(\d+\.\d+\.\d+\.\d+)')
fi

# 检查端口是否被占用，并停止占用端口的进程
stop_existing_process_by_port() {
  local PORT=$1
  # 只匹配监听该端口的进程，避免误杀连接到该端口的其他服务（如 Eureka 客户端长连接）
  local PIDS=$(lsof -nP -t -iTCP:$PORT -sTCP:LISTEN)

  if [ -n "$PIDS" ]; then
    for PID in $PIDS; do
      kill -9 "$PID"
      if [ $? -eq 0 ]; then
        echo "检测到端口 $PORT 被占用, 已停止进程 PID: $PID"
      else
        echo "停止进程 PID: $PID 失败"
      fi
    done
  fi
}

# 启动 windy-starter
stop_existing_process_by_port 9768
echo "[1/3] 启动windy-console ..."
sh ./console/start-console.sh $DB_HOST $DB_USERNAME $DB_PASSWORD $EUREKA_ZONE $TOKEN_SECRET

# 启动 windy-master
stop_existing_process_by_port 9888
echo "[2/3] 启动 windy-master ..."
sh ./master/start-master.sh $DB_HOST $DB_USERNAME $DB_PASSWORD $EUREKA_ZONE

# 启动 windy-client
stop_existing_process_by_port 8070
echo "[3/3] 启动 windy-client ..."
sh ./client/start-client.sh $MAVEN_PATH $EUREKA_ZONE

# 等待服务启动的函数
wait_for_service() {
  local HOST=$1
  local PORT=$2
  local TIMEOUT=120
  local INTERVAL=2
  local ELAPSED_TIME=0

  echo "等待 Windy 服务启动..."

  while true; do
    # -f: HTTP 错误码视为失败; -m 3: 单次请求最多 3 秒
    RESPONSE=$(curl -sf -m 3 http://$HOST:$PORT/v1/devops/system/version)

    if [ $? -eq 0 ]; then
      # 用 sed 提取 data.consoleVersion，macOS/Linux 均自带，无需安装 jq
      CONSOLE_VERSION=$(printf '%s' "$RESPONSE" | sed -n 's/.*"consoleVersion"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
      if [ -n "$CONSOLE_VERSION" ]; then
        echo "=========================================="
        echo ""
        echo "Windy 服务启动成功，版本: $CONSOLE_VERSION"
        echo ""
        echo "=========================================="
        break
      fi
    fi

    if [ $ELAPSED_TIME -ge $TIMEOUT ]; then
      echo "在 $TIMEOUT 秒内未能探测成功，退出脚本"
      exit 1
    fi

    sleep $INTERVAL
    ELAPSED_TIME=$((ELAPSED_TIME + INTERVAL))
  done
}

# 等待 Windy 启动
wait_for_service $HOST_IP 9768

# 最后提示
echo "Windy 已安装完成，请访问: http://localhost:9768"
echo "或者访问当前主机的 IP 地址: http://$HOST_IP:9768"
