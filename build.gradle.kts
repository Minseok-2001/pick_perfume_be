plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
}

kapt {
    correctErrorTypes = true
}


group = "ym_cosmetic"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // 스프링 핵심 의존성
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.session:spring-session-jdbc")


    // mail
    implementation ("org.springframework.boot:spring-boot-starter-mail")


    // 코틀린 관련
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 이벤트 처리
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // 데이터베이스
    implementation("com.mysql:mysql-connector-j")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.h2database:h2")

    // 마이그레이션
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")

    // 보안
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.mindrot:jbcrypt:0.4")

    // 캐싱
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // AWS S3
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.589")

    // OpenSearch
    implementation("org.opensearch.client:spring-data-opensearch:1.6.3")
//    implementation("org.opensearch.client:spring-data-opensearch-starter:1.6.3")
//    implementation("org.opensearch.client:opensearch-rest-high-level-client:2.11.1")
//    implementation("org.opensearch.client:opensearch-java:2.11.1")
    implementation("jakarta.json:jakarta.json-api:2.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")


    // API 문서화
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // 모니터링 및 로깅
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // 유틸리티
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("commons-io:commons-io:2.15.1")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")

    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // AWS S3 SDK
    implementation("software.amazon.awssdk:s3:2.23.11")
    // S3 Presigned URL 생성을 위한 의존성
    implementation("software.amazon.awssdk:s3-transfer-manager:2.23.11")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
