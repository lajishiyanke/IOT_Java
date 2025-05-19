package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 传感器数据实体类
 */
@Data
@TableName("sensor_data")
public class SensorData {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("device_id")
    private Integer deviceId;
    
    @TableField("channel_id")
    private String channelId;
    
    @TableField("data_value")
    private Double dataValue;
    
    @TableField("data_unit")
    private String dataUnit;
    
    @TableField("data_type")
    private String dataType;
    
    @TableField("collect_time")
    private LocalDateTime collectTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 