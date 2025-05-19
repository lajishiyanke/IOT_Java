package com.iot.platform.signal.impl;

import org.springframework.stereotype.Service;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.enums.FilterType;
import com.iot.platform.signal.SignalFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SignalFilterImpl implements SignalFilter {

    @Override
    public double[] filter(SignalDTO signalDTO, FilterType type, double cutoffFreq, double... params) {
        switch (type) {
            case LOW_PASS:
                return lowPassFilter(signalDTO.getValues(), cutoffFreq);
            case HIGH_PASS:
                return highPassFilter(signalDTO.getValues(), cutoffFreq);
            case BAND_PASS:
                if (params.length < 1) {
                    throw new IllegalArgumentException("Band pass filter needs two cutoff frequencies");
                }
                return bandPassFilter(signalDTO.getValues(), cutoffFreq, params[0]);
            default:
                throw new IllegalArgumentException("Unsupported filter type");
        }
    }

    private double[] lowPassFilter(double[] signals, double cutoffFreq) {
        int n = signals.length;
        double[] filtered = new double[n];
        double dt = 1.0; // 采样时间间隔
        double alpha = dt / (1.0 / (2 * Math.PI * cutoffFreq) + dt);
        
        filtered[0] = signals[0];
        for (int i = 1; i < n; i++) {
            filtered[i] = filtered[i-1] + alpha * (signals[i] - filtered[i-1]);
        }
        
        return filtered;
    }

    private double[] highPassFilter(double[] signals, double cutoffFreq) {
        int n = signals.length;
        double[] filtered = new double[n];
        double dt = 1.0; // 采样时间间隔
        double alpha = 1.0 / (2 * Math.PI * cutoffFreq * dt + 1);
        
        filtered[0] = signals[0];
        for (int i = 1; i < n; i++) {
            filtered[i] = alpha * (filtered[i-1] + signals[i] - signals[i-1]);
        }
        
        return filtered;
    }

    private double[] bandPassFilter(double[] signals, double lowCutoff, double highCutoff) {
        // 先应用高通滤波，再应用低通滤波
        double[] highPassed = highPassFilter(signals, lowCutoff);
        return lowPassFilter(highPassed, highCutoff);
    }

} 