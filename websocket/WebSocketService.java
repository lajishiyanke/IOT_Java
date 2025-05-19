package com.iot.platform.websocket;

import com.iot.platform.dto.ChartData;
import com.iot.platform.dto.SignalDTO;
import com.iot.platform.entity.AlarmRecord;
import com.iot.platform.entity.SensorData;

public interface WebSocketService {
    
    void pushSignalData(String deviceCode, SignalDTO signalData);
    
    void pushRealTimeData(String deviceCode, SensorData data);
    
    void pushAlarmNotification(AlarmRecord alarm);
    
    void pushChartUpdate(String deviceCode, ChartData chartData);
    
    void pushDeviceStatusChange(String deviceCode, boolean status);
} 