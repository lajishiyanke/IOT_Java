package com.iot.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常规则Mapper接口
 */
@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {
} 