#!/usr/bin/env bash
set -e

COMMAND=${1:-up}

KAFKA_COMPOSE="docker/sandbox/kafka/docker-compose.yml"
OBS_COMPOSE="docker/sandbox/observability/docker-compose.yml"
REDIS_COMPOSE="docker/sandbox/redis/docker-compose.yml"
APP_COMPOSE="docker-compose-dev.yml"

up() {
  echo "🚀 Starting Kafka..."
  docker compose -f $KAFKA_COMPOSE up -d

  echo "📊 Starting Observability stack..."
  docker compose -f $OBS_COMPOSE up -d

  echo "🧠 Starting Redis..."
  docker compose -f $REDIS_COMPOSE up -d

  echo "⏳ Waiting for services..."
  sleep 5

  echo "🧩 Starting application..."
  docker compose -f $APP_COMPOSE up -d
}

down() {
  echo "🧩 Stopping application..."
  docker compose -f $APP_COMPOSE down

  echo "🧠 Stopping Redis..."
  docker compose -f $REDIS_COMPOSE down

  echo "📊 Stopping Observability stack..."
  docker compose -f $OBS_COMPOSE down

  echo "🚀 Stopping Kafka..."
  docker compose -f $KAFKA_COMPOSE down
}

restart() {
  down
  up
}

clean() {
  echo "🧹 Cleaning all sandbox containers and volumes..."

  docker compose -f $APP_COMPOSE down -v
  docker compose -f $REDIS_COMPOSE down -v
  docker compose -f $OBS_COMPOSE down -v
  docker compose -f $KAFKA_COMPOSE down -v
}

case "$COMMAND" in
  up)
    up
    ;;
  down)
    down
    ;;
  restart)
    restart
    ;;
  clean)
    clean
    ;;
  *)
    echo "❌ Unknown command: $COMMAND"
    echo "Usage: ./sandbox.sh [up|down|restart|clean]"
    exit 1
    ;;
esac