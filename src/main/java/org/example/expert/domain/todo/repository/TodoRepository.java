package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

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

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
