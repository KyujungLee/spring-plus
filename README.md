# SPRING PLUS

## Lv.1 코드 개선 퀴즈 - @Transactional 의 이해
파일위치 :  
package org.example.expert.domain.todo.service.TodoService

### 1. 원인
(A) 에러 원문
```text
jakarta.servlet.ServletException: Request processing failed: org.springframework.orm.jpa.JpaSystemException: could not execute statement [Connection is read-only. Queries leading to data modification are not allowed] [insert into todos (contents,created_at,modified_at,title,user_id,weather) values (?,?,?,?,?,?)]
```
(B) 원인 파악 
- 데이터가 변경되어야 할 메서드까지 클래스 단계에서 전역적으로 @Transactional(readOnly = true)가 설정되어 있어, JPA Insert 문이 실행되지 않음.

### 2. 해결
1. 데이터의 변경, 조회, 삭제가 진행되는 매서드에 @Transactional 작성. (조회 매서드는 readOnly = Ture 설정)

## Before

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // <- 해당 클래스의 모든 매서드 전역설정
public class TodoService {

    /**
     * 트랜잭션 상에서 쿼리가 실행되어야 할 매서드까지 @Transactional(readOnly = true) 가 설정되어 있음.
     */
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        // 비즈니스 로직...
        Todo savedTodo = todoRepository.save(newTodo);
        // 비즈니스 로직...   
    }
    
    public Page<TodoResponse> getTodos(int page, int size) {
        // 비즈니스 로직...
        Page<Todo> todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);
        // 비즈니스 로직...
    }

    public TodoResponse getTodo(long todoId) {
        // 비즈니스 로직...
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));
        // 비즈니스 로직...
    }
}
```

## After

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
파일위치 :  
package org.example.expert.domain.user.entity.User  
package org.example.expert.domain.user.service.UserService  
package org.example.expert.domain.user.controller.UserController  
package org.example.expert.config.JwtUtil  
package org.example.expert.domain.auth.service.AuthService


### 1. 원인
1. 기획자의 요구사항 : User 정보에 nickname 추가.
2. 프론트엔드 개발자 요구사항 : JWT에서 nickname 을 꺼내 보여주길 원함.

### 2. 해결
1. User Entity에 nickname 필드 추가.
2. 컨트롤러와 서비스에 nickname 업데이트 기능을 추가하여, 기존 사용자도 문제없이 추가 할 수 있도록 수정.
3. JWT에 해당 nickname 추가. (JwtUtil, AuthService)

## Addition

```java
@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private String nickname; // nickname 필드 추가

    // ...
    
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