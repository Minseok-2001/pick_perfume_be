# Pick Perfume Backend - Elasticsearch 설정 가이드

## Elasticsearch 수동 설정 방법

### 1. Elasticsearch 실행

```bash
docker-compose up -d elasticsearch
```

### 2. Nori 형태소 분석기 설치

Elasticsearch가 실행된 후 Nori 형태소 분석기를 설치합니다.

```bash
# 현재 설치된 플러그인 확인
curl -X GET "localhost:9200/_cat/plugins?v"

# Nori 플러그인 설치 (Elasticsearch 컨테이너 내부에서 실행)
docker exec -it pick-perfume-elasticsearch elasticsearch-plugin install analysis-nori

# 설치 후 Elasticsearch 재시작
docker restart pick-perfume-elasticsearch
```

### 3. 인덱스 생성 및 설정

#### 3.1 인덱스 생성

```bash
curl -X PUT "localhost:9200/perfumes" -H "Content-Type: application/json"
```

#### 3.2 인덱스 설정 (분석기 설정)

```bash
curl -X PUT "localhost:9200/perfumes/_settings" -H "Content-Type: application/json" -d'
{
  "index": {
    "max_ngram_diff": 10,
    "analysis": {
      "analyzer": {
        "korean": {
          "type": "custom",
          "tokenizer": "nori_tokenizer",
          "filter": ["nori_part_of_speech", "lowercase", "synonym"]
        },
        "ngram_analyzer": {
          "type": "custom",
          "tokenizer": "ngram_tokenizer",
          "filter": ["lowercase"]
        },
        "autocomplete": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "edge_ngram_filter"]
        },
        "text_search": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": ["lowercase", "synonym"]
        }
      },
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed",
          "user_dictionary": "user_dict.txt"
        },
        "ngram_tokenizer": {
          "type": "ngram",
          "min_gram": 2,
          "max_gram": 5,
          "token_chars": ["letter", "digit"]
        }
      },
      "filter": {
        "nori_part_of_speech": {
          "type": "nori_part_of_speech",
          "stoptags": [
            "E", "IC", "J", "MAG", "MAJ", "MM", "SC", "SE", "SF", "SP", "SSC",
            "SSO", "SY", "UNA", "UNKNOWN", "VA", "VCN", "VCP", "VSV", "VV",
            "VX", "XPN", "XR", "XSA", "XSN", "XSV"
          ]
        },
        "synonym": {
          "type": "synonym",
          "synonyms": [
            "향수, 퍼퓸, perfume",
            "남성향수, 남자향수, men's perfume, 맨즈 퍼퓸",
            "여성향수, 여자향수, women's perfume, 우먼즈 퍼퓸",
            "중성향수, 유니섹스향수, unisex perfume",
            "시트러스, citrus, 감귤",
            "플로럴, floral, 꽃향, 플라워",
            "우디, woody, 나무향",
            "머스크, musk, 사향",
            "오리엔탈, oriental, 동양적인",
            "스파이시, spicy, 향신료",
            "프루티, fruity, 과일향",
            "그린, green, 풀향",
            "아쿠아, aqua, 물향, 바다향",
            "파우더리, powdery, 분말향",
            "달콤한, sweet, 스위트",
            "쌉쌀한, 쓴, bitter",
            "가죽, leather, 레더",
            "흙냄새, earthy, 어씨",
            "담배, tobacco, 타바코",
            "바닐라, vanilla, 바닐라향"
          ]
        },
        "edge_ngram_filter": {
          "type": "edge_ngram",
          "min_gram": 1,
          "max_gram": 10
        }
      }
    }
  }
}'
```

#### 3.3 사용자 사전 등록

```bash
# 사용자 사전 파일 생성
cat > user_dict.txt << EOL
시트러스
플로럴
우디
머스크
오리엔탈
스파이시
프루티
아쿠아
파우더리
바닐라
베르가못
라벤더
재스민
로즈
페이션트
샌달우드
베티버
앰버
파출리
시나몬
카다멈
아니스
오렌지블라썸
일랑일랑
네롤리
튜베로즈
아이리스
바이올렛
프랑킨센스
미르
시더우드
베티버
통카빈
코코넛
피그
복숭아
자몽
레몬
라임
베르가못
오드퍼퓸
오드뚜왈렛
오드콜로뉴
퍼퓸
EOL

# 사용자 사전 파일을 Elasticsearch 컨테이너에 복사
docker cp user_dict.txt pick-perfume-elasticsearch:/usr/share/elasticsearch/config/

# Elasticsearch 재시작
docker restart pick-perfume-elasticsearch
```

### 4. 매핑 설정

```bash
curl -X PUT "localhost:9200/perfumes/_mapping" -H "Content-Type: application/json" -d'
{
  "properties": {
    "id": {
      "type": "long"
    },
    "name": {
      "type": "text",
      "analyzer": "korean",
      "fields": {
        "keyword": {
          "type": "keyword"
        },
        "ngram": {
          "type": "text",
          "analyzer": "ngram_analyzer"
        }
      }
    },
    "content": {
      "type": "text",
      "analyzer": "korean",
      "fields": {
        "standard": {
          "type": "text",
          "analyzer": "standard"
        }
      }
    },
    "releaseYear": {
      "type": "integer"
    },
    "brandName": {
      "type": "keyword",
      "fields": {
        "text": {
          "type": "text",
          "analyzer": "korean"
        }
      }
    },
    "brandId": {
      "type": "long"
    },
    "concentration": {
      "type": "keyword"
    },
    "imageUrl": {
      "type": "keyword"
    },
    "notes": {
      "type": "nested",
      "include_in_parent": true,
      "properties": {
        "id": {
          "type": "long"
        },
        "name": {
          "type": "text",
          "analyzer": "korean",
          "fields": {
            "keyword": {
              "type": "keyword"
            },
            "ngram": {
              "type": "text",
              "analyzer": "ngram_analyzer"
            }
          }
        },
        "type": {
          "type": "keyword"
        }
      }
    },
    "accords": {
      "type": "nested",
      "include_in_parent": true,
      "properties": {
        "id": {
          "type": "long"
        },
        "name": {
          "type": "text",
          "analyzer": "korean",
          "fields": {
            "keyword": {
              "type": "keyword"
            },
            "ngram": {
              "type": "text",
              "analyzer": "ngram_analyzer"
            }
          }
        }
      }
    },
    "designers": {
      "type": "nested",
      "properties": {
        "id": {
          "type": "long"
        },
        "name": {
          "type": "text",
          "analyzer": "korean",
          "fields": {
            "keyword": {
              "type": "keyword"
            }
          }
        },
        "role": {
          "type": "keyword"
        }
      }
    },
    "averageRating": {
      "type": "double"
    },
    "reviewCount": {
      "type": "integer"
    },
    "isApproved": {
      "type": "boolean"
    },
    "seasonality": {
      "type": "object",
      "properties": {
        "spring": {
          "type": "float"
        },
        "summer": {
          "type": "float"
        },
        "fall": {
          "type": "float"
        },
        "winter": {
          "type": "float"
        }
      }
    },
    "gender": {
      "type": "keyword"
    },
    "createdAt": {
      "type": "date",
      "format": "date_hour_minute_second"
    },
    "updatedAt": {
      "type": "date",
      "format": "date_hour_minute_second"
    }
  }
}'
```

### 5. 인덱스 상태 확인

```bash
# 인덱스 목록 확인
curl -X GET "localhost:9200/_cat/indices?v"

# 특정 인덱스 설정 확인
curl -X GET "localhost:9200/perfumes/_settings?pretty"

# 특정 인덱스 매핑 확인
curl -X GET "localhost:9200/perfumes/_mapping?pretty"

# 분석기 테스트
curl -X POST "localhost:9200/perfumes/_analyze" -H "Content-Type: application/json" -d'
{
  "analyzer": "korean",
  "text": "향수는 좋은 향기가 나는 액체입니다"
}'
```

### 6. 인덱스 삭제 (필요시)

```bash
curl -X DELETE "localhost:9200/perfumes"
```

## 유용한 Elasticsearch API 명령어

### 클러스터 상태 확인

```bash
curl -X GET "localhost:9200/_cluster/health?pretty"
```

### 노드 정보 확인

```bash
curl -X GET "localhost:9200/_nodes?pretty"
```

### 인덱스 통계 확인

```bash
curl -X GET "localhost:9200/perfumes/_stats?pretty"
```

### 인덱스 별칭 생성

```bash
curl -X POST "localhost:9200/_aliases" -H "Content-Type: application/json" -d'
{
  "actions": [
    {
      "add": {
        "index": "perfumes",
        "alias": "perfumes_search"
      }
    }
  ]
}'
```

### 인덱스 새로고침 (변경사항 즉시 검색 가능하게)

```bash
curl -X POST "localhost:9200/perfumes/_refresh"
```

### 인덱스 강제 병합 (성능 최적화)

```bash
curl -X POST "localhost:9200/perfumes/_forcemerge"
```
