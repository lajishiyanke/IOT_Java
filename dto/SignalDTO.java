package com.iot.platform.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SignalDTO {
    private double[] times;     // 时间数组
    private double[] values;    // 幅值数组
    private double samplingRate;
    private LocalDateTime collectTime;
} 