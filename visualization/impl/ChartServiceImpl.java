package com.iot.platform.visualization.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.iot.platform.dto.AggregationResult;
import com.iot.platform.dto.ChartData;
import com.iot.platform.entity.SensorData;
import com.iot.platform.service.AlarmService;
import com.iot.platform.service.DataAggregationService;
import com.iot.platform.service.SensorDataService;
import com.iot.platform.visualization.ChartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChartServiceImpl implements ChartService {

    private final SensorDataService sensorDataService;
    private final AlarmService alarmService;
    private final DataAggregationService aggregationService;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public ChartData getRealTimeChart(Long deviceId, String channelId) {
        ChartData chartData = new ChartData();
        chartData.setTitle("实时数据监控");
        chartData.setType("line");
        
        // 获取最近的数据点
        SensorData sensorData = sensorDataService.getLatestSensorData(deviceId, channelId);
        
        // 构建图表数据
        List<String> times = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        
        times.add(TIME_FORMATTER.format(sensorData.getCollectTime()));
        values.add(sensorData.getDataValue());
        
        chartData.setXAxis(times);
        
        ChartData.Series series = new ChartData.Series();
        series.setName("实时值");
        series.setType("line");
        series.setData(new ArrayList<>(values));
        
        chartData.setSeries(Collections.singletonList(series));
        
        // 设置图表选项
        Map<String, Object> options = new HashMap<>();
        options.put("animation", true);
        options.put("showSymbol", true);
        chartData.setOptions(options);
        
        return chartData;
    }

    @Override
    public ChartData getHistoryTrendChart(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime) {
        ChartData chartData = new ChartData();
        chartData.setTitle("历史趋势分析");
        chartData.setType("line");
        
        // 获取历史数据
        List<SensorData> dataList = sensorDataService.getDeviceSensorData(deviceId, channelId, startTime, endTime);
        
        // 构建图表数据
        List<String> times = dataList.stream()
                .map(data -> DATE_FORMATTER.format(data.getCollectTime()))
                .collect(Collectors.toList());
                
        List<Double> values = dataList.stream()
                .map(data -> data.getDataValue())
                .collect(Collectors.toList());
        
        chartData.setXAxis(times);
        
        ChartData.Series series = new ChartData.Series();
        series.setName("历史数据");
        series.setType("line");
        series.setData(new ArrayList<>(values));
        
        // 添加移动平均线
        ChartData.Series maLine = new ChartData.Series();
        maLine.setName("移动平均");
        maLine.setType("line");
        List<Double> maValues = calculateMovingAverage(values, 5);
        maLine.setData(new ArrayList<>(maValues));
        
        chartData.setSeries(Arrays.asList(series, maLine));
        
        return chartData;
    }

    @Override
    public ChartData getStatisticsChart(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String type) {
        ChartData chartData = new ChartData();
        chartData.setTitle("统计分析");
        chartData.setType("bar");
        
        // 获取统计数据
        AggregationResult stats = aggregationService.getDeviceDataStats(deviceId, channelId, startTime, endTime, type);
        
        // 构建图表数据
        List<String> categories = Arrays.asList("最小值", "最大值", "平均值", "标准差");
        List<Double> values = Arrays.asList(
            stats.getMinValue(),
            stats.getMaxValue(),
            stats.getAvgValue(),
            Math.sqrt(stats.getSumValue() / stats.getCount())
        );
        
        chartData.setXAxis(categories);
        
        ChartData.Series series = new ChartData.Series();
        series.setName("统计值");
        series.setType("bar");
        series.setData(new ArrayList<>(values));
        
        chartData.setSeries(Collections.singletonList(series));
        
        return chartData;
    }

    @Override
    public Map<String, Object> getDeviceDashboard(Long deviceId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // 设备基本信息
        dashboard.put("deviceInfo", getDeviceInfo(deviceId));
        
        // 实时状态
        dashboard.put("status", getDeviceStatus(deviceId));
        
        // 告警统计
        dashboard.put("alarms", getAlarmStats(deviceId));
        
        // 性能指标
        dashboard.put("performance", getPerformanceMetrics(deviceId));
        
        return dashboard;
    }

    @Override
    public ChartData getAlarmDistributionChart(Long deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        ChartData chartData = new ChartData();
        return chartData;
    }

    // 辅助方法
    private List<Double> calculateMovingAverage(List<Double> values, int window) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            double sum = 0;
            int count = 0;
            for (int j = Math.max(0, i - window + 1); j <= i; j++) {
                sum += values.get(j);
                count++;
            }
            result.add(sum / count);
        }
        return result;
    }

    private Map<String, Object> getDeviceInfo(Long deviceId) {
        // TODO: 实现设备信息获取逻辑
        return new HashMap<>();
    }

    private Map<String, Object> getDeviceStatus(Long deviceId) {
        // TODO: 实现设备状态获取逻辑
        return new HashMap<>();
    }

    private Map<String, Object> getAlarmStats(Long deviceId) {
        // TODO: 实现告警统计逻辑
        return new HashMap<>();
    }

    private Map<String, Object> getPerformanceMetrics(Long deviceId) {
        // TODO: 实现性能指标获取逻辑
        return new HashMap<>();
    }
} 