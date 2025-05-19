package com.iot.platform.signal;

import java.util.Map;

/**
 * 外部信号处理接口
 */
public interface ExternalProcessor {
   
    /**
     * 调用Python处理信号
     */
    double[] processByPython(double[] signals, String scriptName, Map<String, Object> params);
       
    /**
     * 获取Python计算结果
     */
    Map<String, Object> getPythonResults(String scriptName, Map<String, Object> params);
} 
