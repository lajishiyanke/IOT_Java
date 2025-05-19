package com.iot.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.DeviceDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.User;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "设备相关接口")
public class DeviceController {

    private final DeviceService deviceService;

    private final UserService userService;

    @GetMapping
    @Operation(summary = "获取用户的所有设备")
    public ResponseEntity<List<Device>> getUserDevices() {
        // 从SecurityContext获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        return ResponseEntity.ok(deviceService.getUserDevices(userId));
    }

    @PostMapping
    @Operation(summary = "添加设备")
    public ResponseEntity<Device> addDevice(@RequestBody DeviceDTO deviceDTO) {
        return ResponseEntity.ok(deviceService.addDevice(deviceDTO));
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备信息")
    public ResponseEntity<Device> updateDevice(
            @PathVariable Long deviceId,
            @RequestBody DeviceDTO deviceDTO) {
        deviceDTO.setId(deviceId);
        return ResponseEntity.ok(deviceService.updateDevice(deviceDTO));
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备详情")
    public ResponseEntity<Device> getDevice(@PathVariable Integer deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceById(deviceId));
    }

    @GetMapping("/online")
    @Operation(summary = "获取用户的所有在线设备")
    public ResponseEntity<List<Device>> getUserOnlineDevices() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        return ResponseEntity.ok(deviceService.getUserOnlineDevices(userId));
    }


} 