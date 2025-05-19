package com.iot.platform.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.SignalFile;
import com.iot.platform.service.SignalFileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/signal")
@RequiredArgsConstructor
public class SignalController {
    
    private final SignalFileService signalFileService;
    
    @GetMapping("/files")
    public ResponseEntity<List<SignalFile>> getSignalFiles(
            @RequestParam Integer deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(signalFileService.getSignalFiles(deviceId, startTime, endTime));
    }
    
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadSignalFile(@PathVariable Long fileId) {
        SignalFile file = signalFileService.getSignalFile(fileId);
        ByteArrayResource resource = new ByteArrayResource(file.getFileData());
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getFileName() + "\"")
            .contentType(MediaType.TEXT_PLAIN)
            .contentLength(file.getFileSize())
            .body(resource);
    }

    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<SignalDTO> getLatestSignal(@PathVariable Integer deviceId) {
        SignalFile latestFile = signalFileService.getLatestSignalFile(deviceId);
        return ResponseEntity.ok(convertToSignalDTO(latestFile));
    }
    
    @GetMapping("/history")
    public ResponseEntity<List<SignalFile>> getSignalHistory(
            @RequestParam Integer deviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(signalFileService.getSignalHistory(deviceId, startTime, endTime));
    }
    
    private SignalDTO convertToSignalDTO(SignalFile file) {
        SignalDTO dto = new SignalDTO();
        dto.setSamplingRate(file.getSamplingRate());
        dto.setCollectTime(file.getCollectTime());
        
        // 解析CSV数据
        String[] lines = new String(file.getFileData()).split("\n");
        double[] times = new double[lines.length - 1];  // 减去标题行
        double[] values = new double[lines.length - 1];
        
        for (int i = 1; i < lines.length; i++) {  // 从1开始跳过标题行
            String[] parts = lines[i].split(",");
            times[i-1] = Double.parseDouble(parts[0]);
            values[i-1] = Double.parseDouble(parts[1]);
        }
        
        dto.setTimes(times);
        dto.setValues(values);
        return dto;
    }
} 