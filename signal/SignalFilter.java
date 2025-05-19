package com.iot.platform.signal;

import com.iot.platform.dto.SignalDTO;
import com.iot.platform.enums.FilterType;
/**
 * 信号处理接口
 */
public interface SignalFilter {
     
    /**
     * 信号滤波
     * @param signals 输入信号
     * @param type 滤波器类型
     * @param cutoffFreq 截止频率
     * @param params 其他参数（如带通滤波的第二个截止频率）
     * @return 滤波后的信号
     */
    double[] filter(SignalDTO signalDTO, FilterType type, double cutoffFreq, double... params);
    
    
}