package com.iot.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import com.iot.platform.entity.SensorData;

/**
 * 缓存服务接口
 */
public interface CacheService {
    
    /**
     * 缓存最新的传感器数据
     */
    void cacheSensorData(SensorData data);
    
    /**
     * 获取缓存的传感器数据
     */
    SensorData getCachedSensorData(Long deviceId, String channelId);
    
    /**
     * 缓存设备最新数据列表
     */
    void cacheDeviceLatestData(Long deviceId, List<SensorData> dataList);
    
    /**
     * 获取设备最新数据列表
     */
    List<SensorData> getDeviceLatestData(Long deviceId);
    
    /**
     * 缓存设备状态
     */
    void cacheDeviceStatus(Long deviceId, String status);
    
    /**
     * 获取设备状态
     */
    String getDeviceStatus(Long deviceId);
    
    /**
     * 清除过期数据
     */
    void cleanExpiredData(LocalDateTime before);
    
    /**
     * 获取缓存对象
     */
    <T> T getCachedObject(String key, Class<T> type);
    
    /**
     * 缓存对象
     */
    <T> void cacheObject(String key, T object, int minutes);
} 