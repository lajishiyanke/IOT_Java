package com.iot.platform.service.impl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.iot.platform.dto.DeviceDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.DeviceMapper;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 设备服务实现类
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;
    private static final String CACHE_KEY = "device";
    private final UserService userService;
    
    @Override
    @Cacheable(value = "device", key = "'code:' + #deviceCode")  
    public Integer getDeviceIdByCode(String deviceCode) {
        Device device = deviceMapper.selectOne(
            new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceCode, deviceCode)
        );
        return device != null ? device.getId() : null;
    }

    @Override
    @CacheEvict(value = CACHE_KEY, key = "'code:' + #deviceCode")
    public void updateDeviceStatus(String deviceCode, boolean status) {
        UpdateWrapper<Device> wrapper = new UpdateWrapper<>();
        wrapper.eq("device_code", deviceCode)
                .set("status", status);
        deviceMapper.update(null, wrapper);
    }

    @Override
    public List<Device> getUserDevices(Long userId) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        return deviceMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Device addDevice(DeviceDTO deviceDTO) {
        Device device = new Device();
        device.setDeviceCode(deviceDTO.getDeviceCode());
        device.setDeviceName(deviceDTO.getDeviceName());
        device.setDeviceType(deviceDTO.getDeviceType());
        // 获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        device.setUserId(user.getId());
        device.setStatus(deviceDTO.getStatus());
        device.setMqttTopic(deviceDTO.getMqttTopic());
        device.setDescription(deviceDTO.getDescription());
        device.setGroupId(deviceDTO.getGroupId());  
        deviceMapper.insert(device);
        return device;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public Device updateDevice(DeviceDTO deviceDTO) {
        // 更新设备信息 先查找出device再对其赋值
        Device device = deviceMapper.selectById(deviceDTO.getId());
        device.setDeviceCode(deviceDTO.getDeviceCode());
        device.setDeviceName(deviceDTO.getDeviceName());
        device.setDeviceType(deviceDTO.getDeviceType());
        device.setStatus(deviceDTO.getStatus());
        device.setMqttTopic(deviceDTO.getMqttTopic());
        device.setDescription(deviceDTO.getDescription());
        deviceMapper.updateById(device);
        return device;
    }

    @Override
    @Transactional
    @CacheEvict(value = CACHE_KEY, allEntries = true)
    public void deleteDevice(Long deviceId) {
        deviceMapper.deleteById(deviceId);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'id:' + #deviceId")
    public Device getDeviceById(Integer deviceId) {
        return deviceMapper.selectById(deviceId);
    }

    @Override
    @Cacheable(value = CACHE_KEY, key = "'code:' + #deviceCode", unless = "#deviceCode == null")
    public Device getDeviceByCode(String deviceCode) {
        if (deviceCode == null) {
            return null;
        }
        return deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
            .eq(Device::getDeviceCode, deviceCode));
    }

    @Override
    public List<Device> getUserOnlineDevices(Long userId) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("status", true);
        return deviceMapper.selectList(wrapper);
    }
} 
