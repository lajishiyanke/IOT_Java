package com.iot.platform.mqtt;

import java.time.LocalDateTime;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.platform.dto.SensorDataDTO;
import com.iot.platform.entity.SensorData;
import com.iot.platform.service.DeviceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MQTT消息处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mqtt", value = "enabled", havingValue = "true")
public class MqttMessageHandler implements MessageHandler {

    private final ObjectMapper objectMapper;
    private final RocketMQTemplate rocketMQTemplate;
    private final DeviceService deviceService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String topic = message.getHeaders().get("mqtt_receivedTopic", String.class);
            String payload = message.getPayload().toString();
            
            log.info("Received MQTT message - Topic: {}, Payload: {}", topic, payload);

            // 解析设备编号
            String deviceCode = parseDeviceCode(topic);
            
            // 解析传感器数据
            SensorDataDTO sensorDataDTO = objectMapper.readValue(payload, SensorDataDTO.class);
            
            // 构建传感器数据实体
            SensorData sensorData = buildSensorData(deviceCode, sensorDataDTO);
            
            // 发送数据到RocketMQ进行异步处理
            rocketMQTemplate.convertAndSend("sensor-data", sensorData);
            
            // 更新设备状态为在线
            deviceService.updateDeviceStatus(deviceCode, true);
            
        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    private String parseDeviceCode(String topic) {
        // 假设主题格式为：device/{deviceCode}/data
        String[] parts = topic.split("/");
        return parts[1];
    }

    private SensorData buildSensorData(String deviceCode, SensorDataDTO dto) {
        SensorData sensorData = new SensorData();
        sensorData.setDeviceId(deviceService.getDeviceIdByCode(deviceCode));
        sensorData.setChannelId(dto.getChannelId());
        sensorData.setDataValue(dto.getValue());
        sensorData.setDataUnit(dto.getUnit());
        sensorData.setDataType(dto.getType());
        sensorData.setCollectTime(LocalDateTime.now());
        return sensorData;
    }
} 