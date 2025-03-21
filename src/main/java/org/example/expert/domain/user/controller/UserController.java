package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.dto.response.UserSearchResponse;
import org.example.expert.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PutMapping("/users")
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestBody UserChangePasswordRequest userChangePasswordRequest) {
        userService.changePassword(authUser.getUserId(), userChangePasswordRequest);
    }

    // 기존 유저도 닉네임 추가할 수 있도록 API 작성
    @PatchMapping("/users")
    public void updateNickname(@AuthenticationPrincipal AuthUser authUser, @RequestParam String nickname){
        userService.updateNickname(authUser.getUserId(), nickname);
    }

    @GetMapping("/users")
    public ResponseEntity<UserSearchResponse> searchUser(
            @RequestParam String nickname
    ){
        return ResponseEntity.ok(userService.searchUser(nickname));
    }
}
