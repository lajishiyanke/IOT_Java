package com.iot.platform.service;

import java.util.List;

import com.iot.platform.dto.DeviceDTO;
import com.iot.platform.dto.DeviceGroupDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceGroup;

public interface DeviceManagementService {
    
    /**
     * 创建设备分组
     */
    DeviceGroup createGroup(String name, String description);
    
    /**
     * 更新设备分组
     */
    DeviceGroup updateGroup(DeviceGroupDTO groupDTO);
    
    /**
     * 删除设备分组
     */
    void deleteGroup(Long groupId);
    
    /**
     * 获取分组树
     */
    List<DeviceGroup> getGroupTree();
    
    /**
     * 添加设备到分组
     */
    void addDeviceToGroup(Long deviceId, Long groupId);
    
    /**
     * 从分组移除设备
     */
    void removeDeviceFromGroup(Long deviceId, Long groupId);
    
    /**
     * 获取分组下的设备
     */
    List<Device> getDevicesInGroup(Long groupId);
    
    /**
     * 批量导入设备
     */
    List<Device> batchImportDevices(List<DeviceDTO> devices);
    
    /**
     * 设备权限检查
     */
    boolean checkDevicePermission(Long userId, Long deviceId, String operation);

    /**
     * 获取设备所属的组
     */
    List<DeviceGroup> getGroupsForDevice(Long deviceId);

} 