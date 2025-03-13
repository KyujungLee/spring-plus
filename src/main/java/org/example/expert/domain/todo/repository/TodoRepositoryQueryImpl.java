package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.comment;
import static org.example.expert.domain.manager.entity.QManager.manager;
import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

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

    @Override
    public Page<TodoResponse> findTodosByWeatherAndModifiedAtWithPages(
            String weather, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable
    ) {
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
                        endTime != null ? todo.modifiedAt.loe(endTime) : null
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(todo.countDistinct())
                .from(todo)
                .where(
                        weather != null ? todo.weather.eq(weather) : null,
                        startTime != null ? todo.modifiedAt.goe(startTime) : null,
                        endTime != null ? todo.modifiedAt.loe(endTime) : null
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<TodoSearchResponse> searchTodosByTitleAndCreatedAtAndManagers(
            String keywordTitle, LocalDateTime startTime, LocalDateTime endTime, String keywordNickname,
            Pageable pageable
    ) {
        // 조건 생성: 제목, 생성일 범위, 담당자 닉네임 (부분 일치)
        BooleanBuilder condition = new BooleanBuilder();
        if (keywordTitle != null && !keywordTitle.isEmpty()) {
            condition.and(todo.title.contains(keywordTitle));
        }
        if (startTime != null) {
            condition.and(todo.createdAt.goe(startTime));
        }
        if (endTime != null) {
            condition.and(todo.createdAt.loe(endTime));
        }
        if (keywordNickname != null && !keywordNickname.isEmpty()) {
            // todo와 연관된 매니저의 User의 nickname 조건
            condition.and(manager.user.nickname.contains(keywordNickname));
        }

        // 실제 데이터 조회 쿼리
        List<TodoSearchResponse> content = jpaQueryFactory
                .select(Projections.constructor(
                        TodoSearchResponse.class,
                        todo.title,                                // 일정 제목
                        manager.id.countDistinct(),                // 담당자 수 (중복 제거)
                        comment.id.countDistinct()                 // 총 댓글 개수 (중복 제거)
                ))
                .from(todo)
                // 매니저와 매니저의 user join (담당자 닉네임 조건 및 담당자 수 집계)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                // 댓글 join (댓글 수 집계)
                .leftJoin(todo.comments, comment)
                .where(condition)
                // 그룹화: todo의 고유값과 선택된 컬럼만 그룹화 (기본키와 제목, 생성일)
                .groupBy(todo.id, todo.title, todo.createdAt)
                // 생성일 기준 내림차순 정렬 (최신순)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회: 조건에 맞는 Todo의 개수를 DISTINCT로 계산
        Long total = jpaQueryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
