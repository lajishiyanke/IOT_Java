package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.AlarmRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常记录Mapper接口
 */
@Mapper
public interface AlarmRecordMapper extends BaseMapper<AlarmRecord> {
} 