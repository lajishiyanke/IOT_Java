package com.iot.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import com.iot.platform.dto.AlarmRecordDTO;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.AlarmRule;
import com.iot.platform.entity.SensorData;
/**
 * 异常检测服务接口
 */
public interface AlarmService {
    
    /**
     * 检测传感器数据是否异常
     */
    void detectAlarm(SensorData sensorData);

    /**
     * 添加告警记录
     */
    void addAlarmRecord(AlarmRecordDTO alarmRecordDTO);

    /**
     * 获取设备的异常记录
     */
    List<AlarmRecord> getDeviceAlarms(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 处理异常记录
     */
    void handleAlarm(Long alarmId, String handleNote);

    /**
     * 删除告警记录
     */
    void deleteAlarmRecord(Long alarmId);
    
    /**
     * 获取未处理的异常记录
     */
    List<AlarmRecord> getUnhandledAlarms();
    
    /**
     * 设置异常检测规则
     */
    void setAlarmRule(Long deviceId, AlarmRule rule);
    
    /**
     * 删除异常检测规则
     */
    void deleteAlarmRule(Long deviceId, String channelId, String ruleName);     

    /**
     * 修改告警规则
     */
    void updateAlarmRule(Long deviceId, AlarmRule rule);

    /**
     * 获取告警规则
     */
    List<AlarmRule> getAlarmRules(Long deviceId);       
} 
