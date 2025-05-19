package com.iot.platform.service;

import com.iot.platform.dto.ExportRequest;
import com.iot.platform.dto.ExportResult;
import java.time.LocalDateTime;

/**
 * 数据导出服务接口
 */
public interface DataExportService {
    
    /**
     * 导出原始数据
     */
    ExportResult exportRawData(Long deviceId, String channelId, 
            LocalDateTime startTime, LocalDateTime endTime, String format);
    
    /**
     * 导出统计数据
     */
    ExportResult exportStatsData(ExportRequest request);
    
    /**
     * 获取导出进度
     */
    int getExportProgress(String taskId);
    
    /**
     * 取消导出任务
     */
    void cancelExport(String taskId);
} 