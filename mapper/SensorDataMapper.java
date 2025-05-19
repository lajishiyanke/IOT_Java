package com.iot.platform.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SensorData;
/**
 * 传感器数据Mapper接口
 */
@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {
    @Insert("<script>" +
            "INSERT INTO sensor_data (device_id, channel_id, data_value, data_unit, data_type, collect_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.deviceId}, #{item.channelId}, #{item.dataValue}, #{item.dataUnit}, #{item.dataType}, #{item.collectTime})" +
            "</foreach>" +
            "</script>")
    void insertBatch(@Param("list") List<SensorData> sensorDataList);

    @Select("""
        SELECT collect_time, data_value 
        FROM sensor_data 
        WHERE device_id = #{deviceId} 
        AND channel_id = #{channelId}
        AND collect_time BETWEEN #{startTime} AND #{endTime}
        ORDER BY collect_time ASC
    """)
    List<Map<String, Object>> getTimeSeriesData(
        @Param("deviceId") Long deviceId,
        @Param("channelId") String channelId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
} 