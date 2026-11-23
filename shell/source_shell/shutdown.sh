#!/bin/bash

# 默认关闭所有服务
SHUTDOWN_CONSOLE=false
SHUTDOWN_MASTER=false
SHUTDOWN_CLIENT=false

# 检查传入参数
if [ "$#" -gt 0 ]; then
  for ARG in "$@"; do
    case $ARG in
      "console")
        SHUTDOWN_CONSOLE=true
        ;;
      "master")
        SHUTDOWN_MASTER=true
        ;;
      "client")
        SHUTDOWN_CLIENT=true
        ;;
      *)
        echo "无效参数: $ARG"
        echo "使用: console, master, client 或不传参数来关闭所有服务"
        exit 1
        ;;
    esac
  done
else
  # 默认不传参时，关闭所有服务
  SHUTDOWN_CONSOLE=true
  SHUTDOWN_MASTER=true
  SHUTDOWN_CLIENT=true
fi

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

# 检查是否获取到有效的 IP 地址
if [ -z "$HOST_IP" ]; then
  echo "无法获取主机的 IP 地址，请检查网络配置"
  exit 1
fi

# 查找并停止服务的通用函数
stop_service_by_port() {
  local PORT=$1
  local SERVICE_NAME=$2
  local PIDS

  # 只匹配监听该端口的进程，避免误杀连接到该端口的其他服务（如 Eureka 客户端长连接）
  PIDS=$(lsof -nP -t -iTCP:$PORT -sTCP:LISTEN)

  # 如果找到了 PID，则停止对应进程
  if [ -n "$PIDS" ]; then
    for PID in $PIDS; do
      kill -9 "$PID"
      if [ $? -eq 0 ]; then
        echo "$SERVICE_NAME 服务已停止，PID: $PID"
      else
        echo "停止 $SERVICE_NAME 服务失败，PID: $PID"
      fi
    done
  else
    echo "$SERVICE_NAME 服务未在运行，无需停止"
  fi
}

# 如果需要关闭 console 服务
if [ "$SHUTDOWN_CONSOLE" = true ]; then
  stop_service_by_port 9768 "console"
fi

# 如果需要关闭 master 服务
if [ "$SHUTDOWN_MASTER" = true ]; then
  stop_service_by_port 9888 "master"
fi

# 如果需要关闭 client 服务
if [ "$SHUTDOWN_CLIENT" = true ]; then
  stop_service_by_port 8070 "client"
fi
