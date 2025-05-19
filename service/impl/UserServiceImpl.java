package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iot.platform.dto.ChangePasswordDTO;
import com.iot.platform.dto.UserLoginDTO;
import com.iot.platform.dto.UserRegisterDTO;
import com.iot.platform.dto.UserUpdataDTO;
import com.iot.platform.entity.User;
import com.iot.platform.exception.BusinessException;
import com.iot.platform.mapper.UserMapper;
import com.iot.platform.service.UserService;
import com.iot.platform.util.JwtUtil;
import com.iot.platform.util.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final RedisUtil redisUtil;
    private final JavaMailSender mailSender;
    private final AuthenticationManager authenticationManager;
    private final RBloomFilter<String> userBloomFilter;

    private static final String VERIFICATION_CODE_PREFIX = "verification:code:";
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_TOKEN_PREFIX = "user:token:";
    private static final long VERIFICATION_CODE_EXPIRE = 60; // 验证码分钟期
    private static final long USER_CACHE_EXPIRE = 30; // 30分钟
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";  // 中国大陆手机号格式

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(UserRegisterDTO registerDTO) {
        // 参数校验
        validateRegisterParams(registerDTO);

        // 验证验证码
        String cacheCode = redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + registerDTO.getEmail());
        if (!registerDTO.getVerificationCode().equals(cacheCode)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 检查用户名和邮箱是否已存在
        checkUserExists(registerDTO.getUsername(), registerDTO.getEmail());

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        user.setRoleType("USER");
        user.setIsEnabled(true);

        userMapper.insert(user);
        
        // 将用户名添加到布隆过滤器
        userBloomFilter.add(user.getUsername());
        
        // 清除验证码
        redisTemplate.delete(VERIFICATION_CODE_PREFIX + registerDTO.getEmail());
        
        log.info("New user registered: {}", user.getUsername());
        return desensitizeUser(user);
    }

    @Override
    public String login(UserLoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(), 
                    loginDTO.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtUtil.generateToken(userDetails);
            
        } catch (BadCredentialsException e) {
            throw new BusinessException("用户名或密码错误");
        } catch (DisabledException e) {
            throw new BusinessException("账号已被禁用");
        } catch (Exception e) {
            log.error("登录失败", e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    @Override
    public void sendVerificationCode(String email) {
        // 检查邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("邮箱格式不正确");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        String key = VERIFICATION_CODE_PREFIX + email;
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1546836678@qq.com");
            message.setTo(email);
            message.setSubject("验证码");
            message.setText("您的验证码是: " + code + "，有效期5分钟，请勿泄露给他人。");
            
            mailSender.send(message);
            redisTemplate.opsForValue().set(key, code, VERIFICATION_CODE_EXPIRE, TimeUnit.MINUTES);
            
            log.info("Verification code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification code to {}", email, e);
            throw new BusinessException("发送验证码失败，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String email, String newPassword, String verificationCode) {
        // 验证密码强度
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("密码长度不能小于6位");
        }

        // 验证验证码
        String cacheCode = redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + email);
        if (!verificationCode.equals(cacheCode)) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 更新密码
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        // 清除相关缓存
        clearUserCache(user);
        
        log.info("Password reset for user: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUserInfo(Long userId, UserUpdataDTO updateUser) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 只允许更新特定字段 Phone Email
        if (updateUser.getPhone() != null) {
            // 添加手机号验证
            if (!updateUser.getPhone().matches(PHONE_REGEX)) {
                throw new BusinessException("手机号格式不正确");
            }
            user.setPhone(updateUser.getPhone());
        }
        if (updateUser.getEmail() != null) {
            // 验证新邮箱是否已被使用
            if (!user.getEmail().equals(updateUser.getEmail())) {
                checkEmailExists(updateUser.getEmail());
            }
            user.setEmail(updateUser.getEmail());
        }
        if (updateUser.getRoleType() != null) {
            user.setRoleType(updateUser.getRoleType());
        }

        // 手动设置更新时间为当前时间
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        clearUserCache(user);
        
        log.info("User info updated: {}", user.getUsername());
        return desensitizeUser(user);
    }

    @Override
    public User findByUsername(String username) {
        String cacheKey = USER_CACHE_PREFIX + username;
        Object cachedUser = redisUtil.get(cacheKey);
        if (cachedUser != null) {
            return desensitizeUser((User) cachedUser);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            redisUtil.set(cacheKey, user, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
            return desensitizeUser(user);
        }

        return null;
    }

    @Override
    public Boolean isUserExist(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return userBloomFilter.contains(username);
    }

    @Override
    public User findById(Long id) {
        String cacheKey = USER_CACHE_PREFIX + "id:" + id;
        Object cachedUser = redisUtil.get(cacheKey);
        if (cachedUser != null) {
            return desensitizeUser((User) cachedUser);
        }

        User user = userMapper.selectById(id);
        if (user != null) {
            redisUtil.set(cacheKey, user, USER_CACHE_EXPIRE, TimeUnit.MINUTES);
            return desensitizeUser(user);
        }

        return null;
    }

    @Override
    public void logout(String username) {
        User user = findByUsername(username);
        if (user != null) {
            // 清除用户相关的缓存
            clearUserCache(user);
            // 清除用户的token
            redisTemplate.delete(USER_TOKEN_PREFIX + user.getId());
            log.info("User logged out: {}", username);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = findByUsername(username);
        
        // 验证原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码
        if (changePasswordDTO.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能小于6位");
        }
        
        // 验证确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 不能与原密码相同
        if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userMapper.updateById(user);
        
        // 清除用户缓存
        clearUserCache(user);
        
        // 记录日志
        log.info("User {} changed password", username);
    }
    
    // 私有辅助方法
    private void validateRegisterParams(UserRegisterDTO registerDTO) {
        if (registerDTO.getUsername() == null || registerDTO.getUsername().length() < 4) {
            throw new BusinessException("用户名长度不能小于4位");
        }
        if (registerDTO.getPassword() == null || registerDTO.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能小于6位");
        }
        if (!registerDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        // 添加手机号验证
        if (registerDTO.getPhone() != null && !registerDTO.getPhone().matches(PHONE_REGEX)) {
            throw new BusinessException("手机号格式不正确");
        }
    }

    private void checkUserExists(String username, String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username).or().eq("email", email);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名或邮箱已存在");
        }
    }

    private void checkEmailExists(String email) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("邮箱已被使用");
        }
    }

    private void clearUserCache(User user) {
        redisUtil.delete(USER_CACHE_PREFIX + user.getUsername());
        redisTemplate.delete(USER_TOKEN_PREFIX + user.getId());
    }
    
    /**
     * 对用户信息进行脱敏
     */
    private User desensitizeUser(User user) {
        if (user == null) {
            return null;
        }
        
        User desensitizedUser = new User();
        // 复制基本信息
        desensitizedUser.setId(user.getId());
        desensitizedUser.setUsername(user.getUsername());
        desensitizedUser.setRoleType(user.getRoleType());
        desensitizedUser.setIsEnabled(user.getIsEnabled());
        desensitizedUser.setCreateTime(user.getCreateTime());
        desensitizedUser.setUpdateTime(user.getUpdateTime());
        
        // 脱敏手机号
        if (user.getPhone() != null) {
            desensitizedUser.setPhone(user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        }
        
        // 脱敏邮箱
        if (user.getEmail() != null) {
            desensitizedUser.setEmail(user.getEmail().replaceAll("(\\w{3})\\w+(@\\w+\\.\\w+)", "$1****$2"));
        }
        
        return desensitizedUser;
    }
}