package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iot.platform.dto.AlarmRecordDTO;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.AlarmRule;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SensorData;
import com.iot.platform.entity.User;
import com.iot.platform.exception.BusinessException;
import com.iot.platform.mapper.AlarmRecordMapper;
import com.iot.platform.mapper.AlarmRuleMapper;
import com.iot.platform.service.AlarmService;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.NotificationService;
import com.iot.platform.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 异常检测服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRuleMapper alarmRuleMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DeviceService deviceService;
    @Override
    @Transactional
    public void detectAlarm(SensorData sensorData) {
        // 获取该设备通道的所有告警规则
        QueryWrapper<AlarmRule> ruleWrapper = new QueryWrapper<>();
        ruleWrapper.eq("device_id", sensorData.getDeviceId())
                .eq("channel_id", sensorData.getChannelId())
                .eq("is_enabled", true);
        List<AlarmRule> rules = alarmRuleMapper.selectList(ruleWrapper);

        // 检查每个规则
        for (AlarmRule rule : rules) {
            if (isAlarmTriggered(sensorData.getDataValue(), rule)) {
                // 创建告警记录
                AlarmRecord alarm = createAlarmRecord(sensorData, rule);
                alarmRecordMapper.insert(alarm);

                // 发送告警通知
                notificationService.sendAlarmNotification(alarm);
            }
        }
    }

    @Override
    public void addAlarmRecord(AlarmRecordDTO alarmRecordDTO) {
        AlarmRecord alarmRecord = new AlarmRecord();
        alarmRecord.setDeviceId(alarmRecordDTO.getDeviceId());
        alarmRecord.setChannelId(alarmRecordDTO.getChannelId());
        Device device = deviceService.getDeviceById(alarmRecordDTO.getDeviceId());
        alarmRecord.setDeviceName(device.getDeviceName());
        // 获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        alarmRecord.setUserId(user.getId());
        alarmRecord.setIsHandled(false);
        alarmRecord.setHandleTime(null);
        alarmRecord.setHandleUserId(null);
        alarmRecord.setHandleNote(null);
        alarmRecord.setCreateTime(LocalDateTime.now());
        alarmRecord.setUpdateTime(LocalDateTime.now());
        alarmRecordMapper.insert(alarmRecord);
    }

    @Override
    public List<AlarmRecord> getDeviceAlarms(LocalDateTime startTime, LocalDateTime endTime) {
        //TODO:获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        QueryWrapper<AlarmRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .ge(startTime != null, "create_time", startTime)
                .le(endTime != null, "create_time", endTime)
                .orderByDesc("create_time");
        return alarmRecordMapper.selectList(wrapper);

    }

    @Override
    @Transactional
    public void handleAlarm(Long alarmId, String handleNote) {
        AlarmRecord alarm = alarmRecordMapper.selectById(alarmId);
        if (alarm == null) {
            throw new RuntimeException("告警记录不存在");
        }
        //TODO:获取当前用户Id
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        Long handleUserId = user.getId();
        alarm.setIsHandled(true);
        alarm.setHandleTime(LocalDateTime.now());
        alarm.setHandleUserId(handleUserId);
        alarm.setHandleNote(handleNote);

        alarmRecordMapper.updateById(alarm);
    }

    @Override
    @Transactional
    public void deleteAlarmRecord(Long alarmId) {
        // 先检查记录是否存在
        AlarmRecord alarm = alarmRecordMapper.selectById(alarmId);
        if (alarm == null) {
            throw new BusinessException("告警记录不存在");
        }
        
        // 检查当前用户是否有权限删除
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (!alarm.getUserId().equals(user.getId())) {
            throw new BusinessException("无权删除此告警记录");
        }
        
        // 执行删除
        int rows = alarmRecordMapper.deleteById(alarmId);
        if (rows == 0) {
            throw new BusinessException("删除告警记录失败");
        }
        
        log.info("告警记录已删除: {}", alarmId);
    }

    @Override
    public List<AlarmRecord> getUnhandledAlarms() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        QueryWrapper<AlarmRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("is_handled", false)
                .orderByDesc("create_time");
        return alarmRecordMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void setAlarmRule(Long deviceId, AlarmRule rule) {
        if (rule.getRuleName() == null || rule.getRuleName().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be empty");
        }
        // 检查是否已存在相同规则
        QueryWrapper<AlarmRule> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("channel_id", rule.getChannelId())
                .eq("rule_name", rule.getRuleName());
        AlarmRule existingRule = alarmRuleMapper.selectOne(wrapper);

        if (existingRule != null) {
            // 更新规则
            existingRule.setThresholdValue(rule.getThresholdValue());
            existingRule.setAlarmLevel(rule.getAlarmLevel());
            existingRule.setChannelId(rule.getChannelId());
            existingRule.setRuleName(rule.getRuleName());
            existingRule.setAlarmType(rule.getAlarmType());
            existingRule.setRuleType(rule.getRuleType());
            existingRule.setIsEnabled(true);
            alarmRuleMapper.updateById(existingRule);
        } else {
            // 创建新规则
            rule.setDeviceId(deviceId);
            rule.setRuleName(rule.getRuleName());  // 确保这个值不为空
            rule.setRuleType(rule.getRuleType());
            rule.setChannelId(rule.getChannelId());
            rule.setAlarmType(rule.getAlarmType());
            rule.setThresholdValue(rule.getThresholdValue());
            rule.setAlarmLevel(rule.getAlarmLevel());
            rule.setIsEnabled(true);
            alarmRuleMapper.insert(rule);
        }
    }

    @Override
    @Transactional
    public void deleteAlarmRule(Long deviceId, String channelId, String ruleName) {
        QueryWrapper<AlarmRule> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
                .eq("channel_id", channelId)
                .eq("rule_name", ruleName);
        alarmRuleMapper.delete(wrapper);
    }

    private boolean isAlarmTriggered(Double value, AlarmRule rule) {
        // 根据告警类型判断是否触发
        switch (rule.getAlarmType()) {
            case "THRESHOLD_UPPER":
                return value > rule.getThresholdValue();
            case "THRESHOLD_LOWER":
                return value < rule.getThresholdValue();
            case "THRESHOLD_EQUAL":
                return value.equals(rule.getThresholdValue());
            default:
                return false;
        }
    }

    private AlarmRecord createAlarmRecord(SensorData sensorData, AlarmRule rule) {
        AlarmRecord alarm = new AlarmRecord();
        alarm.setDeviceId(sensorData.getDeviceId());
        alarm.setChannelId(sensorData.getChannelId());
        alarm.setIsHandled(false);
        alarm.setHandleTime(null);
        alarm.setHandleUserId(null);  // 设为null而不是0
        alarm.setHandleNote(null);
        return alarm;
    }

    @Override
    public List<AlarmRule> getAlarmRules(Long deviceId) {
        QueryWrapper<AlarmRule> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId);
        return alarmRuleMapper.selectList(wrapper);
    }

    @Override
    public void updateAlarmRule(Long deviceId, AlarmRule rule) {
        alarmRuleMapper.updateById(rule);
    }
} 
