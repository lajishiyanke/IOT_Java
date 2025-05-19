package com.iot.platform.service;

import java.util.List;

import com.iot.platform.dto.DeviceDTO;
import com.iot.platform.entity.Device;

/**
 * 设备服务接口
 */
public interface DeviceService {
    
    /**
     * 根据设备编号获取设备ID
     */
    Integer getDeviceIdByCode(String deviceCode);
    
    /**
     * 更新设备状态
     */
    void updateDeviceStatus(String deviceCode, boolean status);
    
    /**
     * 获取用户的所有设备
     */
    List<Device> getUserDevices(Long userId);
    
    /**
     * 添加设备
     */
    Device addDevice(DeviceDTO deviceDTO);
    
    /**
     * 更新设备信息
     */
    Device updateDevice(DeviceDTO deviceDTO);
    
    /**
     * 删除设备
     */
    void deleteDevice(Long deviceId);
    
    /**
     * 获取设备详情
     */
    Device getDeviceById(Integer deviceId);
    
    /**
     * 获取设备详情（包含编号）
     */
    Device getDeviceByCode(String deviceCode);


    /**
     * 获取用户的所有在线设备
     */
    List<Device> getUserOnlineDevices(Long userId);

} 