package com.iot.platform.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.iot.platform.entity.SensorData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SignalDataCache {
    
    @Value("${batch.processing.size:100}")
    private int batchSize;
    
    private final ConcurrentLinkedQueue<SensorData> cache = new ConcurrentLinkedQueue<>();
    private final Lock lock = new ReentrantLock();
    
    public void add(SensorData data) {
        cache.offer(data);
    }
    
    public void addAll(List<SensorData> dataList) {
        cache.addAll(dataList);
    }
    
    public List<SensorData> getBatchForProcessing() {
        List<SensorData> batch = new ArrayList<>();
        if (lock.tryLock()) {
            try {
                while (batch.size() < batchSize && !cache.isEmpty()) {
                    SensorData data = cache.poll();
                    if (data != null) {
                        batch.add(data);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        return batch;
    }
    
    public int size() {
        return cache.size();
    }
    
    public boolean shouldProcess() {
        return cache.size() >= batchSize;
    }
} 