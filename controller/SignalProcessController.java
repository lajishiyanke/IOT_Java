package com.iot.platform.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.enums.FilterType;
import com.iot.platform.service.SignalAnalysisService;
import com.iot.platform.signal.ExternalProcessor;
import com.iot.platform.signal.FrequencyAnalyzer;
import com.iot.platform.signal.SignalFilter;
import com.iot.platform.signal.WaveletTransformer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/signal")
@RequiredArgsConstructor
@Tag(name = "信号处理", description = "信号处理相关接口")
public class SignalProcessController {

    private final ExternalProcessor externalProcessor;
    private final SignalFilter signalFilter;
    private final WaveletTransformer waveletTransformer;
    private final FrequencyAnalyzer frequencyAnalyzer;
    private final SignalAnalysisService signalAnalysisService;

    @PostMapping("/python/process")
    @Operation(summary = "使用Python处理信号")
    public ResponseEntity<double[]> processByPython(
            @RequestBody SignalDTO signalDTO,
            @RequestParam String scriptName,
            @RequestBody(required = false) Map<String, Object> params) {
        log.debug("Processing signals with Python, script: {}", scriptName);
        double[] results = externalProcessor.processByPython(signalDTO.getValues(), scriptName, params);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/python/results")
    @Operation(summary = "获取Python计算结果")
    public ResponseEntity<Map<String, Object>> getPythonResults(
            @RequestParam String scriptName,
            @RequestBody(required = false) Map<String, Object> params) {
        log.debug("Getting Python results, script: {}", scriptName);
        Map<String, Object> results = externalProcessor.getPythonResults(scriptName, params);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/filter")
    @Operation(summary = "信号滤波")
    public ResponseEntity<SignalDTO> filter(
            @RequestBody SignalDTO signalDTO,
            @RequestParam FilterType type,
            @RequestParam double cutoffFreq,
            @RequestBody(required = false) double... params) {
        double[] results = signalFilter.filter(signalDTO, type, cutoffFreq, params);
        SignalDTO response = new SignalDTO();
        response.setTimes(signalDTO.getTimes());
        response.setValues(results);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/wavelet")
    @Operation(summary = "小波变换")
    public ResponseEntity<Map<String, double[][]>> wavelet(@RequestBody WaveletRequest request) {
        Map<String, double[][]> results = waveletTransformer.transform(request.getSignal(), request.getScales());
        return ResponseEntity.ok(results);
    }

    @PostMapping("/frequency")
    @Operation(summary = "频谱分析")
    public ResponseEntity<Map<String, double[]>> frequency(@RequestBody SignalDTO signalDTO) {
        Map<String, double[]> results = frequencyAnalyzer.fft(signalDTO);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/predict")
    @Operation(summary = "信号预测")
    public ResponseEntity<Map<String, Object>> predict(@RequestBody SignalDTO signalDTO) {
        try {
            // 设置预测参数
            Map<String, Object> params = new HashMap<>();
            // 修改模型路径，使用相对路径
            params.put("model_path", "scripts/python/models/model.pth");
            
            log.info("开始预测，信号长度: {}", signalDTO.getValues().length);
            
            // 调用Python进行预测
            double[] predictions = externalProcessor.processByPython(
                signalDTO.getValues(),
                "predict.py",           
                params                  
            );
            
            log.info("预测完成，结果: {}", Arrays.toString(predictions));
            
            // 封装预测结果
            Map<String, Object> result = new HashMap<>();
            result.put("x", predictions[0]);     
            result.put("y", predictions[1]);     
            result.put("size", predictions[2]);  
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("预测失败", e);
            // 返回更详细的错误信息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("details", e.getClass().getName());
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
            }
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/process-csv")
    @Operation(summary = "处理CSV文件并展示图片")
    public ResponseEntity<Map<String, Object>> processCsvFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("imageUrl") String imageUrl) {
        try {
            Map<String, Object> result = signalAnalysisService.processCsvFile(file, imageUrl);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("处理CSV文件失败", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Data
    public static class WaveletRequest {
        private SignalDTO signal;
        private double[] scales;
    }
} 