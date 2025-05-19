package com.iot.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExportResult {
    private String taskId;
    private String status;    // PROCESSING, COMPLETED, FAILED
    private String fileUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalRows;
    private String format;
    private String errorMessage;
} 