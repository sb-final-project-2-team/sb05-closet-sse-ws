# 📄 **Common.md**

## 🧩 Common Module Guide

`common` 패키지는 프로젝트 전역에서 공통적으로 사용되는 기능을 모아두는 공간입니다.  
비즈니스 도메인에 종속적이지 않은 재사용 가능 요소만 저장합니다.

---

## 📁 디렉토리 구조 예시

```

common/
├─ config/          # 전역 설정 파일(Spring Config)
├─ exception/       # 공통 Exception · ErrorCode · GlobalExceptionHandler
├─ util/            # 재사용 Util (StringUtils, TokenUtils 등)
└─ response/        # 공통 Response Wrapper(Optional)

```


---

## ✨ Common에 들어갈 수 있는 요소

| 항목 | 예시 |
|---|---|
| 전역 설정 | SecurityConfig, SwaggerConfig, WebConfig |
| 공통 예외처리 | GlobalExceptionHandler, CustomException, ErrorCode |
| 응답 포맷 | ApiResponse<T> wrapper 구조 |
| 공용 유틸 | TokenUtil, DateTimeUtil 등 |
| 인터셉터/필터 | LoggingInterceptor, AuthFilter |

---

## 📌 규칙

- **도메인 의존 X** (common → module 방향 가능, 반대 금지)
- 모든 모듈에서 사용될 수 있는 코드만 배치
- 비즈니스 로직 또는 특정 도메인 로직은 포함하지 않음

---

## 👍 예시 코드

```java
// common/exception/ErrorCode.java
public enum ErrorCode {
    USER_NOT_FOUND,
    INVALID_REQUEST,
    INTERNAL_SERVER_ERROR
}

```