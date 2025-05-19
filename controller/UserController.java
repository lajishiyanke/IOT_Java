package com.iot.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.ChangePasswordDTO;
import com.iot.platform.dto.UserUpdataDTO;
import com.iot.platform.entity.User;
import com.iot.platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        user.setPassword(null); // 不返回密码
        return ResponseEntity.ok(user);
    }

    @GetMapping("/id/{userId}")
    @Operation(summary = "根据用户ID查找用户信息")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        User user = userService.findById(userId);
        user.setPassword(null); // 不返回密码
        return ResponseEntity.ok(user);
    }

    @GetMapping("/name/{username}")
    @Operation(summary = "根据用户名查找用户信息")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = userService.findByUsername(username);
        user.setPassword(null); // 不返回密码
        return ResponseEntity.ok(user);
    }   

    @GetMapping("/has/{username}")
    @Operation(summary = "根据用户名判断用户是否存在")
    public ResponseEntity<Boolean> isUserExist(@PathVariable String username) {
        Boolean isExist = userService.isUserExist(username);
        return ResponseEntity.ok(isExist);
    }

    @PutMapping("/updateByUserId/{userId}")
    @Operation(summary = "修改用户信息")
    public ResponseEntity<User> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdataDTO userUpdataDTO) {
        // 验证当前用户是否有权限更新此用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(authentication.getName());
        
        if (!currentUser.getId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        User updatedUser = userService.updateUserInfo(userId, userUpdataDTO);
        updatedUser.setPassword(null); // 不返回密码
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/check-login")
    @Operation(summary = "检查用户是否登录")
    public ResponseEntity<Boolean> checkLogin(
            @RequestHeader(value = "token", required = false) String token,
            @RequestHeader(value = "username", required = false) String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // 检查是否已认证且不是匿名用户
            boolean isLoggedIn = authentication != null && 
                                authentication.isAuthenticated() && 
                                !"anonymousUser".equals(authentication.getPrincipal());
            return ResponseEntity.ok(isLoggedIn);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @DeleteMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "token", required = false) String token,
            @RequestHeader(value = "username", required = false) String username) {
        try {
            // 获取当前认证用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String currentUsername = authentication.getName();
                userService.logout(currentUsername);
                SecurityContextHolder.clearContext();
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/change-password")
    @Operation(summary = "修改密码")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }
} 