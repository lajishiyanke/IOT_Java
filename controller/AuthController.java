package com.iot.platform.controller;

import com.iot.platform.dto.UserLoginDTO;
import com.iot.platform.dto.UserRegisterDTO;
import com.iot.platform.entity.User;
import com.iot.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "注册新用户并返回用户信息")
    @ApiResponse(responseCode = "200", description = "注册成功")
    @ApiResponse(responseCode = "400", description = "请求参数错误")
    @ApiResponse(responseCode = "409", description = "用户名已存在")
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        return ResponseEntity.ok(userService.register(registerDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verification-code")
    @Operation(summary = "发送验证码")
    public ResponseEntity<Void> sendVerificationCode(@RequestParam String email) {
        userService.sendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String email,
            @RequestParam String newPassword,
            @RequestParam String verificationCode) {
        userService.resetPassword(email, newPassword, verificationCode);
        return ResponseEntity.ok().build();
    }
} 