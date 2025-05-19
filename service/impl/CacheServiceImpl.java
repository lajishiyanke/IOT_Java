package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.iot.platform.entity.SensorData;
import com.iot.platform.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SENSOR_DATA_KEY = "sensor:data:";
    private static final String DEVICE_DATA_KEY = "device:data:";
    private static final String DEVICE_STATUS_KEY = "device:status:";
    
    @Override
    public void cacheSensorData(SensorData data) {
        String key = SENSOR_DATA_KEY + data.getDeviceId() + ":" + data.getChannelId();
        redisTemplate.opsForValue().set(key, data, 30, TimeUnit.MINUTES);
    }
    
    @Override
    public SensorData getCachedSensorData(Long deviceId, String channelId) {
        String key = SENSOR_DATA_KEY + deviceId + ":" + channelId;
        return (SensorData) redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void cacheDeviceLatestData(Long deviceId, List<SensorData> dataList) {
        String key = DEVICE_DATA_KEY + deviceId;
        redisTemplate.opsForValue().set(key, dataList, 30, TimeUnit.MINUTES);
    }
    
    @Override
    public List<SensorData> getDeviceLatestData(Long deviceId) {
        String key = DEVICE_DATA_KEY + deviceId;
        return (List<SensorData>) redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void cacheDeviceStatus(Long deviceId, String status) {
        String key = DEVICE_STATUS_KEY + deviceId;
        redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
    }
    
    @Override
    public String getDeviceStatus(Long deviceId) {
        String key = DEVICE_STATUS_KEY + deviceId;
        return (String) redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void cleanExpiredData(LocalDateTime before) {
        // 这里可以实现清理过期数据的逻辑
        // 可以使用Redis的SCAN命令扫描并删除过期的键
        log.info("Cleaning expired data before: {}", before);
    }
    
    @Override
    public <T> T getCachedObject(String key, Class<T> type) {
        return (T) redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public <T> void cacheObject(String key, T object, int minutes) {
        redisTemplate.opsForValue().set(key, object, minutes, TimeUnit.MINUTES);
    }
} 