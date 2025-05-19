package com.iot.platform.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.AlarmRecordDTO;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.AlarmRule;
import com.iot.platform.service.AlarmService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
@Tag(name = "告警管理", description = "告警相关接口")
public class AlarmController {

    private final AlarmService alarmService;

    //添加告警记录
    @PostMapping("/add")
    @Operation(summary = "添加告警")
    public ResponseEntity<Void> addAlarmRecord(@RequestBody AlarmRecordDTO alarmRecordDTO) {
        alarmService.addAlarmRecord(alarmRecordDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get")
    @Operation(summary = "查询告警记录")
    public ResponseEntity<List<AlarmRecord>> getDeviceAlarms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(alarmService.getDeviceAlarms(startTime, endTime));
    }

    @PutMapping("/{alarmId}/handle")
    @Operation(summary = "处理告警")
    public ResponseEntity<Void> handleAlarm(
            @PathVariable Long alarmId,
            @RequestParam String handleNote) {
        alarmService.handleAlarm(alarmId,handleNote);
        return ResponseEntity.ok().build();
    }

    //TODO：删除告警记录
    @DeleteMapping("/{alarmId}/delete")
    @Operation(summary = "删除告警记录")
    public ResponseEntity<Void> deleteAlarmRecord(@PathVariable Long alarmId) {
        alarmService.deleteAlarmRecord(alarmId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unhandled")
    @Operation(summary = "获取未处理的告警记录")
    public ResponseEntity<List<AlarmRecord>> getUnhandledAlarms() {
        return ResponseEntity.ok(alarmService.getUnhandledAlarms());
    }

    @PostMapping("/{deviceId}/rules/set")
    @Operation(summary = "设置告警规则")
    public ResponseEntity<Void> setAlarmRule(@PathVariable Long deviceId, @RequestBody AlarmRule rule) {
        alarmService.setAlarmRule(deviceId,rule);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{deviceId}/rules/delete")
    @Operation(summary = "删除告警规则")
    public ResponseEntity<Void> deleteAlarmRule(@PathVariable Long deviceId, @RequestBody AlarmRule rule) {
        alarmService.deleteAlarmRule(deviceId, rule.getChannelId(), rule.getRuleName());
        return ResponseEntity.ok().build();
    }
    //告警规则的查询  
    @GetMapping("/{deviceId}/rules/get")
    @Operation(summary = "获取告警规则")
    public ResponseEntity<List<AlarmRule>> getAlarmRules(@PathVariable Long deviceId) {
        return ResponseEntity.ok(alarmService.getAlarmRules(deviceId));
    }

    //告警规则的修改
    @PutMapping("/{deviceId}/rules/update")
    @Operation(summary = "修改告警规则")
    public ResponseEntity<Void> updateAlarmRule(@PathVariable Long deviceId, @RequestBody AlarmRule rule) {
        alarmService.updateAlarmRule(deviceId, rule);
        return ResponseEntity.ok().build();
    }

} 