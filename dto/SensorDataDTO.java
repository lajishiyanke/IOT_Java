package com.iot.platform.dto;

import lombok.Data;

/**
 * 传感器数据DTO
 */
@Data
public class SensorDataDTO {
    private String channelId;
    private Double value;
    private String unit;
    private String type;
    private String timestamp;
} 