package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserSearchResponse {

    private final String nickname;

    public UserSearchResponse(String nickname) {
        this.nickname = nickname;
    }
}
