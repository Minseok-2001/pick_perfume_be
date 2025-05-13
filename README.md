# Pick Perfume Backend

향수 추천 및 커뮤니티 서비스를 위한 백엔드 서비스입니다.

## 기술 스택

- Kotlin + Spring Boot
- MySQL
- Elasticsearch
- Docker
- AWS Elastic Beanstalk

## 로컬 개발 환경 설정

### 필수 요구사항

- JDK 21
- Docker & Docker Compose
- Gradle

### 로컬 환경 실행 방법

1. 저장소 클론

   ```
   git clone https://github.com/your-username/pick_perfume_be.git
   cd pick_perfume_be
   ```

2. 환경 변수 설정

   ```
   cp .env.example .env
   ```

   `.env` 파일을 열고 필요한 환경 변수를 설정합니다.

3. Docker Compose로 의존성 서비스 실행

   ```
   docker-compose up -d
   ```

4. 애플리케이션 실행

   ```
   ./gradlew bootRun
   ```

5. API 문서 접속
   ```
   http://localhost:8080/docs
   ```

## 배포

### AWS Elastic Beanstalk 배포

프로젝트는 GitHub Actions를 통해 AWS Elastic Beanstalk에 자동으로 배포됩니다.
`main` 브랜치에 코드가 푸시되면 자동으로 배포 프로세스가 시작됩니다.

1. Docker Hub에 이미지가 빌드되어 푸시됩니다.
2. Elastic Beanstalk은 Docker Hub에서 이미지를 가져와 실행합니다.

### GitHub Secrets 설정

GitHub 저장소에 다음 시크릿을 설정해야 합니다:

- `DOCKERHUB_USERNAME`: Docker Hub 사용자명
- `DOCKERHUB_TOKEN`: Docker Hub 액세스 토큰
- `AWS_ACCESS_KEY_ID`: AWS 액세스 키
- `AWS_SECRET_ACCESS_KEY`: AWS 시크릿 키
- `AWS_REGION`: AWS 리전

### 환경 변수 설정

Elastic Beanstalk에서 다음 환경 변수를 설정해야 합니다:

1. AWS Elastic Beanstalk 콘솔에 접속
2. 애플리케이션 > 환경 선택
3. 구성 > 소프트웨어 수정 클릭
4. 환경 속성에 다음 변수 추가:
   - `SPRING_DATASOURCE_URL`: 데이터베이스 URL
   - `SPRING_DATASOURCE_USERNAME`: 데이터베이스 사용자명
   - `SPRING_DATASOURCE_PASSWORD`: 데이터베이스 비밀번호
   - `ELASTICSEARCH_HOST`: Elasticsearch 호스트
   - `ELASTICSEARCH_PORT`: Elasticsearch 포트
   - `ELASTICSEARCH_USERNAME`: Elasticsearch 사용자명
   - `ELASTICSEARCH_PASSWORD`: Elasticsearch 비밀번호
   - `JWT_SECRET_KEY`: JWT 비밀 키
   - `AWS_ACCESS_KEY`: AWS 액세스 키
   - `AWS_SECRET_KEY`: AWS 시크릿 키
   - `AWS_S3_BUCKET`: S3 버킷 이름
   - `SWAGGER_USERNAME`: Swagger UI 접근용 사용자명
   - `SWAGGER_PASSWORD`: Swagger UI 접근용 비밀번호

## API 문서

API 문서는 Swagger UI를 통해 제공됩니다.
배포된 환경에서는 `/docs` 경로로 접근할 수 있으며, Basic Auth 인증이 필요합니다.

## 프로젝트 구조

```
src/main/kotlin/ym_cosmetic/pick_perfume_be/
├── accord/           # 향 노트 관련 기능
├── auth/             # 인증 관련 기능
├── batch/            # 배치 작업 관련 기능
├── brand/            # 브랜드 관련 기능
├── common/           # 공통 기능 및 설정
├── designer/         # 디자이너 관련 기능
├── infrastructure/   # 인프라 관련 기능 (S3, ES 등)
├── member/           # 회원 관련 기능
├── note/             # 노트 관련 기능
├── perfume/          # 향수 관련 기능
├── recommendation/   # 추천 관련 기능
├── review/           # 리뷰 관련 기능
├── search/           # 검색 관련 기능
├── security/         # 보안 관련 기능
├── survey/           # 설문 관련 기능
└── vote/             # 투표 관련 기능
```

## 환경 설정 가이드

### 로컬 개발 환경

1. 로컬에서 개발 시 `application.yml` 파일을 사용합니다.
2. 민감한 정보는 `application-local.yml` 파일에 별도로 관리하세요 (gitignore에 포함됨).

### 배포 환경 (AWS Elastic Beanstalk)

1. 환경변수는 Elastic Beanstalk 콘솔에서 설정합니다.
2. 다음 환경변수를 설정해야 합니다:

```
# 데이터베이스 설정
RDS_HOSTNAME=your_rds_hostname
RDS_PORT=3306
RDS_DB_NAME=your_db_name
RDS_USERNAME=your_username
RDS_PASSWORD=your_password

# Elasticsearch 설정
ES_HOST=your_es_host
ES_PORT=9200
ES_USERNAME=elastic
ES_PASSWORD=your_es_password

# JWT 설정
JWT_SECRET=your_jwt_secret

# S3 설정 (필요한 경우)
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
S3_BUCKET_NAME=your_bucket_name
```

3. `.ebextensions/03_environment.config` 파일이 이러한 환경변수를 애플리케이션에서 사용할 수 있도록 설정합니다.

### 보안 관련 주의사항

- 절대로 민감한 정보(DB 비밀번호, API 키 등)를 코드에 하드코딩하지 마세요.
- 항상 환경변수나 안전한 시크릿 관리 서비스를 통해 관리하세요.
- 로컬 개발 시 사용하는 민감 정보는 `.gitignore`에 포함된 파일에 보관하세요.

## 배포 방법

1. 코드를 빌드합니다: `./gradlew build`
2. Docker 이미지를 생성합니다: `docker build -t scentist-app .`
3. AWS Elastic Beanstalk에 배포합니다: `./deploy.sh`

## Elasticsearch 설정

Elasticsearch는 향수 검색 및 추천 기능에 사용됩니다.
초기 설정은 `es-setup.sh` 스크립트를 통해 수행할 수 있습니다.

## 배포 방법

### AWS Elastic Beanstalk 배포 (Java 플랫폼)

도커 없이 AWS Elastic Beanstalk의 Java 21 Corretto 플랫폼을 사용하여 배포할 수 있습니다.

#### 배포 준비

1. AWS CLI가 설치되어 있고 적절한 권한이 설정되어 있어야 합니다.
2. 배포 스크립트에 실행 권한이 있어야 합니다:
   ```bash
   chmod +x deploy-eb.sh
   ```

#### 배포 실행

다음 명령어로 배포를 실행합니다:

```bash
./deploy-eb.sh
```

필요한 경우 환경 변수를 설정하여 배포 대상을 변경할 수 있습니다:

```bash
EB_ENV_NAME=my-env EB_APP_NAME=my-app AWS_REGION=ap-northeast-2 ./deploy-eb.sh
```

#### 배포 구성

배포 설정은 `.ebextensions` 디렉토리에 있는 설정 파일로 관리됩니다:

- `01_java.config`: Java 환경 설정
- `02_elasticsearch.config`: Elasticsearch 설정
- `03_files.config`: 파일 복사 설정
- `04_logging.config`: 로깅 설정

#### 배포 내용

이 배포 방식은 다음과 같은 장점이 있습니다:

1. 도커 없이 직접 Java 애플리케이션으로 배포하여 배포 시간 단축
2. AWS Elastic Beanstalk의 관리형 Java 21 Corretto 환경 활용
3. 필요한 설정 파일 및 스크립트 자동 배포
