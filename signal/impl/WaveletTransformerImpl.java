package com.iot.platform.signal.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.signal.WaveletTransformer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WaveletTransformerImpl implements WaveletTransformer {
    
    private static final double PI = Math.PI;
    
    // Morlet小波的中心频率
    // ω0 = 6.0 是一个常用值，原因：
    // 1. 在时间和频率分辨率之间提供良好的平衡
    // 2. 使得小波函数的均值非常接近于0（满足允许条件）
    // 3. 大约提供3个完整的振荡周期
    private static final double CENTRAL_FREQ = 6.0;
    
    // 如果需要更好的时间分辨率，可以使用更小的值，如：
    // private static final double CENTRAL_FREQ = 2.0;
    
    // 如果需要更好的频率分辨率，可以使用更大的值，如：
    // private static final double CENTRAL_FREQ = 8.0;
    
    @Override
    public Map<String, double[][]> transform(SignalDTO signalDTO, double[] scales) {
        double[] signal = signalDTO.getValues();
        double[] times = signalDTO.getTimes();
        int scaleCount = scales.length;
        int signalLength = signal.length;
        
        // 计算采样间隔
        double dt = times[1] - times[0];
        
        // 初始化系数矩阵
        double[][] coefficients = new double[scaleCount][signalLength];
        
        // 对每个尺度进行计算
        for (int i = 0; i < scaleCount; i++) {
            double scale = scales[i];
            // 计算当前尺度下的小波系数
            for (int t = 0; t < signalLength; t++) {
                // 使用滑动窗口进行卷积
                int windowSize = Math.max(10, (int)(4 * scale));
                int halfWindow = windowSize / 2;
                
                double sumReal = 0;
                double sumImag = 0;
                
                for (int n = -halfWindow; n < halfWindow; n++) {
                    int idx = t + n;
                    // 边界处理
                    if (idx >= 0 && idx < signalLength) {
                        // 计算小波基函数
                        double tau = n * dt;
                        double arg = CENTRAL_FREQ * tau / scale;
                        double exp = Math.exp(-arg * arg / 2);
                        
                        // 复数Morlet小波
                        double waveletReal = exp * Math.cos(arg);
                        double waveletImag = exp * Math.sin(arg);
                        
                        sumReal += signal[idx] * waveletReal;
                        sumImag += signal[idx] * waveletImag;
                    }
                }
                
                // 计算幅值并归一化
                coefficients[i][t] = Math.sqrt(sumReal * sumReal + sumImag * sumImag) / Math.sqrt(scale);
            }
        }
        
        // 返回结果，frequencies直接使用scales
        Map<String, double[][]> result = new HashMap<>();
        result.put("coefficients", coefficients);
        result.put("times", new double[][]{times});
        result.put("scales", new double[][]{scales});
        result.put("frequencies", new double[][]{scales}); // 直接使用scales代替frequencies
        
        return result;
    }
    
    @Override
    public double[] calculateWaveletPower(double[][] coefficients) {
        int scaleCount = coefficients.length;
        double[] power = new double[scaleCount];
        
        for (int i = 0; i < scaleCount; i++) {
            double sumSquared = 0;
            for (double coefficient : coefficients[i]) {
                sumSquared += coefficient * coefficient;
            }
            power[i] = sumSquared / coefficients[i].length;
        }
        
        return power;
    }
} 