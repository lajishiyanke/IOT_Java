package com.iot.platform.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.iot.platform.dto.AggregationResult;
import com.iot.platform.dto.ExportRequest;
import com.iot.platform.dto.ExportResult;
import com.iot.platform.entity.SensorData;
import com.iot.platform.service.DataAggregationService;
import com.iot.platform.service.DataExportService;
import com.iot.platform.service.SensorDataService;
import com.iot.platform.util.CsvUtil;
import com.iot.platform.util.ExcelUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportServiceImpl implements DataExportService {

    private final DataAggregationService aggregationService;
    private final SensorDataService sensorDataService;
    private final CsvUtil csvUtil;
    private final ExcelUtil excelUtil;

    @Value("${export.file.path}")
    private String exportFilePath;

    private final Map<String, Integer> exportProgress = new ConcurrentHashMap<>();

    @Override
    @Async
    public ExportResult exportRawData(Long deviceId, String channelId,
            LocalDateTime startTime, LocalDateTime endTime, String format) {
        String taskId = UUID.randomUUID().toString();
        ExportResult result = new ExportResult();
        result.setTaskId(taskId);
        result.setStartTime(LocalDateTime.now());
        result.setStatus("PROCESSING");
        result.setFormat(format);

        try {
            // 获取数据
            List<SensorData> dataList = sensorDataService.getDeviceSensorData(deviceId, channelId, startTime, endTime);
            //TODO:将dataList转为double[]
            double[] data = new double[dataList.size()];
            for (int i = 0; i < dataList.size(); i++) {
                data[i] = dataList.get(i).getDataValue();
            }
            result.setTotalRows((long) dataList.size());

            // 创建导出文件路径
            String fileName = String.format("raw_data_%s_%s.%s", deviceId, taskId, format.toLowerCase());
            Path filePath = Paths.get(exportFilePath, fileName);

            // 根据格式导出数据
            switch (format.toUpperCase()) {
                case "CSV":
                    csvUtil.writeSignalData(data, new File(filePath.toString()));
                    break;
                case "EXCEL":
                    excelUtil.exportToExcel(dataList, filePath.toString());
                    break;
                case "JSON":
                    // 实现JSON导出
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }

            result.setStatus("COMPLETED");
            result.setFileUrl("/export/files/" + fileName);
            result.setEndTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Export failed", e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    @Override
    @Async
    public ExportResult exportStatsData(ExportRequest request) {
        String taskId = UUID.randomUUID().toString();
        ExportResult result = new ExportResult();
        result.setTaskId(taskId);
        result.setStartTime(LocalDateTime.now());
        result.setStatus("PROCESSING");
        result.setFormat(request.getFormat());

        try {
            // 获取统计数据
            AggregationResult statsList = aggregationService.getDeviceDataStats(
                request.getDeviceId(),
                request.getChannelIds().get(0),
                request.getStartTime(),
                request.getEndTime(),
                request.getTimeUnit()
            );

            // 创建导出文件路径
            String fileName = String.format("stats_data_%s_%s.%s",
                    request.getDeviceId(), taskId, request.getFormat().toLowerCase());
            Path filePath = Paths.get(exportFilePath, fileName);

            List<AggregationResult> statsDataList = Collections.singletonList(statsList);
            //TODO:将statsDataList转为double[]
            double[] data = new double[statsDataList.size()];
            for (int i = 0; i < statsDataList.size(); i++) {
                data[i] = statsDataList.get(i).getMaxValue();
            }

            // 根据格式导出数据
            switch (request.getFormat().toUpperCase()) {
                case "CSV":
                    csvUtil.writeSignalData(data, new File(filePath.toString()));
                    break;
                case "EXCEL":
                    excelUtil.exportToExcel(statsDataList, filePath.toString());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported format: " + request.getFormat());
            }

            result.setStatus("COMPLETED");
            result.setFileUrl("/export/files/" + fileName);
            result.setEndTime(LocalDateTime.now());

        } catch (Exception e) {
            log.error("Export failed", e);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        return result;
    }

    @Override
    public int getExportProgress(String taskId) {
        return exportProgress.getOrDefault(taskId, 0);
    }

    @Override
    public void cancelExport(String taskId) {
        // 实现取消导出任务的逻辑
        exportProgress.remove(taskId);
    }
} 