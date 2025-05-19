package com.iot.platform.service;

import java.util.List;

import com.iot.platform.entity.SensorData;

/**
 * 批处理服务接口
 */
public interface BatchProcessingService {
    
    /**
     * 添加数据到批处理队列
     */
    void addToBatch(SensorData data);
    
    /**
     * 批量处理数据
     */
    void processBatch(List<SensorData> dataList);
    
    /**
     * 获取当前批次大小
     */
    int getCurrentBatchSize();
    
    /**
     * 清空批处理队列
     */
    void clearBatch();
} 