package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.iot.platform.dto.NotificationTemplate;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.User;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.NotificationService;
import com.iot.platform.service.UserService;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DeviceService deviceService;
    private final UserService userService;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    private static final String NOTIFICATION_LIMIT_KEY = "notification:limit:";
    private static final long NOTIFICATION_LIMIT_PERIOD = 5; // 5分钟内最多发送一次相同类型的通知

    @Override
    public void sendAlarmNotification(AlarmRecord alarm) {
        try {
            // 获取设备信息
            Device device = deviceService.getDeviceById(alarm.getDeviceId());
            if (device == null) {
                log.error("Device not found: {}", alarm.getDeviceId());
                return;
            }

            // 获取设备所属用户
            User user = userService.findById(device.getUserId());
            if (user == null) {
                log.error("User not found: {}", device.getUserId());
                return;
            }

            // 构建通知内容
            String title = String.format("设备告警通知 - %s", device.getDeviceName());
            String content = buildAlarmNotificationContent(alarm, device);

            // 发送邮件通知
            if (user.getEmail() != null) {
                sendEmailNotification(user.getEmail(), title, content);
            }

            // 发送短信通知
            if (user.getPhone() != null) {
                sendSmsNotification(user.getPhone(), content);
            }

            // 发送系统通知
            sendSystemNotification(user.getId(), title, content, "ALARM");

        } catch (Exception e) {
            log.error("Failed to send alarm notification", e);
        }
    }

    @Override
    public void sendEmailNotification(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    @Override
    public void sendSmsNotification(String phoneNumber, String content) {
        try {
            // 配置阿里云短信服务客户端
            com.aliyun.dysmsapi20170525.Client client = createAliyunSmsClient();

            // 创建短信请求
            com.aliyun.dysmsapi20170525.models.SendSmsRequest request = 
                new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signName)
                    .setTemplateCode("SMS_TEMPLATE_CODE")
                    .setTemplateParam("{\"content\":\"" + content + "\"}");

            // 发送短信
            client.sendSms(request);
            log.info("SMS sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
        }
    }

    @Override
    public void sendSystemNotification(Long userId, String title, String content, String type) {
        try {
            // 这里可以实现系统内部通知，如站内信、WebSocket推送等
            // TODO: 实现具体系统通知逻辑
            log.info("System notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send system notification to user: {}", userId, e);
        }
    }

    @Override
    public NotificationTemplate getNotificationTemplate(String templateCode) {
        // TODO: 从数据库或缓存中获取通知模板
        return null;
    }

    private String buildAlarmNotificationContent(AlarmRecord alarm, Device device) {
        return String.format(
            "设备【%s】发生告警\n" +
            "时间：%s",
            device.getDeviceName(),
            LocalDateTime.now()
        );
    }

    private boolean isNotificationLimited(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void setNotificationLimit(String key) {
        redisTemplate.opsForValue().set(key, "1", NOTIFICATION_LIMIT_PERIOD, TimeUnit.MINUTES);
    }

    private com.aliyun.dysmsapi20170525.Client createAliyunSmsClient() throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
            .setEndpoint("dysmsapi.aliyuncs.com");
        return new com.aliyun.dysmsapi20170525.Client(config);
    } 
} 