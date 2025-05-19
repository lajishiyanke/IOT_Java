package com.iot.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import com.iot.platform.entity.SignalFile;

public interface SignalFileService {
    List<SignalFile> getSignalFiles(Integer deviceId, LocalDateTime startTime, LocalDateTime endTime);
    SignalFile getSignalFile(Long fileId);
    SignalFile getLatestSignalFile(Integer deviceId);
    List<SignalFile> getSignalHistory(Integer deviceId, LocalDateTime startTime, LocalDateTime endTime);
    
} 