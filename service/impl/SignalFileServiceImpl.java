package com.iot.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.iot.platform.entity.SignalFile;
import com.iot.platform.mapper.SignalFileMapper;
import com.iot.platform.service.SignalFileService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "signals")
public class SignalFileServiceImpl implements SignalFileService {
    
    private final SignalFileMapper signalFileMapper;
    
    @Override
    public List<SignalFile> getSignalFiles(Integer deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return signalFileMapper.selectList(new LambdaQueryWrapper<SignalFile>()
            .eq(SignalFile::getDeviceId, deviceId)
            .between(SignalFile::getCollectTime, startTime, endTime)
            .orderByDesc(SignalFile::getCollectTime));
    }
    
    @Override
    public SignalFile getSignalFile(Long fileId) {
        return signalFileMapper.selectById(fileId);
    }

    @Cacheable(key = "'latest:' + #deviceId")
    public SignalFile getLatestSignalFile(Integer deviceId) {
        return signalFileMapper.selectList(new LambdaQueryWrapper<SignalFile>()
            .eq(SignalFile::getDeviceId, deviceId)
            .orderByDesc(SignalFile::getCollectTime)
            .last("LIMIT 1"))
            .stream()
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<SignalFile> getSignalHistory(Integer deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return signalFileMapper.selectList(new LambdaQueryWrapper<SignalFile>()
            .eq(SignalFile::getDeviceId, deviceId)
            .between(SignalFile::getCollectTime, startTime, endTime)
            .orderByDesc(SignalFile::getCollectTime));
    }
        
} 