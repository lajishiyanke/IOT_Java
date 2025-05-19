package com.iot.platform.dto;
import lombok.Data;

@Data
public class DeviceDTO {
    private Long id;
    private String deviceName;
    private String deviceCode;
    private String deviceType;
    private Long groupId;       
    private String mqttTopic;
    private String description;
    private Boolean status;
    // Add other necessary fields
} 