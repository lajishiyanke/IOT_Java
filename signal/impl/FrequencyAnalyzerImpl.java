package com.iot.platform.signal.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.springframework.stereotype.Service;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.signal.FrequencyAnalyzer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FrequencyAnalyzerImpl implements FrequencyAnalyzer {

    @Override
    public Map<String, double[]> fft(SignalDTO signalDTO) {
        // 计算采样频率 (1/dt)
        double dt = signalDTO.getTimes()[1] - signalDTO.getTimes()[0];  // 时间间隔
        double samplingRate = 1.0 / dt;  // 采样频率
        
        // 补零至2的幂次方长度
        int n = signalDTO.getValues().length;
        int powerOfTwo = Integer.highestOneBit(n - 1) << 1;
        double[] paddedSignals = new double[powerOfTwo];
        System.arraycopy(signalDTO.getValues(), 0, paddedSignals, 0, n);

        // 执行FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] complexResults = transformer.transform(paddedSignals, TransformType.FORWARD);

        // 计算幅值谱
        double[] magnitudes = new double[complexResults.length / 2];  // 只取一半（由于对称性）
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = complexResults[i].abs() / n;  // 归一化
        }

        // 计算频率数组
        double[] frequencies = new double[magnitudes.length];
        double df = samplingRate / powerOfTwo;  // 频率分辨率
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = i * df;
        }

        // 返回频率和幅值
        Map<String, double[]> result = new HashMap<>();
        result.put("frequencies", frequencies);
        result.put("magnitudes", magnitudes);
        return result;
    }

}