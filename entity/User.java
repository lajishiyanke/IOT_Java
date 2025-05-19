package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 用户实体类
 */
@Data
@TableName("users")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    @JsonIgnore
    @TableField("password")
    private String password;
    
    private String email;
    
    private String phone;
    
    @TableField("role_type")
    private String roleType; // ADMIN, USER
    
    @TableField("is_enabled")
    private Boolean isEnabled;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    public String getPhone() {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
    
    public String getEmail() {
        if (email == null) {
            return null;
        }
        return email.replaceAll("(\\w{3})\\w+(@\\w+\\.\\w+)", "$1****$2");
    }
} 