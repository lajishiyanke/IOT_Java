package com.iot.platform.service;

import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.dto.NotificationTemplate;

/**
 * 通知服务接口
 */
public interface NotificationService {
    
    /**
     * 发送告警通知
     */
    void sendAlarmNotification(AlarmRecord alarm);
    
    /**
     * 发送邮件通知
     */
    void sendEmailNotification(String to, String subject, String content);
    
    /**
     * 发送短信通知
     */
    void sendSmsNotification(String phoneNumber, String content);
    
    /**
     * 发送系统通知
     */
    void sendSystemNotification(Long userId, String title, String content, String type);
    
    /**
     * 获取通知模板
     */
    NotificationTemplate getNotificationTemplate(String templateCode);
} 