package com.iot.platform.service.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.codec.binary.Base64;
import org.apache.qpid.jms.JmsConnection;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.JmsConnectionListener;
import org.apache.qpid.jms.message.JmsInboundMessageDispatch;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.iot.platform.config.AmqpConfig;
import com.iot.platform.handler.AmqpMessageHandler;
import com.iot.platform.websocket.WebSocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class AmqpServiceImpl implements InitializingBean {
    
    @Autowired
    private AmqpConfig amqpConfig;
    
    private final List<Connection> connections = new ArrayList<>();
    
    @Autowired
    private MessageProcessService messageProcessService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    private final ExecutorService executorService = new ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors() * 2, 
        60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>(50000)
    );

    private final AmqpMessageHandler amqpMessageHandler;

    @Value("${amqp.connectionCount}")
    private int connectionCount;

    @Override
    public void afterPropertiesSet() throws Exception {
        createConnection(0);
    }

    private void initConnections() throws Exception {
        for (int i = 0; i < amqpConfig.getConnectionCount(); i++) {
            createConnection(i);
        }
    }

    private void createConnection(int index) throws Exception {
        try {
            // 1. 生成认证信息
            long timestamp = System.currentTimeMillis();
            String signMethod = "hmacsha1";
            
            // 2. 构建username
            String userName = String.format("%s-%d|authMode=aksign,signMethod=%s,timestamp=%d,authId=%s,iotInstanceId=%s,consumerGroupId=%s|",
                amqpConfig.getClientId(),
                index,
                signMethod,
                timestamp,
                amqpConfig.getAccessKey(),
                amqpConfig.getIotInstanceId(),
                amqpConfig.getConsumerGroupId()
            );
            
            // 3. 构建待签名字符串
            String signContent = "authId=" + amqpConfig.getAccessKey() + "&timestamp=" + timestamp;
            String password = doSign(signContent, amqpConfig.getAccessSecret(), signMethod);

            // 4. 修改为 amqp 协议
            String connectionUrl = String.format(
                "failover:(amqps://%s:5671?" +
                "amqp.idleTimeout=80000" +
                "&amqp.saslMechanisms=PLAIN" +
                "&jms.prefetchPolicy.all=100)" +
                "?failover.maxReconnectAttempts=10" +
                "&failover.startupMaxReconnectAttempts=10" +
                "&failover.reconnectDelay=3000",
                amqpConfig.getHost()
            );

            log.info("Connecting with URL: {}", connectionUrl);
            log.info("Username: {}", userName);

            // 5. 创建连接工厂
            JmsConnectionFactory factory = new JmsConnectionFactory();
            factory.setRemoteURI(connectionUrl);
            factory.setUsername(userName);
            factory.setPassword(password);
            
            // 6. 创建连接
            Connection connection = factory.createConnection();
            connections.add(connection);
            
            // 7. 添加连接监听器
            ((JmsConnection)connection).addConnectionListener(createConnectionListener());
            
            // 8. 创建会话和消费者
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.start();
            
            // 创建队列
            String queueName = String.format("/%s/%s/%s", 
                amqpConfig.getIotInstanceId(),
                amqpConfig.getConsumerGroupId(),
                amqpConfig.getClientId() + "-" + index
                );
            Destination queue = session.createQueue(queueName);
            MessageConsumer consumer = session.createConsumer(queue);
            consumer.setMessageListener(createMessageListener());
            
            log.info("Successfully created AMQP connection {}", index);
            
        } catch (Exception e) {
            log.error("Failed to create connection {}: {}", index, e.getMessage());
            throw e;
        }
    }

    private MessageListener createMessageListener() {
        return message -> {
            try {
                executorService.submit(() -> processMessage(message));
            } catch (Exception e) {
                log.error("Submit task error", e);
            }
        };
    }

    private void processMessage(Message message) {
        try {
            byte[] body = message.getBody(byte[].class);
            String content = new String(body);
            String topic = message.getStringProperty("topic");
            String messageId = message.getStringProperty("messageId");
            long generateTime = message.getLongProperty("generateTime");
            
            log.info("Received message: topic={}, messageId={}, generateTime={}, content={}",
                    topic, messageId, generateTime, content);

            // 直接处理消息
            messageProcessService.processMessage(topic, content);
            
        } catch (Exception e) {
            log.error("Process message error", e);
        }
    }

    private JmsConnectionListener createConnectionListener() {
        return new JmsConnectionListener() {
            @Override
            public void onConnectionEstablished(URI remoteURI) {
                log.info("onConnectionEstablished, remoteUri:{}", remoteURI);
            }

            @Override
            public void onConnectionFailure(Throwable error) {
                log.error("onConnectionFailure, {}", error.getMessage());
            }

            @Override
            public void onConnectionInterrupted(URI remoteURI) {
                log.info("onConnectionInterrupted, remoteUri:{}", remoteURI);
            }

            @Override
            public void onConnectionRestored(URI remoteURI) {
                log.info("onConnectionRestored, remoteUri:{}", remoteURI);
            }

            @Override
            public void onInboundMessage(JmsInboundMessageDispatch envelope) {}

            @Override
            public void onSessionClosed(Session session, Throwable cause) {}

            @Override
            public void onConsumerClosed(MessageConsumer consumer, Throwable cause) {}

            @Override
            public void onProducerClosed(MessageProducer producer, Throwable cause) {}
        };
    }

    private String doSign(String content, String key, String signMethod) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), signMethod);
        Mac mac = Mac.getInstance(signMethod);
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(content.getBytes());
        return Base64.encodeBase64String(rawHmac);
    }

    @PreDestroy
    public void destroy() {
        connections.forEach(connection -> {
            try {
                connection.close();
            } catch (JMSException e) {
                log.error("Close connection error", e);
            }
        });

        executorService.shutdown();
        try {
            if (executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.info("Shutdown success");
            } else {
                log.warn("Failed to handle messages");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Shutdown interrupted", e);
        }
    }
}