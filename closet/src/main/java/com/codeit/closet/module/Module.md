---

# 📄 **Module.md**

## 📦 Module(Domain) Guide

`module` 디렉토리는 각 도메인(User, Auth, Order 등)을 독립적으로 관리하기 위한 공간입니다.  
각 도메인은 controller/service/repository/entity/dto 기준으로 구성됩니다.

---

## 📁 디렉토리 구조 예시

```
module/
├─ user/
│   ├─ controller/
│   ├─ service/
│   ├─ repository/
│   ├─ entity/
│   └─ dto/
│
├─ auth/
├─ order/
└─ ...
```
---

## ✨ 포함되는 요소

| 위치 | 설명 |
|---|---|
| controller | API Endpoint, HTTP 진입점 |
| service | 비즈니스 로직 실행 |
| repository | 데이터 접근 계층 (JPA, QueryDSL 등) |
| entity | DB 테이블 매핑 객체 |
| dto | Request/Response 사용 |

---

## 📌 모듈 구성 규칙

- 같은 도메인 로직은 **같은 폴더 안에서 해결**
- 도메인 간 직접 의존 최소화 (공통 로직은 common으로 이동)
- Controller → Service → Repository 흐름 유지
- Entity는 해당 도메인에 속하며 타 도메인과 분리

---

```
## 👀 User 도메인 예시 코드

```java
// module/user/entity/User.java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String name;
}
```

```java
// module/user/service/UserService.java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }
}
```

---

## 도메인 추가하는 방법

```
module/
 └─ product/
     ├─ controller
     ├─ service
     ├─ repository
     ├─ entity
     └─ dto
```

→ 생성 후 Service/Entity/Repository만 작성하면 곧바로 사용 가능

---