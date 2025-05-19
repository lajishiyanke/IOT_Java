package com.iot.platform.util;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExcelUtil {

    /**
     * 导出数据到Excel文件
     */
    public <T> void exportToExcel(List<T> dataList, String filePath) throws Exception {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 获取类的字段作为表头
            Field[] fields = dataList.get(0).getClass().getDeclaredFields();
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(fields[i].getName());
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            int rowNum = 1;
            for (T data : dataList) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Cell cell = row.createCell(i);
                    Object value = fields[i].get(data);
                    setCellValue(cell, value);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < fields.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 保存文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

            log.info("Successfully exported {} records to Excel file: {}", dataList.size(), filePath);
        } catch (Exception e) {
            log.error("Failed to export data to Excel", e);
            throw e;
        }
    }

    /**
     * 导出数据到Excel文件（自定义表头）
     */
    public <T> void exportToExcel(List<T> dataList, String[] headers, String[] fields, String filePath) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            int rowNum = 1;
            for (T data : dataList) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < fields.length; i++) {
                    Field field = data.getClass().getDeclaredField(fields[i]);
                    field.setAccessible(true);
                    Cell cell = row.createCell(i);
                    Object value = field.get(data);
                    setCellValue(cell, value);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 保存文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

            log.info("Successfully exported {} records to Excel file: {}", dataList.size(), filePath);
        } catch (Exception e) {
            log.error("Failed to export data to Excel", e);
            throw e;
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
} 