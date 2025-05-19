package com.iot.platform.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ChartData {
    private String title;
    private String type;  // line, bar, pie等
    private List<String> xAxis;
    private List<String> yAxis;
    private List<Series> series;
    private Map<String, Object> options;
    
    @Data
    public static class Series {
        private String name;
        private String type;
        private List<Object> data;
        private Map<String, Object> itemStyle;
    }
} 