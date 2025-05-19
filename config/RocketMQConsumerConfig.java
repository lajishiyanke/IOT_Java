package com.iot.platform.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "rocketmq", value = "enabled", havingValue = "true")
public class RocketMQConsumerConfig {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.consumer.group}")
    private String consumerGroup;

    @Value("${rocketmq.consumer.topic}")
    private String topic;

    @Bean
    public DefaultMQPushConsumer defaultMQPushConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setVipChannelEnabled(false);
        consumer.setInstanceName(consumerGroup + "_" + System.currentTimeMillis());
        
        try {
            consumer.subscribe(topic, "*");
            log.info("RocketMQ consumer started successfully");
        } catch (Exception e) {
            log.error("Failed to start RocketMQ consumer", e);
            throw e;
        }
        
        return consumer;
    }
} 