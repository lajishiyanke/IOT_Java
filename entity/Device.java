package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 设备实体类
 */
@Data
@TableName("devices")
public class Device {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("device_code")
    private String deviceCode;
    
    @TableField("device_name")
    private String deviceName;
    
    @TableField("device_type")
    private String deviceType;
    
    @TableField("user_id")
    private Long userId;
    
    private boolean status;
    
    @TableField("mqtt_topic")
    private String mqttTopic;
    
    private String description;

    @TableField("group_id")
    private Long groupId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 