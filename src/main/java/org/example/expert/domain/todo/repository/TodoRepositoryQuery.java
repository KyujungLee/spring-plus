package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TodoRepositoryQuery {

    Optional<Todo> findTodoByIdWithUser(Long id);

    Page<TodoResponse> findTodosByWeatherAndModifiedAtWithPages(
            String weather, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable
    );

    Page<TodoSearchResponse> searchTodosByTitleAndCreatedAtAndManagers(
            String keywordTitle, LocalDateTime startTime, LocalDateTime endTime, String keywordNickname,
            Pageable convertPageable
    );
}
