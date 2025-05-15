#!/bin/bash
set -e

# Elasticsearch 호스트 설정 (기본값: localhost)
ES_HOST=${ELASTICSEARCH_HOST:-https://search.scentist.link}
USER=${ELASTICSEARCH_USERNAME:-scentist}
PASSWORD=${ELASTICSEARCH_PASSWORD:-yHjgX%4N3sK0%l}

echo "Elasticsearch 호스트: $ES_HOST"

# Elasticsearch가 준비될 때까지 대기
echo "Elasticsearch 연결 확인 중..."
until curl -s -f -u $USER:$PASSWORD "$ES_HOST" > /dev/null; do
  echo "Elasticsearch를 사용할 수 없습니다. 5초 후 다시 시도합니다."
  sleep 5
done

echo "Elasticsearch에 연결되었습니다."

# 인덱스가 이미 존재하는지 확인
INDEX_EXISTS=$(curl -s -o /dev/null -w "%{http_code}" -u $USER:$PASSWORD "$ES_HOST/perfumes")

if [ "$INDEX_EXISTS" = "200" ]; then
  echo "perfumes 인덱스가 이미 존재합니다. 삭제 후 재생성하시겠습니까? (y/n)"
  read -r RECREATE
  
  if [ "$RECREATE" = "y" ]; then
    echo "기존 인덱스 삭제 중..."
    curl -X DELETE -u $USER:$PASSWORD "$ES_HOST/perfumes"
    echo "기존 인덱스가 삭제되었습니다."
  else
    echo "기존 인덱스를 유지합니다. 설정 작업을 종료합니다."
    exit 0
  fi
fi

# 인덱스 생성 및 설정
echo "인덱스 생성 및 설정 중..."
curl -X PUT -u $USER:$PASSWORD "$ES_HOST/perfumes" -H "Content-Type: application/json" -d @settings.json
echo "인덱스 설정이 완료되었습니다."

# 매핑 설정
echo "매핑 설정 중..."
curl -X PUT -u $USER:$PASSWORD "$ES_HOST/perfumes/_mapping" -H "Content-Type: application/json" -d @mapping.json
echo "매핑 설정이 완료되었습니다."

echo "Elasticsearch 설정이 모두 완료되었습니다."