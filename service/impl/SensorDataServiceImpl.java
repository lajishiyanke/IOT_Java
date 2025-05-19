package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iot.platform.cache.SignalDataCache;
import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.SensorData;
import com.iot.platform.exception.BusinessException;
import com.iot.platform.mapper.SensorDataMapper;
import com.iot.platform.service.SensorDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 传感器数据服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SensorDataServiceImpl extends ServiceImpl<SensorDataMapper, SensorData> implements SensorDataService {

    private final SensorDataMapper sensorDataMapper;
    private final SignalDataCache signalDataCache;

    @Value("${batch.processing.size:100}")
    private int batchSize;

    @Override
    public void saveSensorData(SensorData sensorData) {
        // 添加保存日志
        log.info("Saving sensor data: deviceId={}, channelId={}, value={}, time={}", 
            sensorData.getDeviceId(), sensorData.getChannelId(), sensorData.getDataValue(), sensorData.getCollectTime());
            
        signalDataCache.add(sensorData);
        processIfNeeded();
    }

    @Override
    public void saveBatchSensorData(List<SensorData> sensorDataList) {
        signalDataCache.addAll(sensorDataList);
        processIfNeeded();
    }

    private void processIfNeeded() {
        if (signalDataCache.shouldProcess()) {
            List<SensorData> batchData = signalDataCache.getBatchForProcessing();
            if (!batchData.isEmpty()) {
                try {
                    sensorDataMapper.insertBatch(batchData);
                    log.info("Successfully processed batch of {} sensor data records", batchData.size());
                } catch (Exception e) {
                    throw new BusinessException("批量处理传感器数据失败");
                }
            }
        }
    }

    @Override
    public List<SensorData> getDeviceSensorData(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime) {
        // 添加查询参数日志
        log.info("Querying sensor data with params: deviceId={}, channelId={}, startTime={}, endTime={}", 
            deviceId, channelId, startTime, endTime);
            
        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq(channelId != null, "channel_id", channelId)
                .ge(startTime != null, "collect_time", startTime)
                .le(endTime != null, "collect_time", endTime)
                .orderByDesc("collect_time");
        List<SensorData> sensorDataList = sensorDataMapper.selectList(wrapper);
        
        // 添加结果日志
        log.info("Query result size: {}", sensorDataList.size());
        if (!sensorDataList.isEmpty()) {
            log.info("First record: deviceId={}, channelId={}, value={}, time={}", 
                sensorDataList.get(0).getDeviceId(), sensorDataList.get(0).getChannelId(), 
                sensorDataList.get(0).getDataValue(), sensorDataList.get(0).getCollectTime());
        }
        
        return sensorDataList;
    }

    @Override
    public SensorData getLatestSensorData(Long deviceId, String channelId) {
        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("channel_id", channelId)
                .orderByDesc("collect_time")
                .last("LIMIT 1");
        return sensorDataMapper.selectOne(wrapper);
    }

    @Override
    public List<SensorData> getDeviceSensorDataStats(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String statsType) {
        String groupByFormat;
        switch (statsType.toLowerCase()) {
            case "hour":
                groupByFormat = "DATE_FORMAT(collect_time, '%Y-%m-%d %H:00:00')";
                break;
            case "day":
                groupByFormat = "DATE_FORMAT(collect_time, '%Y-%m-%d')";
                break;
            case "week":
                groupByFormat = "DATE_FORMAT(collect_time, '%Y-%u')"; // %u表示周数
                break;
            case "month":
                groupByFormat = "DATE_FORMAT(collect_time, '%Y-%m')";
                break;
            default:
                throw new IllegalArgumentException("Unsupported stats type: " + statsType);
        }

        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.select(
                "device_id",
                "channel_id",
                groupByFormat + " as collect_time",
                "AVG(value) as avg_value",
                "MAX(value) as max_value",
                "MIN(value) as min_value",
                "COUNT(*) as count"
        )
        .eq("device_id", deviceId)
        .eq(channelId != null, "channel_id", channelId)
        .ge(startTime != null, "collect_time", startTime)
        .le(endTime != null, "collect_time", endTime)
        .groupBy("device_id", "channel_id", groupByFormat)
        .orderByAsc(groupByFormat);
        return sensorDataMapper.selectList(wrapper);
    }

    @Override
    public SignalDTO getTimeSeriesData(
            Long deviceId, 
            String channelId,
            LocalDateTime startTime, 
            LocalDateTime endTime) {
            
        // 获取时序数据
        List<Map<String, Object>> rawData = sensorDataMapper.getTimeSeriesData(
            deviceId, channelId, startTime, endTime);
            
        // 转换为信号数据格式
        double[] times = new double[rawData.size()];
        double[] values = new double[rawData.size()];
        
        // 获取起始时间戳（用于计算相对时间）
        LocalDateTime firstTime = startTime;
        
        for (int i = 0; i < rawData.size(); i++) {
            Map<String, Object> point = rawData.get(i);
            LocalDateTime timestamp = (LocalDateTime) point.get("collect_time");
            // 计算相对时间（毫秒）
            times[i] = ChronoUnit.MILLIS.between(firstTime, timestamp);
            values[i] = ((Number) point.get("data_value")).doubleValue();
        }
        
        SignalDTO signalDTO = new SignalDTO();
        signalDTO.setTimes(times);
        signalDTO.setValues(values);
        return signalDTO;
    }

    /**
     * 将传感器数据转换为时序信号数组
     * @param sensorDataList 传感器数据列表
     * @return 时序信号数组，每个元素代表对应时刻的幅值
     */
    public double[] convertToSignals(List<SensorData> sensorDataList) {
        // 按照采集时间排序
        sensorDataList.sort(Comparator.comparing(SensorData::getCollectTime));
        
        // 创建信号数组
        double[] signals = new double[sensorDataList.size()];
        
        // 将数据值填充到数组中
        for (int i = 0; i < sensorDataList.size(); i++) {
            signals[i] = sensorDataList.get(i).getDataValue();
        }
        
        return signals;
    }

    /**
     * 获取时间戳数组（如果需要）
     * @param sensorDataList 传感器数据列表
     * @return 时间戳数组（单位：毫秒）
     */
    public double[] getTimeArray(List<SensorData> sensorDataList) {
        // 按照采集时间排序
        sensorDataList.sort(Comparator.comparing(SensorData::getCollectTime));
        
        // 获取起始时间
        LocalDateTime startTime = sensorDataList.get(0).getCollectTime();
        
        // 创建时间数组
        double[] timeArray = new double[sensorDataList.size()];
        
        // 计算每个数据点相对于起始时间的时间差（转换为毫秒）
        for (int i = 0; i < sensorDataList.size(); i++) {
            LocalDateTime currentTime = sensorDataList.get(i).getCollectTime();
            timeArray[i] = ChronoUnit.MILLIS.between(startTime, currentTime);
        }
        
        return timeArray;
    }


} 