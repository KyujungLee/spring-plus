package org.example.expert.domain.user.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.dto.response.UserSearchResponse;

import static org.example.expert.domain.user.entity.QUser.user;

@RequiredArgsConstructor
public class UserRepositoryQueryImpl implements UserRepositoryQuery{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public UserSearchResponse findNicknameByNickname(String nickname) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        UserSearchResponse.class,
                        user.nickname
                ))
                .from(user)
                .where(user.nickname.eq(nickname))
                .fetchOne();
    }
}
