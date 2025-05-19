package com.iot.platform.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("signal_files")
public class SignalFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer deviceId;
    private String fileName;
    private Double samplingRate;
    private LocalDateTime collectTime;
    private byte[] fileData;
    private Integer fileSize;
    private Integer channelCount;
    private Integer dataPoints;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
} 