# sb-main-project
메인 프로젝트 입니다.

# 기초 템플릿 구성
ISSUE : bug report, feature report 구성
CI : 구성
CODEOWNERS 구성
PR : PR TEMPLATE 구성

ISSUE 작업 자동화 확인
코드래빗 적용 테스트

브랜치 규칙 적용 완료
main, develop 직접 push reject, 코드 리뷰 2명이상 해야 push, merge가능

## 📦 Tech Stack & Dependencies

본 프로젝트는 **Spring Boot 4.x + Java 17** 기반으로 구성되어 있으며,
확장성과 실무 사용성을 고려한 라이브러리들을 사용합니다.

---

### ☕ Language & Build

* **Java 17**
* **Gradle**
* **Spring Boot 4.0.1**

---

### 🌐 Web & API

* **spring-boot-starter-web**

    * REST API 개발을 위한 기본 Web MVC 스택
* **spring-boot-starter-validation**

    * Bean Validation (`@NotNull`, `@Email` 등)
* **spring-boot-starter-thymeleaf**

    * 서버 사이드 렌더링(View) 지원
* **spring-boot-starter-websocket**

    * 실시간 통신(WebSocket) 기능 지원

---

### 🔐 Security & Authentication

* **spring-boot-starter-security**

    * 인증/인가 전반 처리
* **spring-boot-starter-oauth2-client**

    * OAuth2 기반 소셜 로그인 (Google, Kakao 등)
* **thymeleaf-extras-springsecurity6**

    * Thymeleaf + Spring Security 연동

---

### 🗄️ Data & Persistence

* **spring-boot-starter-data-jpa**

    * JPA 기반 ORM 처리
* **spring-boot-starter-batch**

    * 대용량 데이터 처리 및 배치 작업
* **PostgreSQL Driver**

    * 운영 환경용 RDBMS
* **H2 Database**

    * 개발 및 테스트용 인메모리 DB

---

### 🔄 DTO Mapping

* **MapStruct**

    * 컴파일 타임 기반 DTO ↔ Entity 매핑
    * 성능 저하 없는 명시적 매핑 처리

---

### ✉️ Mail (SMTP)

* **spring-boot-starter-mail**

    * 이메일 인증, 알림 메일, 비밀번호 재설정 등 SMTP 메일 전송

---

### 🔁 Resilience & Stability

* **Spring Cloud Resilience4j**

    * Circuit Breaker 기반 장애 대응
    * 외부 시스템 장애 격리 및 복구 처리

---

### 📊 Monitoring & Observability

* **spring-boot-starter-actuator**

    * 애플리케이션 상태 및 메트릭 제공
* **Micrometer Prometheus Registry**

    * Prometheus 연동 메트릭 수집

---

### 🛠️ Development Productivity

* **Lombok**

    * Boilerplate 코드 제거 (`@Getter`, `@Builder` 등)
* **Spring Boot DevTools**

    * 개발 중 자동 재시작, Live Reload 지원

---

### 🧪 Test

* **spring-boot-starter-test**

    * JUnit 5, Mockito 등 통합 테스트 환경
* **spring-security-test**

    * 인증/인가 테스트 지원
* **spring-batch-test**

    * Batch Job / Step 테스트 전용 유틸리티

---

## 🔍 Environment Strategy

* **H2**: 로컬 개발 및 테스트 환경
* **PostgreSQL**: 운영 환경
* **Spring Profile 기반 설정 분리 예정**

---

## ✨ Design Considerations

* 실무 기준 확장 가능한 구조
* 인증 / 배치 / 메일 / 장애 대응을 고려한 구성
* 운영 환경을 고려한 모니터링 및 메트릭 수집

---
## 📁 프로젝트 구조 가이드 (단일 모듈 + 도메인 분리 기반 DDD Lite)

>핵심: common은 공용, module 안에 비즈니스 도메인 분리,
>service/repository 방식 사용 가능.
>단지 도메인을 기능 묶음 기준으로 독립성 있게 관리한다는 목적.

🔥 디렉토리 구조 예시

``` markdown
src/main/java/com/project
├─ common/                         # 공통 모듈 (전역에서 활용)
│   ├─ config/                      # 공통 설정(Spring Config 등)
│   ├─ exception/                   # GlobalException, ErrorCode 등
│   └─ util/                        # 유틸 클래스
│
└─ module/                         # 도메인 그룹
├─ user/                       # User Domain
│   ├─ controller/             # API 진입점
│   ├─ service/                # 서비스(비즈니스 로직)
│   ├─ repository/             # Repository (JPA/Hibernate)
│   ├─ entity/                 # DB Entity
│   └─ dto/                    # Request / Response DTO
│
├─ auth/                       # Auth Domain
│   ├─ controller/
│   ├─ service/
│   ├─ repository/
│   ├─ entity/
│   └─ dto/
│
├─ order/
│   ├─ controller/
│   ├─ service/
│   ├─ repository/
│   ├─ entity/
│   └─ dto/
│
└─ ...
```
📌 ModuleConfig 제거, 도메인별 폴더로만 분리
📌 Service/Repository 구조는 동일하게 유지