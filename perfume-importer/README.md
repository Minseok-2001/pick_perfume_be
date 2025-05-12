# 향수 데이터 가져오기 (Perfume Importer)

이 프로젝트는 CSV 파일에서 향수 데이터를 가져와 MySQL 데이터베이스에 저장하는 배치 프로그램입니다.

## 기능

- 브랜드, 향수, 노트, 어코드 데이터 가져오기
- CSV 파일 데이터를 읽고 처리
- MikroORM을 사용한 엔티티 관리
- 로깅 및 오류 처리

## 시작하기

### 필수 조건

- Node.js 14 이상
- MySQL 서버
- TypeScript

### 설치

1. 저장소 클론하기

   ```
   git clone <repository-url>
   cd perfume-importer
   ```

2. 의존성 설치하기

   ```
   npm install
   ```

3. 환경 설정

   - `example.env` 파일을 `.env`로 복사하고 필요한 설정을 변경합니다.

   ```
   cp example.env .env
   ```

4. 데이터베이스 스키마 생성
   ```
   npx mikro-orm schema:create --run
   ```

### 실행

다음 명령어로 배치 작업을 실행합니다:

```
npm run import
```

## CSV 파일 형식

1. **brand.csv**: 브랜드 정보

   - id: 브랜드 ID
   - name: 브랜드 이름
   - country: 브랜드 국가

2. **perfume_simple.csv**: 향수 정보

   - id: 향수 ID
   - url: 이미지 URL
   - title: 향수 이름
   - brand_id: 브랜드 ID
   - gender: 성별 및 농도 정보
   - rating_value: 평점
   - rating_count: 평점 수
   - year: 출시 연도
   - perfumer1: 조향사 1
   - perfumer2: 조향사 2
   - content: 설명

3. **note.csv**: 노트 정보

   - id: 노트 ID
   - perfume_id: 향수 ID
   - note_type: 노트 타입 (TOP, MIDDLE, BASE)
   - note_name: 노트 이름

4. **main_accord.csv**: 어코드 정보
   - id: 어코드 ID
   - perfume_id: 향수 ID
   - accord_name: 어코드 이름

## 프로젝트 구조

```
perfume-importer/
├── src/
│   ├── batch/
│   │   ├── models/          # CSV 데이터 모델
│   │   ├── services/        # 데이터 가져오기 서비스
│   │   └── importPerfume.ts # 배치 작업 진입점
│   ├── config/              # 설정 파일
│   ├── entities/            # ORM 엔티티
│   └── utils/               # 유틸리티 함수
├── .env                     # 환경 변수
├── package.json
└── tsconfig.json
```

## 추가 기능

- **디버깅 모드**: 자세한 로그 확인을 위해 `DEBUG=1` 환경 변수 설정
- **부분 가져오기**: 특정 데이터만 가져오려면 `--only=brands,perfumes` 옵션 사용

## 라이센스

이 프로젝트는 ISC 라이센스를 따릅니다.
