package com.iot.platform.dto;

import lombok.Data;

/**
 * 通知模板DTO
 */
@Data
public class NotificationTemplate {
    private String templateCode;
    private String title;
    private String content;
    private String type;
    private Boolean isEnabled;
} 