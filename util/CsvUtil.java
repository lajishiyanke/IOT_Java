package com.iot.platform.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CsvUtil {

    /**
     * 从CSV文件读取数据
     * @param file CSV文件
     * @return 数据列表，每行是一个字符串数组
     */
    public static List<String[]> readCsv(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {
            
            return csvReader.readAll();
        } catch (IOException | CsvException e) {
            log.error("Failed to read CSV file", e);
            throw new RuntimeException("Failed to read CSV file", e);
        }
    }

    /**
     * 从CSV文件读取信号数据
     * @param file CSV文件
     * @return 信号数据数组
     */
    public static double[] readSignalData(MultipartFile file) {
        List<String[]> rows = readCsv(file);
        List<Double> signals = new ArrayList<>();
        
        // 跳过可能的标题行
        for (int i = 1; i < rows.size(); i++) {
            try {
                signals.add(Double.parseDouble(rows.get(i)[0]));
            } catch (NumberFormatException e) {
                log.warn("Skipping invalid number format at row {}: {}", i, Arrays.toString(rows.get(i)));
            }
        }
        
        // 转换为double数组
        double[] result = new double[signals.size()];
        for (int i = 0; i < signals.size(); i++) {
            result[i] = signals.get(i);
        }
        
        return result;
    }

    /**
     * 将数据写入CSV文件
     * @param data 数据列表
     * @param file 目标文件
     */
    public static void writeCsv(List<String[]> data, File file) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            writer.writeAll(data);
        } catch (IOException e) {
            log.error("Failed to write CSV file", e);
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    /**
     * 将信号数据写入CSV文件
     * @param signals 信号数据
     * @param file 目标文件
     */
    public static void writeSignalData(double[] signals, File file) {
        List<String[]> data = new ArrayList<>();
        
        // 添加标题行
        data.add(new String[]{"Signal"});
        
        // 添加数据行
        for (double signal : signals) {
            data.add(new String[]{String.valueOf(signal)});
        }
        
        writeCsv(data, file);
    }
} 