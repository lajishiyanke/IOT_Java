package com.iot.platform.service;

import java.util.Map;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.enums.FilterType;

public interface SignalProcessService {
    /**
     * 使用Python处理信号
     */
    double[] processByPython(SignalDTO signalDTO, String scriptName, Map<String, Object> params);
    
    /**
     * 获取Python计算结果
     */
    Map<String, Object> getPythonResults(String scriptName, Map<String, Object> params);
    
    /**
     * 信号滤波
     */
    SignalDTO filter(SignalDTO signalDTO, FilterType type, double cutoffFreq, double... params);
    
    /**
     * 小波变换
     */
    Map<String, double[][]> wavelet(SignalDTO signalDTO, double[] scales);
    
    /**
     * 频谱分析
     */
    Map<String, double[]> frequency(SignalDTO signalDTO);
} 