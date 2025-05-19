package com.iot.platform.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.iot.platform.entity.User;
import com.iot.platform.exception.BusinessException;
import com.iot.platform.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityUtils {

    private static UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        SecurityUtils.userService = userService;
    }

    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Current authentication: {}", authentication);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("用户未登录");
        }
        
        Object principal = authentication.getPrincipal();
        log.debug("Principal type: {}", principal.getClass().getName());
        
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            return userService.findByUsername(username);
        } else if (principal instanceof User) {
            return (User) principal;
        }
        
        log.error("Unexpected principal type: {}", principal.getClass().getName());
        throw new BusinessException("无法获取当前用户信息");
    }
    
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
} 