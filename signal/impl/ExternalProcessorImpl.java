package com.iot.platform.signal.impl;

import com.iot.platform.signal.ExternalProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalProcessorImpl implements ExternalProcessor {


    @Value("${python.executable}")
    private String pythonExecutable;

    @Value("${python.scripts-path}")
    private String pythonScriptsPath;

    @Override
    public double[] processByPython(double[] signals, String scriptName, Map<String, Object> params) {
        try {
            // 获取脚本完整路径
            Resource scriptResource = new ClassPathResource(pythonScriptsPath + "/" + scriptName);
            String scriptPath = scriptResource.getFile().getAbsolutePath();
            log.debug("Executing Python script: {}", scriptPath);

            // 保存信号数据到临时文件
            Path tempFile = Files.createTempFile("signals", ".csv");
            savePythonData(signals, tempFile, params);

            // 构建Python命令
            ProcessBuilder pb = new ProcessBuilder(
                pythonExecutable,
                scriptResource.getFile().getAbsolutePath(),
                tempFile.toString()
            );
            pb.redirectErrorStream(true);
            pb.directory(scriptResource.getFile().getParentFile());  // 设置工作目录

            // 执行Python脚本
            Process process = pb.start();
            // 记录Python输出
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("Python output: {}", line);
                }
            }
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);

            if (!completed) {
                throw new RuntimeException("Python processing timeout");
            }

            // 读取结果
            return loadPythonResults(tempFile.toString() + ".data");

        } catch (Exception e) {
            log.error("Failed to process signals with Python", e);
            throw new RuntimeException("Python processing failed", e);
        }
    }


    @Override
    public Map<String, Object> getPythonResults(String scriptPath, Map<String, Object> params) {
        try {
            // 创建临时文件存储参数
            Path tempFile = Files.createTempFile("params", ".json");
            savePythonParams(params, tempFile);

            // 构建Python命令
            ProcessBuilder pb = new ProcessBuilder(
                pythonExecutable,
                scriptPath,
                tempFile.toString()
            );
            pb.redirectErrorStream(true);

            // 执行Python脚本
            Process process = pb.start();
            boolean completed = process.waitFor(30, TimeUnit.SECONDS);

            if (!completed) {
                throw new RuntimeException("Python execution timeout");
            }

            // 读取结果
            return loadPythonMap(tempFile.toString() + ".result");

        } catch (Exception e) {
            log.error("Failed to get results from Python", e);
            throw new RuntimeException("Python execution failed", e);
        }
    }

    private void savePythonData(double[] signals, Path file, Map<String, Object> params) {
        try {
            // 使用CSV格式保存数据
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                // 写入参数作为CSV头
                if (params != null && !params.isEmpty()) {
                    writer.write("# Parameters: ");
                    writer.write(new ObjectMapper().writeValueAsString(params));
                    writer.newLine();
                }
                
                // 写入信号数据
                for (double signal : signals) {
                    writer.write(String.valueOf(signal));
                    writer.newLine();
                }
            }
            log.debug("Saved signal data to CSV file: {}", file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save signal data to CSV", e);
        }
    }

    private double[] loadPythonResults(String file) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(file));
            double[] results = new double[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                results[i] = Double.parseDouble(lines.get(i).trim());
            }
            return results;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load results from CSV", e);
        }
    }

    private void savePythonParams(Map<String, Object> params, Path file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file.toFile(), params);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save Python parameters", e);
        }
    }

    private Map<String, Object> loadPythonMap(String file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(new File(file), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Python results map", e);
        }
    }
} 