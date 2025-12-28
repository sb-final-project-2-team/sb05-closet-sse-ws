> 본 문서는 Code Review 기준 문서입니다.
> 일부 규칙은 강제 규칙(MUST)이며,
> 일부는 권장 규칙(SHOULD)입니다.
> 리뷰어는 규칙 위반 시 맥락을 고려하여 판단합니다.

---

## 기본 네이밍

### 1. 변수, 메서드, 함수

- **메서드나 변수** 표기 시 카멜케이스 (Camel Case) 적용

```java
// 단어 첫 글자 소문자 시작 + 단어 사이 구분을 대문자 사용
String userName;
String userEmail;
private User createUser(...){
```

- **변수** 작성 시에, 짧게 쓰기보단 **최대한 자세하게** 작성한다.

```java
// Good Case
UserCreateRequest userCreateRequest = new UserCreateRequest();   

// Bad Case
UserCreateRequest request = new UserCreateRequest();   
```

- **다른 타입으로 전환하는 메서드/빌더** 패턴을 구현한 **클래스의 메서드**에는 전치사를 쓸 수 있다.
`ex) toString() to[Entity]()`
- **메서드 이름**은 동사/전치사로 짓는다. (기본은 동사)
`ex) findByUsername, findByUserWithProfile`

### **2. 클래스**

- **클래스나 인터페이스** 표기 시 파스칼 케이스(PascalCase) 적용

```java
// 단어 첫 글자를 대문자로 시작 + 단어 사이 구분을 대문자 사용
record UserRequestDTO
interface UserService(...)
```

- **클래스 이름**은 명사로 짓는다.
`public class BasicUserService`
- **인터페이스 이름**은 명사/형용사로 짓는다.
`public interface BinaryContentService`

### **3. 패키지**

- **패키지 명** 표기 시 전체 소문자(LowerCase) 적용

```java
// Good Case
package com.sprint.mission.discodeit.exception.userstatus;  

// Bad Case
package com.sprint.mission.discodeit.exception.userStatus; 
```

### **4. 컬럼명**

- **DB 컬럼명**은 스네이크 케이스(SnakeCase) 적용

```java
  @Column(name = "last_read_at", nullable = false)
  private Instant lastReadAt;

```

### **5. 상수**

- **상수(static final로 선언된 필드)는 대문자와 언더스코어(*)로 구성한다.**
`ex)* private static final MAX_INTEGER_VALUE`

---

## 서비스 CRUD 메서드 이름

### 1. 생성
> 기본 CRUD 메서드는 create / find / update / delete를 사용한다.
> 단, 추가 파라미터가 필요하거나 명확한 비즈니스 의미가 있는 경우
> 도메인 행위 중심 메서드 네이밍을 허용한다.

- create

```java
// Good Case
public UserResponse create(CreateUserRequest createUserRequest ){...}      

// Bad Case
public UserResponse createUser(CreateUserRequest createUserRequest ){...}  
```

### 2. 조회

- find, findAll

```java
public UserResponse find(...)

public UserFilteredListResponse findFilteredList(...)
```

### 3. 수정

- update

```java
public UpdateUserResponse update(...)
```

### 4. 삭제

- delete

```java
public void delete(...)

public String delete(...)
```

---

## DTO 이름

### 1. 엔티티 공통 출력명

- **엔티티별 공통  출력값 [Entity] + DTO** (!!DTO는 반드시 대문자로!!)
- “기본적으로 Response / Request를 사용하며, 공통 출력용 DTO가 필요한 경우에만 DTO 접미사를 사용한다.”

```java
public record UserResponse (...){...}

// (DTO는 중복되는 출력값이 많이 있을 경우 사용)
public record UserDTO (...) {...}
```

### 2. 기능 DTO 명

- **명사 + CRUD + res/req**

```java
public record UpdateUserResponse (...) {...}

public record UpdatePasswordRequest (...) {...}
```

---

## Repository (JPA) 컨벤션

### 1. Repository 네이밍

- Repository 인터페이스는 **Entity + Repository** 형태로 작성한다.
- 반드시 `JpaRepository<Entity, [IdType]>`를 상속한다.

```java
public interface UserRepository extends JpaRepository<User, UUID> {
}

```

### 2. 기본 CRUD는 JpaRepository 제공 메서드 사용

- **save / findById / findAll / deleteById** 등은 직접 정의하지 않는다.
- 특별한 이유가 없는 한 커스텀 메서드 작성 금지.

```java
// Good Case
userRepository.save(user);
userRepository.findById(userId);

// Bad Case
User saveUser(User user);
Optional<User> findUserById(UUID id);
```

### 3. 조회 메서드 네이밍 규칙

### 3-1. 기본 조회

- 단건 조회: `findBy`
- 다건 조회: `findAllBy`

```java
Optional<User> findByEmail(String email);

List<User> findAllByStatus(UserStatus status);

```

### 3-2. 조건이 2개 이상일 경우

- `And` 사용
- **조건 순서는 비즈니스 중요도 순**

```java
Optional<User> findByEmailAndDeletedAtIsNull(String email);

List<User> findAllByChannelIdAndStatus(UUID channelId, UserStatus status);

```

### 3-3. Boolean / 상태 값 조건

- Boolean 필드는 `IsTrue / IsFalse`
- nullable 상태는 `IsNull / IsNotNull`

```java
List<User> findAllByIsOnlineTrue();

Optional<User> findByIdAndDeletedAtIsNull(UUID id);

```

### 4. 존재 여부 확인 메서드

- `existsBy` 사용
- **중복 검사 전용**

```java
boolean existsByEmail(String email);

boolean existsByUsername(String username);

// Bad Case
User findByEmail(String email); // 중복 검사 용도로 사용 ❌
```

### 5. 카운트 쿼리

- `countBy` 사용
- 페이징, 제한 조건 계산용으로만 사용

```java
long countByChannelId(UUID channelId);
```

### 6. 삭제 메서드 컨벤션

### 6-1. Soft Delete

- Repository에서는 **deleteBy 사용 금지**
- Service 계층에서 상태 변경으로 처리

```java
// User 엔티티
private Instant deletedAt;

// Service
user.markAsDeleted();

```

### 6-2. Hard Delete

- 물리 삭제가 필요한 경우에만 `deleteBy` 사용

```java
void deleteById(UUID id);

```

### 7. 정렬 & 페이징

- 정렬은 `OrderBy`
- 페이징은 `Pageable` 사용

```java
List<Message> findAllByChannelIdOrderByCreatedAtDesc(UUID channelId);

Page<User> findAllByStatus(UserStatus status, Pageable pageable);

```

### 8. Fetch Join / 성능 최적화 규칙

### 8-1. 연관 관계 즉시 로딩 금지

- `@ManyToOne(fetch = FetchType.LAZY)` 기본
- Repository에서 필요한 경우만 fetch join

---

### 8-2. fetch join 메서드는 네이밍에 명시 ⭐️

- `With[연관엔티티]` 접미사 사용

```java
@Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.profile
    WHERE u.id = :userId
""")
Optional<User> findByIdWithProfile(UUID userId);

```

### 9. @Query 사용 규칙

- 메서드 네이밍으로 표현 불가능한 경우만 사용
- JPQL 사용을 원칙으로 한다 (Native Query 지양)

```java
@Query("""
    SELECT u
    FROM User u
    WHERE u.lastLoginAt < :time
""")
List<User> findInactiveUsers(Instant time);

```

### 10. Repository 반환 타입 규칙

- 단건: `Optional<Entity>`
- 다건: `List<Entity>` / `Page<Entity>`
- DTO 직접 반환은 **조회 최적화 목적일 때만 허용**

```java
Optional<User> findById(UUID id);

Page<User> findAll(Pageable pageable);

```

```java
@Query("""
    SELECT new com.sprint.mission.discodeit.dto.UserDTO(u.id, u.username)
    FROM User u
""")
List<UserDTO> findUserSummaryList();

```

---

## Service 컨벤션

### 1. Service 네이밍

### 인터페이스

- **[Entity] + Service**

```java
public interface UserService {
}

// Bad Case
public interface UserServiceImpl {
}
```

### 구현체

- **[역할] + [Entity] + Service**
- 기본 구현체는 `Basic` 접두사 사용

```java
public class BasicUserService implements UserService {
}

// Bad Case
public class UserServiceImpl {
}

```

### 2. Service 역할과 책임

- 비즈니스 로직 처리
- 트랜잭션 관리
- Repository 결과 해석 및 예외 변환
- DTO ↔ Entity 변환 (또는 Mapper 호출)

### 3. 트랜잭션 규칙

- Service 계층에서만 `@Transactional` 사용
- 조회 전용 메서드는 `readOnly = true`

```java
@Transactional
public UserResponse createUser(UserCreateRequest userCreateRequest) {
}

```

```java
@Transactional(readOnly = true)
public UserResponse findUser(UUID userId) {
}

```

### 4. Service 메서드 네이밍

- CRUD 규칙을 그대로 따른다

```java
public UserResponse createUser(UserCreateRequest userCreateRequest);

public UserResponse findUser(UUID userId);

public UserResponse updateUser(UUID userId, UserUpdateRequest userUpdateRequest);

public void deleteUser(UUID userId);

```

### 5. Service 반환 타입

- Entity 직접 반환 금지
- Response DTO 반환

```java
public UserResponse findUser(UUID userId);

```

```java
// Bad Case
public User findUser(UUID userId);

```

### 6. 예외 처리

- Optional 직접 사용 금지
- 의미 있는 도메인 예외로 변환

```java
User user = userRepository.findById(userId)
    .orElseThrow(() -> new UserNotFoundException(userId));

```

```java
// Bad Case
return userRepository.findById(userId).get();

```

### 7. 중복 검사

- Service에서 판단
- `existsBy` 메서드만 사용

```java
if (userRepository.existsByEmail(request.email())) {
    throw new DuplicateEmailException();
}

```

```java
// Bad Case
User user = userRepository.findByEmail(request.email()).orElse(null);

```

### 8. 삭제 규칙

### 8-1. Soft Delete

- delete 메서드는 상태 변경

```java
@Transactional
public void deleteUser(UUID userId) {
    User user = findUserEntity(userId);
    user.markAsDeleted();
}

```

- `deleteUser` = **논리 삭제**
- 상태 변경 (`deletedAt`)
- Repository `deleteBy` ❌

### 8-2. Hard Delete

- hardDelete[Entity]
- Repository `deleteBy` 사용 가능

```java
@Transactional
public void hardDeleteUser(UUID userId) {
    userRepository.deleteById(userId);
}
```

- `hardDeleteUser` = **물리 삭제**
- Repository `deleteById` 사용 O

### 9. 조회 전용 메서드

- 비즈니스 의미 중심으로 네이밍

```java
@Transactional(readOnly = true)
public UserFilteredListResponse findUserFilteredList(UserFilterCondition condition) {
}

```

```java
// Bad Case
public List<User> findUsers(UserFilterCondition condition);

```

### 10. Service 내부 헬퍼 메서드

- 공통 Entity 조회 로직은 private
- 외부 노출 금지

```java
private User findUserEntity(UUID userId) {
    return userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
}

// Bad Case
public User findUserEntity(UUID userId) {
}
```

---

## GIT 컨벤션

### 메시지 구조

- 커밋 메시지는 크게 **제목, 본문, 꼬리말** 세 가지로 나누고 각 파트는 빈칸을 두어서 구분을 하게 해준다.

```markdown
type: subject
(한 칸 뛰운다.)
body - (선택)
(한 칸 뛰운다.)
footer - (선택)
```

기본적으로 소문자만 사용하고, 같은 파트는 내용들은 이어서 붙인다.

위처럼 type(태그), subject(제목), body(내용), footer(꼬리말)이 들어가게 되는데

각종 파트별로 스타일을 구분한다.

### type (태그)

- **feat: 기능 추가**
- **fix : 버그 수정**
- **docs: 문서 수정**
- style : 코드 포맷팅, 세미콜론 누락, 코드 변경 없을 때
- refactor: 코드 리펙토링
- test: 테스트 코드 추가 및 리펙토링 테스트 코드 추가
- chore : 기타 등등

전체적으로는 feat, fix, docs가 많이 사용을 하면 된다.

### subjet (제목)

- 제목은 절대 길게 사용하지 않는다. (30자 이내)
- 개조식 구문을 작성 → 완전한 서술형 문장이 아닌 간결하고 요점적인 서술

  ex) 유저 서비스 수정하였다. → 유저 서비스 **수정**

- 기능에 대한 명시를 사용할 것 클래스 네임, 코드 네임 사용 X

  ex) userCreateInfo 수정 X → 유저 생성 기능 추가

- 영문 표기일 경우 동사 사용 (과거형 금지)

  ex) **fixed (X)  —> Fix (O)**


### body (본문) [선택]

- **제목만으로 기능 구현 상황을 표현하기 어려울 시 본문도 같이 적기**
- 기본적으로 50자 내 작성
- **하지만!! 무조건 적으로 50자를 맞출 필요없음 자세하게 작성하는게 더 중요**
- 쓰기 어려우면 본문 내용은 **무엇을 → 어떻게 → 왜** 이렇게 3가지를 기본적으로 구성하여 짠다.
  단 “왜”를 굳이 넣을 필요 없음
- 하나의 클래스에 여러작업이 일어났으면 줄마다 작성 하여 구분

### footer (꼬릿말)

closes : 일반적인 개발과 관련된 이슈에 해당하는 경우 이슈 번호

fixes : 버그 픽스, 핫 픽스 관련 이슈에 해당하는 경우 이슈 번호

see also : 커밋의 이슈와 연관되어 있는 이슈들이 존재 하는 경우, 또는 관련된 이슈들이 있는 경우 이슈 번호

## Commit 예시

```markdown
feat: 유저 회원 가입 기능 구현

Oauth2 적용 OpenAPI사용하여 구성함.
코드 구조 개선 Mapper사용하여 구성함 가독성을 위해서.

closes: #42
fixes: #12
```

---