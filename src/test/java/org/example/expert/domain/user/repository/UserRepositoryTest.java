package org.example.expert.domain.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.expert.Config.QueryDslTestConfig;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.example.expert.domain.user.entity.QUser.user;

@DataJpaTest
//@Transactional
//@Rollback(false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslTestConfig.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;


    private static final String[] firstName = {"김", "이", "최", "박", "석", "신", "전", "조", "정", "원"};
    private static final String[] middleName = {"규", "민", "승", "연", "기", "한", "정", "영", "용", "의"};
    private static final String[] lastName = {"정", "홍", "걸", "환", "빈", "준", "용", "진", "은", "지", ""};
    private static final Random random = new Random();
    private static final int BATCH_SIZE = 10000;

    private static final String firstNickname = "신정용2130";
    private static final String middleNickname = "원용용2341";
    private static final String lastNickname = "전승홍903";


//    @Test
    private void 유저_100만건_생성_성공() {
        int totalCount = 1_000_000;
        Set<String> uniqueNicknames = new HashSet<>();
        List<User> batchUsers = new ArrayList<>();

        while (uniqueNicknames.size() < totalCount) {
            String nickname = firstName[random.nextInt(firstName.length)] +
                    middleName[random.nextInt(middleName.length)] +
                    lastName[random.nextInt(lastName.length)] +
                    random.nextInt(10000);

            if (uniqueNicknames.add(nickname)) {
                User user = new User();
                ReflectionTestUtils.setField(user, "nickname", nickname);
                batchUsers.add(user);
            }

            // Batch Insert 처리
            if (batchUsers.size() >= BATCH_SIZE) {
                userRepository.saveAll(batchUsers);
                entityManager.flush();
                entityManager.clear();
                batchUsers.clear();
            }
        }

        // 남은 데이터 저장
        if (!batchUsers.isEmpty()) {
            userRepository.saveAll(batchUsers);
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Test
    void 유저_닉네임_조회_성능_분석() {
        String targetNickname = lastNickname;
        int repeatCount = 100;

        List<Long> durationsJPA = new ArrayList<>();
        List<Long> durationsQueryDSL = new ArrayList<>();
        List<Long> durationsJPQL = new ArrayList<>();

        // 반복하여 조회 및 시간 측정
        for (int i = 0; i < repeatCount; i++) {
            entityManager.clear();

            // JPA
            long startJpa = System.currentTimeMillis();
            userRepository.findByNickname(targetNickname);
            durationsJPA.add(System.currentTimeMillis() - startJpa);

            entityManager.clear();

            // QueryDSL
            long startDsl = System.currentTimeMillis();
            jpaQueryFactory.selectFrom(user)
                    .where(user.nickname.eq(targetNickname))
                    .fetch();
            durationsQueryDSL.add(System.currentTimeMillis() - startDsl);

            entityManager.clear();

            // JPQL
            long startJPQL = System.currentTimeMillis();
            userRepository.findByNicknameJPQL(targetNickname);
            durationsJPQL.add(System.currentTimeMillis() - startJPQL);

            entityManager.clear();
        }

        // 성능 분석 결과 출력
        System.out.println("===================== 성능 측정 결과 =====================");
        printStatistics("JPA", durationsJPA);
        printStatistics("QueryDSL", durationsQueryDSL);
        printStatistics("JPQL", durationsJPQL);

        assertThat(durationsJPQL).isNotNull();
    }

    private void printStatistics(String method, List<Long> durations) {
        double average = durations.stream().mapToLong(Long::longValue).average().orElse(0);
        long max = durations.stream().mapToLong(Long::longValue).max().orElse(0);
        long min = durations.stream().mapToLong(Long::longValue).min().orElse(0);
        double stddev = calculateStdDev(durations, average);

        System.out.printf("=== [%s] 조회 시간 통계 ===%n", method);
        System.out.printf("평균: %.2fms, 최대: %dms, 최소: %dms, 표준편차: %.2fms%n", average, max, min, stddev);
    }

    private double calculateStdDev(List<Long> durations, double average) {
        double sum = 0;
        for (long duration : durations) {
            sum += Math.pow(duration - average, 2);
        }
        return Math.sqrt(sum / durations.size());
    }

    @Test
    void 유저_닉네임_조회_성능_분석_v2() {
        String targetNickname = lastNickname;
        int repeatCount = 100;

        List<Long> durationsJPA = new ArrayList<>();
        List<Long> durationsQueryDSL = new ArrayList<>();

        // 반복하여 조회 및 시간 측정
        for (int i = 0; i < repeatCount; i++) {
            entityManager.clear();

            // JPA
            long startJpa = System.currentTimeMillis();
            userRepository.findByNickname(targetNickname);
            durationsJPA.add(System.currentTimeMillis() - startJpa);

            entityManager.clear();

            // QueryDSL
            long startDsl = System.currentTimeMillis();
            jpaQueryFactory
                    .select(user.nickname)
                    .from(user)
                    .where(user.nickname.eq(targetNickname))
                    .fetch();
            durationsQueryDSL.add(System.currentTimeMillis() - startDsl);

            entityManager.clear();

        }

        // 성능 분석 결과 출력
        System.out.println("===================== 성능 측정 결과 =====================");
        printStatistics("JPA", durationsJPA);
        printStatistics("QueryDSL", durationsQueryDSL);

        assertThat(durationsJPA).isNotNull();
    }

    @Test
    void 유저_닉네임_조회_성능_분석_v3() {
        String targetNickname = lastNickname;
        int repeatCount = 100;

        List<Long> durationsQueryDSL = new ArrayList<>();
        List<Long> durationsJPQL = new ArrayList<>();

        // 반복하여 조회 및 시간 측정
        for (int i = 0; i < repeatCount; i++) {
            entityManager.clear();

            // QueryDSL
            long startDsl = System.currentTimeMillis();
            jpaQueryFactory
                    .select(user.nickname)
                    .from(user)
                    .where(user.nickname.eq(targetNickname))
                    .fetch();
            durationsQueryDSL.add(System.currentTimeMillis() - startDsl);

            entityManager.clear();

            // JPQL
            long startJPQL = System.currentTimeMillis();
            userRepository.findByNicknameJPQL(targetNickname);
            durationsJPQL.add(System.currentTimeMillis() - startJPQL);

            entityManager.clear();
        }

        // 성능 분석 결과 출력
        System.out.println("===================== 성능 측정 결과 =====================");
        printStatistics("QueryDSL", durationsQueryDSL);
        printStatistics("JPQL", durationsJPQL);

        assertThat(durationsJPQL).isNotNull();
    }
}