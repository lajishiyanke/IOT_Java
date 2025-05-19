package com.iot.platform.service;

import com.iot.platform.dto.AggregationResult;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 数据聚合统计服务接口
 */
public interface DataAggregationService {
    
    /**
     * 获取设备数据统计
     */
    AggregationResult getDeviceDataStats(Long deviceId, String channelId, 
            LocalDateTime startTime, LocalDateTime endTime, String aggregationType);
    
    /**
     * 获取设备小时统计数据
     */
    List<AggregationResult> getHourlyStats(Long deviceId, String channelId, LocalDateTime date);
    
    /**
     * 获取设备日统计数据
     */
    List<AggregationResult> getDailyStats(Long deviceId, String channelId, 
            LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取设备月统计数据
     */
    List<AggregationResult> getMonthlyStats(Long deviceId, String channelId, int year, int month);
    
    /**
     * 获取实时聚合数据
     */
    Map<String, Object> getRealTimeStats(Long deviceId);
} 