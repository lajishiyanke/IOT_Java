package com.iot.platform.config;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Configuration
@ConditionalOnProperty(prefix = "rocketmq", value = "enabled", havingValue = "true", matchIfMissing = false)
public class RocketMQConfig implements SmartLifecycle {
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.producer.group}")
    private String producerGroup;
    
    private volatile boolean running = false;
    private RocketMQTemplate rocketMQTemplate;
    
    @Bean
    public RocketMQTemplate rocketMQTemplate() throws Exception {
        this.rocketMQTemplate = new RocketMQTemplate();
        
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(nameServer);
        producer.setProducerGroup(producerGroup);
        producer.setVipChannelEnabled(false);
        producer.setRetryTimesWhenSendFailed(2);
        producer.setSendMsgTimeout(3000);
        
        rocketMQTemplate.setProducer(producer);
        rocketMQTemplate.setMessageConverter(new MappingJackson2MessageConverter());
        
        return rocketMQTemplate;
    }
    
    @Override
    public void start() {
        if (!running) {
            if (rocketMQTemplate != null) {
                try {
                    rocketMQTemplate.getProducer().start();
                    running = true;
                } catch (Exception e) {
                    // 如果RocketMQ服务未启动，记录错误但不阻止应用启动
                    running = false;
                }
            }
        }
    }
    
    @Override
    public void stop() {
        if (running) {
            if (rocketMQTemplate != null) {
                rocketMQTemplate.getProducer().shutdown();
            }
            running = false;
        }
    }
    
    @Override
    public boolean isRunning() {
        return running;
    }
} 