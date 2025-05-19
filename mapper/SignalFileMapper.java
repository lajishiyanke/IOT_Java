package com.iot.platform.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iot.platform.entity.SignalFile;

@Mapper
public interface SignalFileMapper extends BaseMapper<SignalFile> {
} 