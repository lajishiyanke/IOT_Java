package com.iot.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExportRequest {
    private Long deviceId;
    private List<String> channelIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timeUnit;  // HOUR, DAY, MONTH
    private String format;    // CSV, EXCEL, JSON
    private List<String> metrics; // MIN, MAX, AVG, SUM, COUNT
    private String email;     // 导出完成后发送通知的邮箱
} 