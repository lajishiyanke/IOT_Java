package com.iot.platform.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.enums.FilterType;
import com.iot.platform.service.SignalProcessService;
import com.iot.platform.signal.ExternalProcessor;
import com.iot.platform.signal.SignalFilter;
import com.iot.platform.signal.impl.FrequencyAnalyzerImpl;
import com.iot.platform.signal.impl.WaveletTransformerImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalProcessServiceImpl implements SignalProcessService {
    
    private final ExternalProcessor externalProcessor;
    private final SignalFilter signalFilter;
    private final WaveletTransformerImpl waveletTransformer;
    private final FrequencyAnalyzerImpl frequencyAnalyzer;

    @Override
    public double[] processByPython(SignalDTO signalDTO, String scriptName, Map<String, Object> params) {
        log.debug("Processing signals with Python, script: {}", scriptName);
        return externalProcessor.processByPython(signalDTO.getValues(), scriptName, params);
    }

    @Override
    public Map<String, Object> getPythonResults(String scriptName, Map<String, Object> params) {
        log.debug("Getting Python results, script: {}", scriptName);
        return externalProcessor.getPythonResults(scriptName, params);
    }

    @Override
    public SignalDTO filter(SignalDTO signalDTO, FilterType type, double cutoffFreq, double... params) {
        double[] results = signalFilter.filter(signalDTO, type, cutoffFreq, params);
        SignalDTO response = new SignalDTO();
        response.setTimes(signalDTO.getTimes());
        response.setValues(results);
        return response;
    }

    @Override
    public Map<String, double[][]> wavelet(SignalDTO signalDTO, double[] scales) {
        return waveletTransformer.transform(signalDTO, scales);
    }

    @Override
    public Map<String, double[]> frequency(SignalDTO signalDTO) {
        return frequencyAnalyzer.fft(signalDTO);
    }
} 