package com.iot.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AggregationResult {
    private Long deviceId;
    private String channelId;
    private LocalDateTime timestamp;
    private Double minValue;
    private Double maxValue;
    private Double avgValue;
    private Double sumValue;
    private Long count;
    private String timeUnit;  // HOUR, DAY, MONTH
} 