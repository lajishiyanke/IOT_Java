package com.iot.platform.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iot.platform.entity.SensorData;
import com.iot.platform.service.BatchProcessingService;
import com.iot.platform.service.SensorDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchProcessingServiceImpl implements BatchProcessingService {

    private final SensorDataService sensorDataService;
    
    @Value("${batch.processing.size}")
    private int batchSize;
    
    private final BlockingQueue<SensorData> dataQueue = new LinkedBlockingQueue<>();

    @Override
    public void addToBatch(SensorData data) {
        try {
            dataQueue.put(data);
            if (dataQueue.size() >= batchSize) {
                processBatch();
            }
        } catch (InterruptedException e) {
            log.error("Failed to add data to batch queue", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void processBatch(List<SensorData> dataList) {
        try {
            sensorDataService.saveBatchSensorData(dataList);
            log.info("Successfully processed {} records", dataList.size());
        } catch (Exception e) {
            log.error("Failed to process batch data", e);
        }
    }

    private void processBatch() {
        List<SensorData> batchList = new ArrayList<>();
        dataQueue.drainTo(batchList, batchSize);
        if (!batchList.isEmpty()) {
            processBatch(batchList);
        }
    }

    @Override
    public int getCurrentBatchSize() {
        return dataQueue.size();
    }

    @Override
    public void clearBatch() {
        dataQueue.clear();
    }
} 