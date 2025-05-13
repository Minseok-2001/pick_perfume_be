#!/bin/bash
set -e

# 환경 변수 설정
export TZ=Asia/Seoul

# Elasticsearch 연결 확인
if [ -n "$ELASTICSEARCH_HOST" ]; then
  echo "Waiting for Elasticsearch to be available..."
  
  # Elasticsearch가 준비될 때까지 대기
  until curl -s -f "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT" > /dev/null; do
    echo "Elasticsearch is unavailable - sleeping"
    sleep 5
  done
  
  echo "Elasticsearch is up - executing setup"
  
  # Elasticsearch 인덱스 생성 및 매핑 설정
  curl -X PUT "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/perfumes" -H "Content-Type: application/json" -d @settings.json
  curl -X PUT "$ELASTICSEARCH_HOST:$ELASTICSEARCH_PORT/perfumes/_mapping" -H "Content-Type: application/json" -d @mapping.json
fi

# Spring Boot 애플리케이션 실행
exec java ${JAVA_OPTS:--Xms512m -Xmx1024m} -jar app.jar 