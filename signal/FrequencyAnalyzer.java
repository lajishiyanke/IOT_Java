package com.iot.platform.signal;

import java.util.Map;

import com.iot.platform.dto.SignalDTO;

/**
 * 频域分析接口
 */
public interface FrequencyAnalyzer {
    
    /**
     * 快速傅里叶变换(FFT)
     */
    Map<String, double[]> fft(SignalDTO signalDTO);
    
} 