# SPRING PLUS

## Lv.1 코드 개선 퀴즈 - @Transactional 의 이해

- 파일위치 :  
package org.example.expert.domain.todo.service.TodoService
---

### 1. 원인
(A) 에러 원문
```text
jakarta.servlet.ServletException: Request processing failed: org.springframework.orm.jpa.JpaSystemException: could not execute statement [Connection is read-only. Queries leading to data modification are not allowed] [insert into todos (contents,created_at,modified_at,title,user_id,weather) values (?,?,?,?,?,?)]
```
(B) 원인 파악 
- 데이터가 변경되어야 할 메서드까지 클래스 단계에서 전역적으로 @Transactional(readOnly = true)가 설정되어 있어, JPA Insert 문이 실행되지 않음.

### 2. 해결
![img.png](images/img.png)
1. 데이터의 변경, 조회, 삭제가 진행되는 매서드에 @Transactional 작성. (조회 매서드는 readOnly = Ture 설정)

## Fixed

```java
@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)  // 전역 설정 주석처리
public class TodoService {

    /**
     * 데이터의 추가, 변경, 삭제 매서드에 @Transactional 작성
     */
    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        // 비즈니스 로직...
        Todo savedTodo = todoRepository.save(newTodo);
        // 비즈니스 로직...
    }

    @Transactional(readOnly = true) // 조회는 readOnry = true 설정
    public Page<TodoResponse> getTodos(int page, int size) {
        // 비즈니스 로직...
        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        // 비즈니스 로직...
    }

    @Transactional(readOnly = true) // 조회는 readOnry = true 설정
    public TodoResponse getTodo(long todoId) {
        // 비즈니스 로직...
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
        // 비즈니스 로직...
    }
}
```

## Lv.2 코드 추가 퀴즈 - JWT의 이해

- 파일위치 :  
package org.example.expert.domain.user.entity.User  
package org.example.expert.domain.user.service.UserService  
package org.example.expert.domain.user.controller.UserController  
package org.example.expert.config.JwtUtil  
package org.example.expert.domain.auth.dto.request.SignupRequest  
package org.example.expert.domain.auth.service.AuthService
---

### 1. 원인
1. 기획자의 요구사항 : User 정보에 nickname 추가.
2. 프론트엔드 개발자 요구사항 : JWT에서 nickname 을 꺼내 보여주길 원함.

### 2. 해결
![img_6.png](images/img_6.png)
![img_7.png](images/img_7.png)


1. User Entity에 nickname 필드 추가.
2. 컨트롤러와 서비스에 nickname 업데이트 기능을 추가하여, 기존 사용자도 문제없이 추가 할 수 있도록 수정.
3. JWT에 해당 nickname 추가. (JwtUtil, AuthService)

## Fixed

```java
@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {
    //...
    
    private String nickname; // nickname 필드 추가

    public User(String email, String password, UserRole userRole, String nickname) {
        this.email = email;
        this.password = password;
        this.userRole = userRole;
        this.nickname = nickname; // 생성자에 nickname 추가
    }
    
    // 유저 nickname 업데이트 매서드 추가
    public void updateNickname(String nickname){
        this.nickname = nickname;
    }
    
    // ...
}
```
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    // ...

    // 기존 유저도 닉네임 추가할 수 있도록 API 작성
    @Transactional
    public void updateNickname(long userId, String nickname){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        user.updateNickname(nickname);
    }

    // ...
}
```
```java
@RestController
@RequiredArgsConstructor
public class UserController {

    // ...
    
    // 기존 유저도 닉네임 추가할 수 있도록 API 작성
    @PatchMapping("/users")
    public void updateNickname(@Auth AuthUser authUser, @RequestParam String nickname){
        userService.updateNickname(authUser.getId(), nickname);
    }
}
```
```java
@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    // ...
    
    public String createToken(Long userId, String email, UserRole userRole, String nickname) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(String.valueOf(userId))
                        .claim("email", email)
                        .claim("userRole", userRole)
                        .claim("nickname", nickname) // JWT 토큰에 nickname 추가
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    // ...
}
```

```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    // ...
    private String nickname; // RequestDto에 닉네임 추가
}
```
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    
    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        // ...
        
        // JWT 토큰에 nickname 추가
        String bearerToken = jwtUtil.createToken(savedUser.getId(), savedUser.getEmail(), userRole, savedUser.getNickname());
        
        //...
    }

    public SigninResponse signin(SigninRequest signinRequest) {
        // ...
        
        // JWT 토큰에 nickname 추가
        String bearerToken = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());
        
        // ...
    }
}
```

## Lv.3 코드 개선 퀴즈 - JPA의 이해

- 파일위치:  
package org.example.expert.domain.todo.controller.TodoController  
package org.example.expert.domain.todo.service.TodoService  
package org.example.expert.domain.todo.repository.TodoRepository
---
### 1. 원인

1. 기획자 요구사항 : 할 일 검색 시 weather 조건으로 검색
2. 기획자 요구사항 : 할 일 검색 시 수정일 기준 기간검색
3. 기획자 요구사항 : JPQL 사용

### 2. 해결
![img_8.png](images/img_8.png)
1. weather 와 수정일 쿼리 파라미터로 받을 수 있도록 추가
2. JPQL에서, 각 조건이 null이면 where 을 건너뛰도록 작성

## Fixed
```java
@RestController
@RequiredArgsConstructor
public class TodoController {

    // ...

    // 날씨, 수정일 조건검색 추가
    @GetMapping("/todos")
    public ResponseEntity<Page<TodoResponse>> getTodos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String weather, LocalDateTime startTime, LocalDateTime endTime
    ) {
        return ResponseEntity.ok(todoService.getTodos(page, size, weather, startTime, endTime));
    }
    
    // ...
}
```
```java
@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)  // 전역 설정 주석처리
public class TodoService {

    // ...
    
    @Transactional(readOnly = true) // 조회는 readOnry = true 설정
    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDateTime startTime, LocalDateTime endTime) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 조건검색(날씨별, 기간별)
        List<Todo> todos = todoRepository.findTodosByWeatherAndModifiedAt(weather, startTime, endTime);

        // 페이징 처리
        Page<Todo> pageTodos = new PageImpl<>(todos, pageable, todos.size());

        return pageTodos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }
    
    // ...
}
```
```java
public interface TodoRepository extends JpaRepository<Todo, Long> {
    // ...
    
    // 조건검색 JPQL (조건이 null 이면 where 을 건너뜀)
    @Query("""
        select t from Todo t left join fetch t.user u
        where (:weather is null or t.weather = :weather) 
        and (:startTime is null or t.modifiedAt >= :startTime)
        and (:endTime is null or t.modifiedAt <= :endTime)
        """)
    List<Todo> findTodosByWeatherAndModifiedAt(
            @Param("weather") String weather,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    
    //...
}
```

## Lv.4 테스트 코드 퀴즈 - 컨트롤러 테스트의 이해

파일위치:  
package org.example.expert.domain.todo.controller.TodoControllerTest

---

### 1. 원인
1. 기존 테스트 코드에서는, 에러 발생 상황에서 성공에 관한 검증을 진행함. 

### 2. 해결
1. 매서드명에 맞게 에러 상황에 대해 검증한다.

## Fixed
```java
@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

    // ...
    
    @Test
    void todo_단건_조회_시_todo가_존재하지_않아_예외가_발생한다() throws Exception {
        // given
        long todoId = 1L;

        // when
        when(todoService.getTodo(todoId))
                .thenThrow(new InvalidRequestException("Todo not found"));

        // then
        mockMvc.perform(get("/todos/{todoId}", todoId))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.message").value("Todo not found"));
    }
}
```

## Lv.5 코드 개선 퀴즈 - AOP의 이해

파일위치:  
package org.example.expert.aop.AdminAccessLoggingAspect

---

### 1. 원인
1. 기존 코드에서는, Advice가 @After 로 설정되어 있어 PointCut 의 컨트롤러 매서드 실행 후 동작하도록 설정됨.
2. AOP 매서드명에 맞는 PointCut 도 아님.

### 2. 해결
```text
2025-03-11T15:24:53.504+09:00  INFO 22044 --- [nio-8080-exec-5] o.e.expert.aop.AdminAccessLoggingAspect  : Admin Access Log - User ID: 3, Request Time: 2025-03-11T15:24:53.504117800, Request URL: /admin/users/1, Method: changeUserRole
Hibernate: select u1_0.id,u1_0.created_at,u1_0.email,u1_0.modified_at,u1_0.nickname,u1_0.password,u1_0.user_role from users u1_0 where u1_0.id=?
Hibernate: update users set email=?,modified_at=?,nickname=?,password=?,user_role=? where id=?
```
1. 요구사항은 해당 컨트롤러 실행 전 AOP 동작이니, Advice를 @Before 로 바꿈.
2. AOP 매서드명에 맞게 PointCut 수정

## Fixed
```java
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAccessLoggingAspect {

    private final HttpServletRequest request;

    // 컨트롤러 실행 전 동작해야 하므로 @After 를 @Before 로 바꿈
    // 매서드명에 맞는 PointCut 수정
    @Before("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public void logAfterChangeUserRole(JoinPoint joinPoint) {
        String userId = String.valueOf(request.getAttribute("userId"));
        String requestUrl = request.getRequestURI();
        LocalDateTime requestTime = LocalDateTime.now();

        log.info("Admin Access Log - User ID: {}, Request Time: {}, Request URL: {}, Method: {}",
                userId, requestTime, requestUrl, joinPoint.getSignature().getName());
    }
}
```

## Lv.6 JPA Cascade

파일위치:  
package org.example.expert.domain.todo.entity.Todo

---

### 1. 원인
1. 서비스 코드에서는 Todo 저장 시 작성자를 이용해 생성한 Manager 객체도 같이 저장하는 척을 함.
2. 하지만 해당 매니저 객체는 영속성 컨텍스트에 포함된 적이 없어서, 실제 Manager Table 에는 저장되지 않음.

### 2. 해결
![img_1.png](images/img_9.png)
1. 해당 매니저 객체를 영속성 컨텍스트에 포함시켜 Manager Table 에 저장하기 위해 CascadeType.PERSIST 설정.

## Fixed
```java
@Getter
@Entity
@NoArgsConstructor
@Table(name = "todos")
public class Todo extends Timestamped {

    // ...
    
    // 매니저 객체를 영속성 컨텍스트에서 관리하기위해 CascadeType.PERSIST 설정
    // Todo 를 save 할 시, managers도 같이 Persist 됨.
    @OneToMany(mappedBy = "todo", cascade = CascadeType.PERSIST)
    private List<Manager> managers = new ArrayList<>();
    
    // ...
}
```

## Lv.7 N+1

파일위치:  
package org.example.expert.domain.comment.repository.CommentRepository

---

### 1. 원인
1. getComments 서비스 매서드에서 댓글들을 조회할 때, 유저를 같이 로드하지 않아 발생하는 문제.
2. 유저가 로드된 게 없으므로, 각 댓글의 유저를 가져올 때마다 유저를 로드하는 쿼리가 발생.

### 2. 해결
```text
Hibernate: select c1_0.id,c1_0.contents,c1_0.created_at,c1_0.modified_at,c1_0.todo_id,c1_0.user_id,u1_0.id,u1_0.created_at,u1_0.email,u1_0.modified_at,u1_0.nickname,u1_0.password,u1_0.user_role from comments c1_0 join users u1_0 on u1_0.id=c1_0.user_id where c1_0.todo_id=?
```
1. 댓글들을 조회할 때 유저도 같이 한꺼번에 로드되도록 JPQL 쿼리 수정.

## Fixed
```java
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // user 도 한꺼번에 로드하기 위해 JPQL 에 join fetch 적용 
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
```

## Lv.8 QueryDSL

파일위치:  
build.gradle  
package org.example.expert.config.QueryDSLConfig  
package org.example.expert.domain.todo.repository.TodoRepository  
package org.example.expert.domain.todo.repository.TodoRepositoryQuery  
package org.example.expert.domain.todo.repository.TodoRepositoryQueryImpl  
package org.example.expert.domain.todo.service.TodoService

---

### 1. 원인
```java
public interface TodoRepository extends JpaRepository<Todo, Long> {

    // ...
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
```
1. 요구사항 : JPQL로 작성된 `findByIdWithUser`를 QueryDSL로 변경
2. N+1 문제가 발생하지 않아야 함.

### 2. 해결
1. build.gradle 에 의존성 추가
2. TodoRepositoryQuery 인터페이스 작성 (TodoRepository에 상속)
3. TodoRepositoryQueryImple 클래스 작성
4. TodoService 에 해당 매서드 적용

## Fixed
```text
    // ...

dependencies {
 
    // ...
     
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

    // ...
```
```java
@Configuration
public class QueryDSLConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(entityManager);
    }
}
```
```java
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryQuery {
}
```
```java
public interface TodoRepositoryQuery {

    Optional<Todo> findTodoByIdWithUser(Long id);
}
```
```java
@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;
    
    @Override
    public Optional<Todo> findTodoByIdWithUser(Long id) {
        Todo result = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user).fetchJoin()
                .where(todo.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}

```
```java
@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)  // 전역 설정 주석처리
public class TodoService {

    // ...
    
    @Transactional(readOnly = true) // 조회는 readOnry = true 설정
    public TodoResponse getTodo(long todoId) {
//        Todo todo = todoRepository.findByIdWithUser(todoId)
//                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
        // QueryDSL 로 변경
        Todo todo = todoRepository.findTodoByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
```

## Addition
- Lv.3 문제도 QueryDSL로 개선

```java
@RequiredArgsConstructor
public class TodoRepositoryQueryImpl implements TodoRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    // ...
    
    @Override
    public Page<TodoResponse> findTodosByWeatherAndModifiedAtWithPages(String weather, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        List<TodoResponse> result = jpaQueryFactory
                .select(
                        Projections.constructor(
                                TodoResponse.class,
                                todo.id,
                                todo.title,
                                todo.contents,
                                todo.weather,
                                Projections.constructor(UserResponse.class, user.id, user.email),
                                todo.createdAt,
                                todo.modifiedAt
                        )
                )
                .from(todo)
                .leftJoin(todo.user, user)
                .where(
                        weather != null ? todo.weather.eq(weather) : null,
                        startTime != null ? todo.modifiedAt.goe(startTime) : null,
                        endTime != null ? todo.modifiedAt.goe(endTime) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(todo.count())
                .from(todo)
                .where(
                        weather != null ? todo.weather.eq(weather) : null,
                        startTime != null ? todo.modifiedAt.goe(startTime) : null,
                        endTime != null ? todo.modifiedAt.goe(endTime) : null
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total != null ? total : 0L);
    }
}
```
```java
@Service
@RequiredArgsConstructor
//@Transactional(readOnly = true)  // 전역 설정 주석처리
public class TodoService {

    private final TodoRepository todoRepository;

    // ...

    @Transactional(readOnly = true) // 조회는 readOnry = true 설정
    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDateTime startTime, LocalDateTime endTime) {
        Pageable pageable = PageRequest.of(page - 1, size);

        // 조건검색(날씨별, 기간별)
        //List<Todo> todos = todoRepository.findTodosByWeatherAndModifiedAt(weather, startTime, endTime);

        return todoRepository.findTodosByWeatherAndModifiedAtWithPages(weather, startTime, endTime, pageable);
    }
    
    // ...
}
```