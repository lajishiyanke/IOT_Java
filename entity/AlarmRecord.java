package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 异常记录实体类
 */
@Data
@TableName("alarm_records")
public class AlarmRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("device_id")
    private Integer deviceId;

    @TableField("device_name")
    private String deviceName;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("channel_id")
    private String channelId;
    
    @TableField("is_handled")
    private Boolean isHandled = false;
    
    @JsonProperty(required = false)
    @TableField("handle_time")
    private LocalDateTime handleTime;
    
    @JsonProperty(required = false)
    @TableField("handle_user_id")
    private Long handleUserId;
    
    @JsonProperty(required = false)
    @TableField("handle_note")
    private String handleNote;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // @TableField("device_code")
    // private String deviceCode;
} 