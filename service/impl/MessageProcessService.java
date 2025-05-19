package com.iot.platform.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iot.platform.entity.Device;
import com.iot.platform.entity.SensorData;
import com.iot.platform.entity.SignalFile;
import com.iot.platform.mapper.SignalFileMapper;
import com.iot.platform.service.DeviceService;
import com.iot.platform.service.SensorDataService;
import com.iot.platform.websocket.WebSocketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessService {
    
    private final DeviceService deviceService;
    private final SignalFileMapper signalFileMapper;
    private final SensorDataService sensorDataService;
    private final WebSocketService webSocketService;
    
    public void processMessage(String topic, String content) {
        try {
            log.info("MessageProcessService 开始处理消息");
            log.debug("消息详情 - Topic: {}, Content: {}", topic, content);
            
            JSONObject json = JSON.parseObject(content);
            
            if (topic.endsWith("/thing/event/property/post")) {
                log.info("处理属性上报消息");
                processPropertyPost(json);
            } else if (topic.endsWith("/user/signal")) {
                log.info("处理信号数据消息");
                processUserSignal(json);
            } else {
                log.warn("未知的消息类型: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
        }
    }
    
    private void processPropertyPost(JSONObject json) {
        String deviceCode = json.getString("deviceName");
        Device device = deviceService.getDeviceByCode(deviceCode);
        
        if (device == null) {
            log.error("未找到设备: {}", deviceCode);
            return;
        }
        
        // 处理属性上报消息
        JSONObject items = json.getJSONObject("items");
        if (items != null) {
            // 检查是否包含CSV数据
            if (items.containsKey("signal_data")) {
                // 处理CSV文件数据
                String csvData = items.getJSONObject("signal_data").getString("value");
                double samplingRate = items.getJSONObject("sampling_rate").getDoubleValue("value");
                
                // 保存为SignalFile
                SignalFile signalFile = new SignalFile();
                signalFile.setDeviceId(device.getId());
                signalFile.setFileName(String.format("signal_%d_%s.csv", device.getId(), 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))));
                signalFile.setSamplingRate(samplingRate);
                signalFile.setCollectTime(LocalDateTime.now());
                signalFile.setFileData(csvData.getBytes(StandardCharsets.UTF_8));
                signalFile.setFileSize(signalFile.getFileData().length);
                signalFile.setChannelCount(3);
                signalFile.setDataPoints((int) csvData.lines().count() - 1);
                
                signalFileMapper.insert(signalFile);
                log.info("成功保存设备 {} 的信号文件，大小: {} bytes", deviceCode, signalFile.getFileSize());
            } else {
                // 处理普通传感器数据
                for (String key : items.keySet()) {
                    JSONObject item = items.getJSONObject(key);
                    
                    SensorData sensorData = new SensorData();
                    sensorData.setDeviceId(device.getId());
                    sensorData.setChannelId(key);
                    sensorData.setDataValue(item.getDoubleValue("value"));
                    sensorData.setDataUnit("mV");
                    sensorData.setDataType("amplitude");
                    sensorData.setCollectTime(LocalDateTime.now());
                    
                    // 保存数据
                    sensorDataService.saveSensorData(sensorData);
                    
                    // 推送实时数据
                    webSocketService.pushRealTimeData(deviceCode, sensorData);
                }
            }
            
            // 更新设备状态
            deviceService.updateDeviceStatus(deviceCode, true);
            webSocketService.pushDeviceStatusChange(deviceCode, true);
        }
    }
    
    private void processUserSignal(JSONObject json) {
        try {
            // 统一处理设备编码
            String deviceCode = json.getString("deviceCode");
            if (deviceCode == null) {
                deviceCode = json.getString("device_code");  // 尝试另一种格式
            }
            
            if (deviceCode == null) {
                log.error("设备编码为空");
                return;
            }

            Device device = deviceService.getDeviceByCode(deviceCode);
            if (device == null) {
                log.error("未找到设备: {}", deviceCode);
                return;
            }
            
            // 处理传感器数据
            JSONArray sensorDataArray = json.getJSONArray("sensorData");
            if (sensorDataArray != null) {
                for (int i = 0; i < sensorDataArray.size(); i++) {
                    JSONObject sensorData = sensorDataArray.getJSONObject(i);
                    
                    SensorData data = new SensorData();
                    data.setDeviceId(device.getId());
                    data.setChannelId(sensorData.getString("channelId"));
                    data.setDataValue(sensorData.getDoubleValue("value"));
                    data.setDataUnit(sensorData.getString("unit"));
                    data.setDataType(sensorData.getString("type"));
                    
                    // 修改时间处理
                    String timestamp = sensorData.getString("timestamp");
                    // 处理UTC时间
                    LocalDateTime collectTime = LocalDateTime.parse(timestamp.replace("Z", "")).plusHours(8);
                    data.setCollectTime(collectTime);
                    
                    log.info("Saving sensor data: deviceId={}, channelId={}, value={}, time={}", 
                        device.getId(), data.getChannelId(), data.getDataValue(), data.getCollectTime());
                    
                    sensorDataService.saveSensorData(data);
                    
                    // 推送实时数据
                    webSocketService.pushRealTimeData(deviceCode, data);
                }
            }
        } catch (Exception e) {
            log.error("处理传感器数据失败: {}", e.getMessage(), e);
        }
    }

    public void processDeviceMessage(String topic, String content) {
        try {
            // log.debug("Processing message - Topic: {}, Content: {}", topic, content);
            
            // 解析消息内容
            JSONObject message = JSON.parseObject(content);

            // 从topic中解析设备信息
            String[] topicParts = topic.split("/");
            if (topicParts.length < 3) {
                log.error("Invalid topic format: {}", topic);
                return;
            }

            // 获取设备编码
            String deviceCode = message.getString("deviceName");  // 从新的消息格式中获取deviceCode
            
            if (deviceCode == null) {
                log.error("Device code not found in message: {}", content);
                return;
            }
            
            // 处理传感器数据
            JSONArray sensorDataArray = message.getJSONArray("signal_data");
            if (sensorDataArray != null) {
                for (int i = 0; i < sensorDataArray.size(); i++) {
                    JSONObject sensorDataJson = sensorDataArray.getJSONObject(i);
                    // 获取设备ID
                    // 获取设备ID
                    Integer deviceId = deviceService.getDeviceIdByCode(deviceCode);
                    log.debug("deviceId: {}", deviceId);
                    if (deviceId == null) {
                        log.error("Device not found for code: {}", deviceCode);
                        return;
                    }
                    SensorData sensorData = new SensorData();
                    sensorData.setDeviceId(deviceId);
                    sensorData.setChannelId(sensorDataJson.getString("channelId"));
                    sensorData.setDataValue(sensorDataJson.getDouble("value"));
                    sensorData.setDataUnit(sensorDataJson.getString("unit"));
                    sensorData.setDataType(sensorDataJson.getString("type"));
                    
                    // 处理时间戳
                    // 处理时间戳
                    String timestamp = sensorDataJson.getString("timestamp");
                    // 处理UTC时间
                    LocalDateTime collectTime = LocalDateTime.parse(timestamp.replace("Z", "")).plusHours(8);
                    sensorData.setCollectTime(collectTime);
                    
                    // 推送实时数据到WebSocket
                    webSocketService.pushRealTimeData(deviceCode, sensorData);
                    
                    // 保存到数据库（如果需要）
                    sensorDataService.saveSensorData(sensorData);
                    
                    log.debug("Processed sensor data: channelId={}, value={}", 
                        sensorData.getChannelId(), sensorData.getDataValue());
                }
                
                // 更新设备状态
                deviceService.updateDeviceStatus(deviceCode, true);
                webSocketService.pushDeviceStatusChange(deviceCode, true);
            }
        } catch (Exception e) {
            log.error("Process device message error - Topic: {}, Content: {}", topic, content, e);
        }
    }
}
