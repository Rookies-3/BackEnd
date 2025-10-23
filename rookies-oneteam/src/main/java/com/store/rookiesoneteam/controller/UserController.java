package com.store.rookiesoneteam.controller;

import com.store.rookiesoneteam.dto.UserDTO;
import com.store.rookiesoneteam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User API", description = "사용자 계정 관련 API (회원가입, 내 정보 관리 등)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserDTO.Response> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO.Response userInfo = userService.findUser(userDetails.getUsername());
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "내 정보 수정 (통합)", description = "현재 로그인한 사용자의 정보를 수정합니다. 현재 비밀번호는 필수이며, 변경할 필드만 선택적으로 포함하여 요청합니다.")
    @PutMapping("/me")
    public ResponseEntity<UserDTO.Response> updateMyInfo(@AuthenticationPrincipal UserDetails userDetails,
                                                       @Valid @RequestBody UserDTO.MyInfoUpdateRequest updateRequest) {
        UserDTO.Response updatedUser = userService.updateMyInfo(userDetails.getUsername(), updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다. 가입 시 상태는 'ACTIVE'가 됩니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserDTO.Response> signupUser(@Valid @RequestBody UserDTO.Request userDTO) {
        return ResponseEntity.ok(userService.signup(userDTO));
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴를 진행합니다. 탈퇴 시 상태는 'DELETED'가 됩니다.")
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@AuthenticationPrincipal UserDetails userDetails) {
        userService.withdraw(userDetails.getUsername());
        return ResponseEntity.ok("회원탈퇴가 완료되었습니다.");
    }

    // --- 관리자용 API 예시 (참고용으로 남겨둠) ---
    @Operation(summary = "모든 유저 검색 (관리자용)", description = "상태가 'ACTIVE'인 전체 사용자를 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<List<UserDTO.UpdateRequest>> getAllUsers() {
        List<UserDTO.UpdateRequest> users = userService.getAllUsers();
        return ResponseEntity.ok().body(users);
    }

    @Operation(summary = "특정 유저 검색 (관리자용)", description = "사용자 이름으로 정보를 조회합니다.")
    @GetMapping("/{username}")
    public ResponseEntity<UserDTO.Response> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.findUser(username));
    }
}
