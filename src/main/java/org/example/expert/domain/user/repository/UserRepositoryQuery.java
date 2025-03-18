package org.example.expert.domain.user.repository;

import org.example.expert.domain.user.dto.response.UserSearchResponse;

import java.util.Optional;

public interface UserRepositoryQuery {

    UserSearchResponse findNicknameByNickname(String nickname);
}
