package com.iot.platform.mq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.iot.platform.entity.SensorData;
import com.iot.platform.service.AlarmService;
import com.iot.platform.service.SensorDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 传感器数据消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "rocketmq", value = "enabled", havingValue = "true")
@RocketMQMessageListener(
    topic = "${rocketmq.consumer.topic}",
    consumerGroup = "${rocketmq.consumer.group}",
    nameServer = "${rocketmq.name-server}"
)
public class SensorDataConsumer implements RocketMQListener<SensorData> {

    private final SensorDataService sensorDataService;
    private final AlarmService alarmService;

    @Override
    public void onMessage(SensorData sensorData) {
        try {
            // 保存传感器数据
            sensorDataService.saveSensorData(sensorData);
            
            // 进行异常检测
            alarmService.detectAlarm(sensorData);
            
        } catch (Exception e) {
            log.error("Error processing sensor data", e);
        }
    }
} 