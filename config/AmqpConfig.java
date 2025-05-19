package com.iot.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "amqp")
@Data
public class AmqpConfig {
    private String accessKey;
    private String accessSecret;
    private String consumerGroupId;
    private String iotInstanceId;
    private String clientId;
    private String host;
    private int connectionCount;
}