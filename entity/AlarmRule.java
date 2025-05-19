package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 异常检测规则实体类
 */
@Data
@TableName("alarm_rules")
public class AlarmRule {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("device_id")
    private Long deviceId;
    
    @TableField("channel_id")
    private String channelId;
    
    @TableField("alarm_type")
    private String alarmType;
    
    @TableField("rule_type")
    private String ruleType;

    @TableField("rule_name")
    private String ruleName;
    
    @TableField("threshold_value")
    private Double thresholdValue;
    
    @TableField("alarm_level")
    private String alarmLevel;
    
    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 