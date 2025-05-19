package com.iot.platform.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.iot.platform.cache.SignalDataCache;
import com.iot.platform.entity.SensorData;
import com.iot.platform.mapper.SensorDataMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SignalDataProcessor {
    
    private final SignalDataCache signalDataCache;
    private final SensorDataMapper sensorDataMapper;
    
    @Scheduled(fixedDelayString = "${batch.processing.interval:5000}")
    public void processRemainingData() {
        List<SensorData> batchData = signalDataCache.getBatchForProcessing();
        if (!batchData.isEmpty()) {
            try {
                sensorDataMapper.insertBatch(batchData);
                log.info("Scheduled processing of {} sensor data records", batchData.size());
            } catch (Exception e) {
                log.error("Error in scheduled processing of sensor data", e);
            }
        }
    }
} 