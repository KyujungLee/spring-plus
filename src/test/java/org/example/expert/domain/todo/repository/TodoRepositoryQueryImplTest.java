package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@EnableJpaAuditing
class TodoRepositoryQueryImplTest {

    @TestConfiguration
    static class QueryDslTestConfig {
        @PersistenceContext
        private EntityManager entityManager;

        @Bean
        public JPAQueryFactory jpaQueryFactory() {
            return new JPAQueryFactory(entityManager);
        }
    }

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    private void setData(){
        User user = new User("test@example.com", "password", UserRole.ROLE_USER, "managerNickname");
        userRepository.save(user);

        Todo todo1 = new Todo("Sunny Task", "Contents 1", "Sunny", user);
        ReflectionTestUtils.setField(todo1, "createdAt", LocalDateTime.now().plusSeconds(0));
        Todo todo2 = new Todo("Cloudy Task", "Contents 2", "Cloudy", user);
        ReflectionTestUtils.setField(todo2, "createdAt", LocalDateTime.now().plusSeconds(1));
        Todo todo3 = new Todo("Rainy Task", "Contents 3", "Rainy", user);
        ReflectionTestUtils.setField(todo3, "createdAt", LocalDateTime.now().plusSeconds(2));
        Todo todo4 = new Todo("Another Sunny Task", "Contents 4", "Sunny", user);
        ReflectionTestUtils.setField(todo4, "createdAt", LocalDateTime.now().plusSeconds(3));

        todoRepository.save(todo1);
        todoRepository.save(todo2);
        todoRepository.save(todo3);
        todoRepository.save(todo4);

        Comment comment1 = new Comment("Contents 1", user, todo1);
        Comment comment2 = new Comment("Contents 2", user, todo1);
        Comment comment3 = new Comment("Contents 3", user, todo1);

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
    }

    @Test
    void findTodosByWeatherAndModifiedAtWithPages() {
        // given
        setData();

        String weather = "Sunny";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<TodoResponse> result = todoRepository.findTodosByWeatherAndModifiedAtWithPages(
                weather, startTime, endTime, pageable
        );

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
        List<TodoResponse> responses = result.getContent();
        responses.forEach(response -> assertThat(response.getWeather()).isEqualTo("Sunny"));
    }

    @Test
    void searchTodosByTitleAndCreatedAtAndManagers() {
        // given
        setData();

        String keywordTitle = "Task";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);
        String keywordNickname = "managerNickname";
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<TodoSearchResponse> page = todoRepository.searchTodosByTitleAndCreatedAtAndManagers(
                keywordTitle, startTime, endTime, keywordNickname, pageable
        );

        // then
        assertThat(page.getTotalElements()).isEqualTo(4);
        List<TodoSearchResponse> responses = page.getContent();
        responses.forEach(response -> {
            assertThat(response.getTitle()).contains("Task");
            assertThat(response.getCountManagers()).isEqualTo(1);
        });
        assertThat(responses.get(3).getCountComments()).isEqualTo(3);
    }
}