package org.example.expert.domain.manager.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.expert.domain.log.Log;
import org.example.expert.domain.log.LogRepository;
import org.example.expert.domain.log.LogService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.common.dto.AuthUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Transactional
@EnableJpaAuditing
@Import({ManagerService.class, LogService.class})
class ManagerServiceTest {

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
    private ManagerService managerService;

    @Autowired
    private LogRepository logRepository;

    @Test
    void 매니저_생성_실패해도_로그_남김() {
        // given
        AuthUser requestUser = new AuthUser(1L, "request@test.com", UserRole.ROLE_USER);
        Long todoId = 1L;
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);

        // when
        assertThrows(Exception.class, () -> managerService.saveManager(requestUser, todoId, managerSaveRequest));

        // then
        List<Log> logs = logRepository.findAll();
        assertThat(logs).hasSize(1);
        Log log = logs.get(0);
        assertThat(log.getRequestUserId()).isEqualTo(requestUser.getUserId());
        assertThat(log.getTargetTodoId()).isEqualTo(todoId);
        assertThat(log.getTargetUserId()).isEqualTo(managerSaveRequest.getManagerUserId());
        assertThat(log.getCreatedAt()).isNotNull();
    }
}
