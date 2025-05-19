package com.iot.platform.handler;

import java.nio.charset.StandardCharsets;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import com.iot.platform.service.impl.MessageProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmqpMessageHandler {
    
    private final MessageProcessService messageProcessService;
    
    public void processMessage(Message<?> message) {
        try {
            String content = new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
            String topic = message.getHeaders().get("topic", String.class);
            
            log.info("AmqpMessageHandler 收到消息 - Topic: {}", topic);
            messageProcessService.processMessage(topic, content);
        } catch (Exception e) {
            log.error("处理AMQP消息失败: {}", e.getMessage(), e);
        }
    }
} 