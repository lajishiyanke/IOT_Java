package com.iot.platform.mqtt;

import java.time.LocalDateTime;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.dto.SensorDataDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SensorData;
import com.iot.platform.service.BatchProcessingService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.websocket.WebSocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectionHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final DeviceService deviceService;
    private final BatchProcessingService batchProcessingService;
    private final WebSocketService webSocketService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
            String payload = message.getPayload().toString();
            
            log.info("Received data - Topic: {}, Payload: {}", topic, payload);

            // 解析设备编号
            String deviceCode = parseDeviceCode(topic);
            Device device = deviceService.getDeviceByCode(deviceCode);
            if (device == null) {
                log.error("Device not found: {}", deviceCode);
                return;
            }

            // 解析传感器数据
            SensorData sensorData = parseSensorData(payload, device.getId());
            
            // 推送实时数据
            webSocketService.pushRealTimeData(deviceCode, sensorData);
            
            // 添加到批处理队列
            batchProcessingService.addToBatch(sensorData);
            
            // 更新设备状态并推送
            deviceService.updateDeviceStatus(deviceCode, true);
            webSocketService.pushDeviceStatusChange(deviceCode, true);
            
        } catch (Exception e) {
            log.error("Error processing data collection message", e);
        }
    }

    private String parseDeviceCode(String topic) {
        // 主题格式：device/{deviceCode}/data
        String[] parts = topic.split("/");
        return parts[1];
    }

    private SensorData parseSensorData(String payload, Integer deviceId) throws Exception {
        SensorDataDTO dto = objectMapper.readValue(payload, SensorDataDTO.class);
        
        SensorData sensorData = new SensorData();
        sensorData.setDeviceId(deviceId);
        sensorData.setChannelId(dto.getChannelId());
        sensorData.setDataValue(dto.getValue());
        sensorData.setDataUnit(dto.getUnit());
        sensorData.setDataType(dto.getType());
        sensorData.setCollectTime(LocalDateTime.now());
        
        return sensorData;
    }
} 