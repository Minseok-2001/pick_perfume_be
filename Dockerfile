FROM gradle:8.13-jdk21 AS build

WORKDIR /app

# 의존성 캐싱을 위해 먼저 build.gradle.kts 파일만 복사
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# 의존성 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사 (자주 변경되는 파일은 마지막에 복사)
COPY src ./src

# 빌드
RUN gradle build -x test --no-daemon

# 실행 이미지
FROM eclipse-temurin:21-jre

WORKDIR /app

# 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=UTC

# 빌드 결과물 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Elasticsearch 설정 파일 복사
COPY mapping.json settings.json ./

# 실행 스크립트
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"] 