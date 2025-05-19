package com.iot.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iot.platform.dto.AggregationResult;
import com.iot.platform.entity.SensorData;
import com.iot.platform.mapper.SensorDataMapper;
import com.iot.platform.service.DataAggregationService;
import com.iot.platform.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataAggregationServiceImpl implements DataAggregationService {

    private final SensorDataMapper sensorDataMapper;
    private final CacheService cacheService;

    @Override
    public AggregationResult getDeviceDataStats(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String aggregationType) {
        // 从缓存获取
        String cacheKey = String.format("stats:%s:%s:%s:%s:%s",
                deviceId, channelId, startTime, endTime, aggregationType);
        AggregationResult cachedResult = cacheService.getCachedObject(cacheKey, AggregationResult.class);
        if (cachedResult != null) {
            return cachedResult;
        }

        // 查询原始数据
        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq(channelId != null, "channel_id", channelId)
                .between("collect_time", startTime, endTime);
        List<SensorData> dataList = sensorDataMapper.selectList(wrapper);

        // 计算统计结果
        AggregationResult result = calculateStats(dataList, aggregationType);
        result.setDeviceId(deviceId);
        result.setChannelId(channelId);
        result.setTimeUnit(aggregationType);

        // 缓存结果
        cacheService.cacheObject(cacheKey, result, 30);  // 缓存30分钟

        return result;
    }

    @Override
    public List<AggregationResult> getHourlyStats(Long deviceId, String channelId, LocalDateTime date) {
        LocalDateTime startTime = date.toLocalDate().atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);

        // 查询数据
        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq(channelId != null, "channel_id", channelId)
                .between("collect_time", startTime, endTime);
        List<SensorData> dataList = sensorDataMapper.selectList(wrapper);

        // 按小时分组
        Map<Integer, List<SensorData>> hourlyData = dataList.stream()
                .collect(Collectors.groupingBy(data -> data.getCollectTime().getHour()));

        // 计算每小时的统计结果
        List<AggregationResult> results = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            List<SensorData> hourData = hourlyData.getOrDefault(hour, Collections.emptyList());
            if (!hourData.isEmpty()) {
                AggregationResult result = calculateStats(hourData, "HOUR");
                result.setDeviceId(deviceId);
                result.setChannelId(channelId);
                result.setTimestamp(startTime.plusHours(hour));
                results.add(result);
            }
        }

        return results;
    }

    @Override
    public List<AggregationResult> getDailyStats(Long deviceId, String channelId,
            LocalDateTime startDate, LocalDateTime endDate) {
        // 实现日统计逻辑
        return null;
    }

    @Override
    public List<AggregationResult> getMonthlyStats(Long deviceId, String channelId, int year, int month) {
        // 实现月统计逻辑
        return null;
    }

    @Override
    public Map<String, Object> getRealTimeStats(Long deviceId) {
        // 获取最近一段时间的数据进行实时统计
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5);  // 最近5分钟的数据

        QueryWrapper<SensorData> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .between("collect_time", startTime, endTime);
        List<SensorData> dataList = sensorDataMapper.selectList(wrapper);

        // 按通道分组统计
        Map<String, List<SensorData>> channelData = dataList.stream()
                .collect(Collectors.groupingBy(SensorData::getChannelId));

        Map<String, Object> result = new HashMap<>();
        channelData.forEach((channelId, data) -> {
            AggregationResult stats = calculateStats(data, "REALTIME");
            result.put(channelId, stats);
        });

        return result;
    }

    private AggregationResult calculateStats(List<SensorData> dataList, String timeUnit) {
        AggregationResult result = new AggregationResult();
        
        if (dataList.isEmpty()) {
            return result;
        }

        DoubleSummaryStatistics stats = dataList.stream()
                .mapToDouble(SensorData::getDataValue)
                .summaryStatistics();

        result.setMinValue(stats.getMin());
        result.setMaxValue(stats.getMax());
        result.setAvgValue(stats.getAverage());
        result.setSumValue(stats.getSum());
        result.setCount(stats.getCount());
        result.setTimeUnit(timeUnit);

        return result;
    }
} 