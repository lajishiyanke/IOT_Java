package com.iot.platform.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.iot.platform.dto.DeviceDTO;
import com.iot.platform.dto.DeviceGroupDTO;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.DeviceGroup;
import com.iot.platform.entity.DeviceGroupRelation;
import com.iot.platform.entity.User;
import com.iot.platform.mapper.DeviceGroupMapper;
import com.iot.platform.mapper.DeviceGroupRelationMapper;
import com.iot.platform.mapper.DeviceMapper;
import com.iot.platform.service.DeviceManagementService;
import com.iot.platform.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private final DeviceGroupMapper groupMapper;
    private final DeviceGroupRelationMapper relationMapper;
    private final DeviceMapper deviceMapper;
    private final UserService userService;
    
    @Override
    @Transactional
    public DeviceGroup createGroup(String name, String description) {
        DeviceGroup group = new DeviceGroup();
        //TODO:获取当前用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        group.setUserId(user.getId());
        group.setName(name);
        group.setDescription(description);
        // 设置其他默认值
        group.setLevel(1);  // 默认为一级分组
        group.setPath("/");  // 默认路径
        
        return groupMapper.insert(group) > 0 ? group : null;
    }

    @Override
    @Transactional
    public DeviceGroup updateGroup(DeviceGroupDTO groupDTO) {
        DeviceGroup group = groupMapper.selectById(groupDTO.getId());
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());
        groupMapper.updateById(group);
        return group;
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {
        // 检查是否有子分组
        QueryWrapper<DeviceGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", groupId);
        if (groupMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("Cannot delete group with sub-groups");
        }
        
        // 删除分组关系
        QueryWrapper<DeviceGroupRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.eq("group_id", groupId);
        relationMapper.delete(relationWrapper);
        
        // 删除分组
        groupMapper.deleteById(groupId);
    }

    @Override
    public List<DeviceGroup> getGroupTree() {
        // 获取当前用户ID
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        Long userId = user.getId();
        
        // 只查询当前用户的分组
        QueryWrapper<DeviceGroup> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<DeviceGroup> allGroups = groupMapper.selectList(wrapper);
        
        return buildGroupTree(allGroups);
    }

    @Override
    @Transactional
    public void addDeviceToGroup(Long deviceId, Long groupId) {
        DeviceGroupRelation relation = new DeviceGroupRelation();
        relation.setDeviceId(deviceId);
        relation.setGroupId(groupId);
        relationMapper.insert(relation);
        DeviceGroup group = groupMapper.selectById(groupId);
        group.getDevices().add(deviceMapper.selectById(deviceId));
        group.setDeviceCount(group.getDevices().size());
        groupMapper.updateById(group);
    }

    @Override
    @Transactional
    public void removeDeviceFromGroup(Long deviceId, Long groupId) {
        QueryWrapper<DeviceGroupRelation> wrapper = new QueryWrapper<>();
        wrapper.eq("device_id", deviceId)
               .eq("group_id", groupId);
        relationMapper.delete(wrapper);

        DeviceGroup group = groupMapper.selectById(groupId);
        group.getDevices().remove(deviceMapper.selectById(deviceId));
        group.setDeviceCount(group.getDevices().size());
        groupMapper.updateById(group);
    }

    @Override
    public List<Device> getDevicesInGroup(Long groupId) {
        return deviceMapper.selectDevicesByGroupId(groupId);
    }

    @Override
    @Transactional
    public List<Device> batchImportDevices(List<DeviceDTO> devices) {
        List<Device> importedDevices = new ArrayList<>();
        for (DeviceDTO dto : devices) {
            Device device = convertToDevice(dto);
            deviceMapper.insert(device);
            importedDevices.add(device);
        }
        return importedDevices;
    }


    @Override
    public boolean checkDevicePermission(Long userId, Long deviceId, String operation) {
        // TODO: 实现权限检查逻辑
        // 检查用户是否拥有对设备的操作权限
        // 例如，检查用户是否是设备的所有者，或者是否在设备所属的组中
        // 返回 true 表示有权限，false 表示无权限   
        // 获取设备所属的组
        List<DeviceGroup> groups = getGroupsForDevice(deviceId);
        // 检查用户是否在设备所属的组中
        for (DeviceGroup group : groups) {
            if (group.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法
    private List<DeviceGroup> buildGroupTree(List<DeviceGroup> groups) {
        Map<Long, List<DeviceGroup>> childrenMap = groups.stream()
                .filter(group -> group.getParentId() != null)
                .collect(Collectors.groupingBy(DeviceGroup::getParentId));
                
        return groups.stream()
                .filter(group -> group.getParentId() == null)
                .peek(group -> group.setChildren(buildChildren(group.getId(), childrenMap)))
                .collect(Collectors.toList());
    }

    private List<DeviceGroup> buildChildren(Long parentId, Map<Long, List<DeviceGroup>> childrenMap) {
        List<DeviceGroup> children = childrenMap.get(parentId);
        if (children == null) {
            return Collections.emptyList();
        }
        
        children.forEach(child -> child.setChildren(buildChildren(child.getId(), childrenMap)));
        return children;
    }

    private Device convertToDevice(DeviceDTO dto) {
        // TODO: 实现DTO到实体的转换逻辑
        Device device = new Device();   
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setStatus(dto.getStatus());
        device.setGroupId(dto.getGroupId());    
        return device;
    }

    @Override
    public List<DeviceGroup> getGroupsForDevice(Long deviceId) {
        // 先查询关联关系
        QueryWrapper<DeviceGroupRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.eq("device_id", deviceId);
        List<DeviceGroupRelation> relations = relationMapper.selectList(relationWrapper);
        
        // 获取所有组ID
        List<Long> groupIds = relations.stream()
            .map(DeviceGroupRelation::getGroupId)
            .collect(Collectors.toList());
            
        // 如果没有关联的组，返回空列表
        if (groupIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 查询组信息
        return groupMapper.selectBatchIds(groupIds);
    }
    
} 