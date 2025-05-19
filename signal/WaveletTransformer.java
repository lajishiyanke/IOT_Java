package com.iot.platform.signal;

import java.util.Map;

import com.iot.platform.dto.SignalDTO;

public interface WaveletTransformer {
    /**
     * 执行连续小波变换
     * @param signal 输入信号
     * @param scales 尺度参数数组
     * @return 小波变换系数矩阵
     */
    Map<String, double[][]> transform(SignalDTO signalDTO, double[] scales);
    
    /**
     * 计算小波能量谱
     * @param coefficients 小波变换系数
     * @return 能量谱
     */
    double[] calculateWaveletPower(double[][] coefficients);
} 