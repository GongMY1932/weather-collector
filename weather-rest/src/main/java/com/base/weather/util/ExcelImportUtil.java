package com.base.weather.util;

import com.base.weather.constant.IndicatorEnum;
import com.base.weather.entity.req.WeatherStrategyAddReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Excel导入工具类
 * 用于解析Excel文件并转换为天气策略请求对象
 */
@Slf4j
@Component
public class ExcelImportUtil {

    /**
     * 中文指标名称到枚举名称的映射
     */
    private static final Map<String, String> INDICATOR_NAME_MAP = new HashMap<>();

    static {
        // 初始化中文指标名称映射
        for (IndicatorEnum indicator : IndicatorEnum.values()) {
            INDICATOR_NAME_MAP.put(indicator.getDescription(), indicator.name());
            // 添加一些常见的变体
            if ("pm2.5".equalsIgnoreCase(indicator.getDescription())) {
                INDICATOR_NAME_MAP.put("PM2.5", indicator.name());
                INDICATOR_NAME_MAP.put("pm2.5", indicator.name());
            }
            if ("pm10".equalsIgnoreCase(indicator.getDescription())) {
                INDICATOR_NAME_MAP.put("PM10", indicator.name());
                INDICATOR_NAME_MAP.put("pm10", indicator.name());
            }
        }
    }

    /**
     * 从Excel文件导入天气策略数据
     *
     * @param file Excel文件
     * @return 天气策略请求对象列表
     * @throws IOException IO异常
     */
    public List<WeatherStrategyAddReq> importFromExcel(MultipartFile file) throws IOException {
        List<WeatherStrategyAddReq> strategyList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 跳过表头，从第二行开始读取数据
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                WeatherStrategyAddReq strategy = parseRow(row);
                if (strategy != null) {
                    strategyList.add(strategy);
                }
            }
        }

        log.info("从Excel文件导入 {} 条策略数据", strategyList.size());
        return strategyList;
    }

    /**
     * 解析Excel行数据
     *
     * @param row Excel行
     * @return 天气策略请求对象
     */
    private WeatherStrategyAddReq parseRow(Row row) {
        WeatherStrategyAddReq strategy = new WeatherStrategyAddReq();

        try {
            // 需求名称（第0列）
            String demandName = getCellValueAsString(row.getCell(0));
            if (demandName == null || demandName.trim().isEmpty()) {
                log.warn("第 {} 行需求名称为空，跳过", row.getRowNum() + 1);
                return null;
            }
            strategy.setDemandName(demandName.trim());

            // 目标地址（第1列，格式：(经度,纬度)）
            String targetAddress = getCellValueAsString(row.getCell(1));
            if (targetAddress != null && !targetAddress.trim().isEmpty()) {
                parseCoordinates(targetAddress.trim(), strategy);
            }

            // 采集内容（第2列，格式：温度;PM2.5;云量;降水量）
            String collectContent = getCellValueAsString(row.getCell(2));
            if (collectContent != null && !collectContent.trim().isEmpty()) {
                String convertedContent = convertIndicatorNames(collectContent.trim());
                strategy.setCollectContent(convertedContent);
            }

            // 采集开始时间（第3列，格式：2026/1/1 12:00）
            String collectStart = getCellValueAsString(row.getCell(3));
            if (collectStart != null && !collectStart.trim().isEmpty()) {
                strategy.setCollectStart(convertDateTime(collectStart.trim()));
            }

            // 采集结束时间（第4列，格式：2026/1/5 12:00）
            String collectEnd = getCellValueAsString(row.getCell(4));
            if (collectEnd != null && !collectEnd.trim().isEmpty()) {
                strategy.setCollectEnd(convertDateTime(collectEnd.trim()));
            }

            return strategy;

        } catch (Exception e) {
            log.error("解析第 {} 行数据失败: {}", row.getRowNum() + 1, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取单元格值（字符串格式）
     *
     * @param cell 单元格
     * @return 字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 日期格式
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    return sdf.format(date);
                } else {
                    // 数字格式
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，去掉小数点
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 解析坐标字符串（格式：(经度,纬度)）
     *
     * @param coordinates 坐标字符串，例如："(12,21)"
     * @param strategy     策略对象
     */
    private void parseCoordinates(String coordinates, WeatherStrategyAddReq strategy) {
        try {
            // 移除括号和空格
            String cleaned = coordinates.replaceAll("[()\\s]", "");
            String[] parts = cleaned.split(",");
            if (parts.length == 2) {
                // 第一个是经度，第二个是纬度
                double longitude = Double.parseDouble(parts[0].trim());
                double latitude = Double.parseDouble(parts[1].trim());
                strategy.setTargetLongitude(longitude);
                strategy.setTargetLatitude(latitude);
            } else {
                log.warn("坐标格式不正确: {}", coordinates);
            }
        } catch (Exception e) {
            log.error("解析坐标失败: {}", coordinates, e);
        }
    }

    /**
     * 转换指标名称（中文转枚举名称）
     * 输入格式：温度;PM2.5;云量;降水量
     * 输出格式：Temperature,PM2p5,Cloud_cover,Precipitation
     *
     * @param indicatorNames 指标名称字符串（用分号分隔）
     * @return 转换后的指标名称字符串（用逗号分隔）
     */
    private String convertIndicatorNames(String indicatorNames) {
        String[] names = indicatorNames.split("[;；]"); // 支持中文和英文分号
        List<String> convertedNames = new ArrayList<>();

        for (String name : names) {
            name = name.trim();
            if (name.isEmpty()) {
                continue;
            }

            // 查找映射
            String enumName = INDICATOR_NAME_MAP.get(name);
            if (enumName == null) {
                // 尝试忽略大小写匹配
                for (Map.Entry<String, String> entry : INDICATOR_NAME_MAP.entrySet()) {
                    if (entry.getKey().equalsIgnoreCase(name)) {
                        enumName = entry.getValue();
                        break;
                    }
                }
            }

            if (enumName != null) {
                convertedNames.add(enumName);
            } else {
                log.warn("未找到指标名称映射: {}", name);
            }
        }

        return String.join(",", convertedNames);
    }

    /**
     * 转换日期时间格式
     * 输入格式：2026/1/1 12:00 或 2026/01/01 12:00
     * 输出格式：2026-01-01 12:00:00
     *
     * @param dateTimeStr 日期时间字符串
     * @return 转换后的日期时间字符串
     */
    private String convertDateTime(String dateTimeStr) {
        try {
            // 处理多种可能的格式
            dateTimeStr = dateTimeStr.trim();

            // 如果包含日期和时间
            if (dateTimeStr.contains(" ")) {
                String[] parts = dateTimeStr.split(" ");
                String datePart = parts[0];
                String timePart = parts.length > 1 ? parts[1] : "00:00";

                // 处理日期部分：2026/1/1 -> 2026-01-01
                String[] dateParts = datePart.split("/");
                if (dateParts.length == 3) {
                    String year = dateParts[0];
                    String month = String.format("%02d", Integer.parseInt(dateParts[1]));
                    String day = String.format("%02d", Integer.parseInt(dateParts[2]));

                    // 处理时间部分：12:00 -> 12:00:00
                    String[] timeParts = timePart.split(":");
                    String hour = timeParts.length > 0 ? String.format("%02d", Integer.parseInt(timeParts[0])) : "00";
                    String minute = timeParts.length > 1 ? String.format("%02d", Integer.parseInt(timeParts[1])) : "00";
                    String second = timeParts.length > 2 ? timeParts[2] : "00";

                    return String.format("%s-%s-%s %s:%s:%s", year, month, day, hour, minute, second);
                }
            } else {
                // 只有日期，没有时间
                String[] dateParts = dateTimeStr.split("/");
                if (dateParts.length == 3) {
                    String year = dateParts[0];
                    String month = String.format("%02d", Integer.parseInt(dateParts[1]));
                    String day = String.format("%02d", Integer.parseInt(dateParts[2]));
                    return String.format("%s-%s-%s 00:00:00", year, month, day);
                }
            }
        } catch (Exception e) {
            log.error("转换日期时间格式失败: {}", dateTimeStr, e);
        }

        // 如果转换失败，返回原值
        return dateTimeStr;
    }
}
