package com.iot.platform.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.iot.platform.dto.SignalDTO;

public interface SignalAnalysisService {
    Map<String, Object> analyzeSignal(SignalDTO signal);
    Map<String, Object> processCsvFile(MultipartFile file, String imageUrl);
} 