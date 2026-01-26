# =========================
# 1️⃣ Build Stage
# =========================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

# 의존성 다운
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src
RUN ./gradlew clean bootJar --no-daemon

## 테스트 + 빌드
## 테스트 최대 3회 재시도
#RUN for i in 1 2 3; do \
#      echo "▶ 테스트 시도 $i"; \
#      ./gradlew clean test bootJar --no-daemon && break; \
#      echo "❌ 실패, 5초 후 재시도"; \
#      sleep 5; \
#    done || (echo "🚨 테스트 최종 실패"; exit 1)

# =========================
# 2️⃣ Runtime Stage
# =========================

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081 9102

ENTRYPOINT ["java", "-jar", "app.jar"]
