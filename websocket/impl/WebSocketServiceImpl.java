package com.iot.platform.websocket.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.iot.platform.dto.ChartData;
import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.SensorData;
import com.iot.platform.websocket.WebSocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    
    private static final String DEVICE_DATA_TOPIC = "/topic/device/%s/data";
    private static final String DEVICE_ALARM_TOPIC = "/topic/device/%s/alarm";
    private static final String DEVICE_CHART_TOPIC = "/topic/device/%s/chart";
    private static final String DEVICE_STATUS_TOPIC = "/topic/device/%s/status";

    @Override
    public void pushRealTimeData(String deviceCode, SensorData data) {
        String destination = String.format(DEVICE_DATA_TOPIC, deviceCode);
        try {
            messagingTemplate.convertAndSend(destination, data);
            log.debug("Pushed real-time data to {}: {}", destination, data);
        } catch (Exception e) {
            log.error("Failed to push real-time data", e);
        }
    }

    @Override
    public void pushAlarmNotification(AlarmRecord alarm) {
        String deviceCode = alarm.getDeviceId().toString();
        String destination = String.format(DEVICE_ALARM_TOPIC, deviceCode);
        try {
            messagingTemplate.convertAndSend(destination, alarm);
            log.info("Pushed alarm notification to {}: {}", destination, alarm);
        } catch (Exception e) {
            log.error("Failed to push alarm notification", e);
        }
    }

    @Override
    public void pushChartUpdate(String deviceCode, ChartData chartData) {
        String destination = String.format(DEVICE_CHART_TOPIC, deviceCode);
        try {
            messagingTemplate.convertAndSend(destination, chartData);
            log.debug("Pushed chart update to {}", destination);
        } catch (Exception e) {
            log.error("Failed to push chart update", e);
        }
    }

    @Override
    public void pushDeviceStatusChange(String deviceCode, boolean status) {
        String destination = String.format(DEVICE_STATUS_TOPIC, deviceCode);
        try {
            messagingTemplate.convertAndSend(destination, status);
            log.info("Pushed status change to {}: {}", destination, status);
        } catch (Exception e) {
            log.error("Failed to push status change", e);
        }
    }

    @Override
    public void pushSignalData(String deviceCode, SignalDTO signalData) {
        String destination = String.format("/topic/device/%s/signal", deviceCode);
        try {
            messagingTemplate.convertAndSend(destination, signalData);
            log.debug("Pushed signal data to {}", destination);
        } catch (Exception e) {
            log.error("Failed to push signal data", e);
        }
    }
} 