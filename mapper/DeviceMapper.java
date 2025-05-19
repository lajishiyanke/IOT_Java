package com.iot.platform.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.Device;

/**
 * 设备Mapper接口
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
    @Select("SELECT * FROM devices WHERE group_id = #{groupId} ORDER BY create_time DESC")
    List<Device> selectDevicesByGroupId(Long groupId);
    
    void batchUpdateFirmware(List<Long> deviceIds, String firmwareVersion);
} 