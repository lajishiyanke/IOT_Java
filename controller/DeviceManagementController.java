package com.iot.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.DeviceGroupDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.service.DeviceManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "设备分组管理")
@RestController
@RequestMapping("/api/device-management")
@RequiredArgsConstructor
public class DeviceManagementController {

    private final DeviceManagementService deviceManagementService;

    @PostMapping("/groups")
    @Operation(summary = "创建设备分组")
    public ResponseEntity<DeviceGroup> createGroup(String name, String description) {
        return ResponseEntity.ok(deviceManagementService.createGroup(name, description));
    }

    @PutMapping("/groups")
    @Operation(summary = "更新设备分组")
    public ResponseEntity<DeviceGroup> updateGroup(@RequestBody DeviceGroupDTO groupDTO) {
        return ResponseEntity.ok(deviceManagementService.updateGroup(groupDTO));
    }

    @DeleteMapping("/groups/{groupId}")
    @Operation(summary = "删除设备分组")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        deviceManagementService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/groups/tree")
    @Operation(summary = "获取分组树")
    public ResponseEntity<List<DeviceGroup>> getGroupTree() {
        return ResponseEntity.ok(deviceManagementService.getGroupTree());
    }

    @PostMapping("/groups/{groupId}/devices/{deviceId}")
    @Operation(summary = "添加设备到分组")
    public ResponseEntity<Void> addDeviceToGroup(@PathVariable Long deviceId, @PathVariable Long groupId) {
        deviceManagementService.addDeviceToGroup(deviceId, groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}/devices/{deviceId}")
    @Operation(summary = "从分组移除设备")
    public ResponseEntity<Void> removeDeviceFromGroup(@PathVariable Long deviceId, @PathVariable Long groupId) {
        deviceManagementService.removeDeviceFromGroup(deviceId, groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/groups/{groupId}/devices")
    @Operation(summary = "获取分组内的设备")
    public ResponseEntity<List<Device>> getDevicesInGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(deviceManagementService.getDevicesInGroup(groupId));
    }
    
}
