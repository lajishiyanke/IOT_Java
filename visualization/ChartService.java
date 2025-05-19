package com.iot.platform.visualization;

import java.time.LocalDateTime;
import java.util.Map;

import com.iot.platform.dto.ChartData;

/**
 * 图表服务接口
 */
public interface ChartService {
    
    /**
     * 获取实时数据图表
     */
    ChartData getRealTimeChart(Long deviceId, String channelId);
    
    /**
     * 获取历史趋势图
     */
    ChartData getHistoryTrendChart(Long deviceId, String channelId, 
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取统计分析图
     */
    ChartData getStatisticsChart(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String type);
    
    /**
     * 获取设备状态仪表盘
     */
    Map<String, Object> getDeviceDashboard(Long deviceId);
    
    //TODO:获取告警分布饼图

    ChartData getAlarmDistributionChart(Long deviceId, LocalDateTime startTime, LocalDateTime endTime);
} 