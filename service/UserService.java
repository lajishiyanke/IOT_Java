package com.iot.platform.service;

import com.iot.platform.dto.ChangePasswordDTO;
import com.iot.platform.dto.UserLoginDTO;
import com.iot.platform.dto.UserRegisterDTO;
import com.iot.platform.dto.UserUpdataDTO;
import com.iot.platform.entity.User;
/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    User register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    String login(UserLoginDTO loginDTO);
    
    /**
     * 发送验证码
     */
    void sendVerificationCode(String email);
    
    /**
     * 重置密码
     */
    void resetPassword(String email, String newPassword, String verificationCode);
    
    /**
     * 更新用户信息
     */
    User updateUserInfo(Long userId, UserUpdataDTO userUpdataDTO);
    
    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据用户名判断用户是否存在
     */
    Boolean isUserExist(String username);
    
    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 登出
     */
    void logout(String username);

    /**
     * 更改密码
     */
    void changePassword(ChangePasswordDTO changePasswordDTO);
} 