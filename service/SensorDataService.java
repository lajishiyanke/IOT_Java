package com.iot.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.SensorData;
/**
 * 传感器数据服务接口
 */
public interface SensorDataService {
    
    /**
     * 保存传感器数据
     */
    void saveSensorData(SensorData sensorData);
    
    /**
     * 批量保存传感器数据
     */
    void saveBatchSensorData(List<SensorData> sensorDataList);
    
    /**
     * 查询设备的传感器数据
     */
    List<SensorData> getDeviceSensorData(Long deviceId, String channelId, 
            LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取最新的传感器数据
     */
    SensorData getLatestSensorData(Long deviceId, String channelId);
    
    /**
     * 统计传感器数据
     */
    List<SensorData> getDeviceSensorDataStats(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String statsType);

    /**
     * 获取时序信号数据
     */
    SignalDTO getTimeSeriesData(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime);
} 