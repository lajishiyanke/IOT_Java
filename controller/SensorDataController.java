package com.iot.platform.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SensorData;
import com.iot.platform.entity.User;
import com.iot.platform.security.SecurityUtils;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.SensorDataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sensor-data")
@RequiredArgsConstructor
@Tag(name = "传感器数据", description = "传感器数据相关接口")
public class SensorDataController {

    private final SensorDataService sensorDataService;
    private final DeviceService deviceService;

    @PostMapping("/save")
    @Operation(summary = "保存传感器数据")
    public ResponseEntity<Void> saveSensorData(@RequestBody SensorData sensorData) {
        sensorDataService.saveSensorData(sensorData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save-batch")
    @Operation(summary = "批量保存传感器数据")
    public ResponseEntity<Void> saveBatchSensorData(@RequestBody List<SensorData> sensorDataList) {
        sensorDataService.saveBatchSensorData(sensorDataList);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备的传感器数据")
    public ResponseEntity<List<SensorData>> getDeviceSensorData(
            @PathVariable Long deviceId,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(sensorDataService.getDeviceSensorData(deviceId, channelId, startTime, endTime));
    }

    @GetMapping("/device/{deviceId}/latest")
    @Operation(summary = "获取最新的传感器数据")
    public ResponseEntity<SensorData> getLatestSensorData(
            @PathVariable Long deviceId,
            @RequestParam String channelId) {
        return ResponseEntity.ok(sensorDataService.getLatestSensorData(deviceId, channelId));
    }

    @GetMapping("/device/{deviceId}/stats")
    @Operation(summary = "获取设备的传感器数据统计")
    public ResponseEntity<List<SensorData>> getDeviceSensorDataStats(
            @PathVariable Long deviceId,
            @RequestParam String channelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam String statsType) {
        return ResponseEntity.ok(sensorDataService.getDeviceSensorDataStats(deviceId, channelId, startTime, endTime, statsType));
    }

    @GetMapping("/user-sensor-data")
    @Operation(summary = "获取当前用户的传感器数据")
    public ResponseEntity<List<SensorData>> getUserSensorData(
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();
        
        // 获取用户的所有设备
        List<Device> userDevices = deviceService.getUserDevices(currentUser.getId());
        
        // 获取所有设备的传感器数据
        List<SensorData> allSensorData = new ArrayList<>();
        for (Device device : userDevices) {
            List<SensorData> deviceData = sensorDataService.getDeviceSensorData(
                device.getId().longValue(),
                channelId,
                startTime,
                endTime
            );
            allSensorData.addAll(deviceData);
        }
        
        return ResponseEntity.ok(allSensorData);
    }
    

    @GetMapping("/timeseries")
    @Operation(summary = "获取时序信号数据")
    public ResponseEntity<SignalDTO> getTimeSeriesData(
            @RequestParam Long deviceId,
            @RequestParam String channelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
            
        SignalDTO signalDTO = sensorDataService.getTimeSeriesData(
            deviceId, channelId, startTime, endTime);
            
        return ResponseEntity.ok(signalDTO);
    }
} 