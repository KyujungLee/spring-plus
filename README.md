# SPRING PLUS

## Lv.1 코드 개선 퀴즈 - @Transactional 의 이해
> package org.example.expert.domain.todo.service.TodoService

---
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

