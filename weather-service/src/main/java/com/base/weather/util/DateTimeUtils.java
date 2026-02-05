package com.base.weather.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供时间字符串解析、时区转换等通用方法
 */
@Slf4j
public class DateTimeUtils {

    /**
     * 解析日期时间字符串（支持多种格式）
     * <p>
     * 支持的格式：
     * - yyyy-MM-dd HH:mm:ss（标准格式，月份和日期都是两位数）
     * - yyyy-M-d HH:mm:ss（月份和日期可以是1位数，如：2026-1-28 00:00:00）
     * - yyyy/MM/dd HH:mm:ss
     * - yyyy/M/d HH:mm:ss
     * - yyyy-MM-dd（只有日期部分）
     * - yyyy-M-d（只有日期部分，月份和日期可以是1位数）
     *
     * @param timeStr 时间字符串
     * @return LocalDateTime 对象，解析失败返回 null
     */
    public static LocalDateTime parseDateTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }

        // 尝试多种格式
        DateTimeFormatter[] formatters = {
                // 标准格式：yyyy-MM-dd HH:mm:ss
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                // 月份和日期可以是1位数：yyyy-M-d HH:mm:ss
                DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss"),
                // 使用斜杠分隔：yyyy/MM/dd HH:mm:ss
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                // 使用斜杠分隔，月份和日期可以是1位数：yyyy/M/d HH:mm:ss
                DateTimeFormatter.ofPattern("yyyy/M/d HH:mm:ss"),
                // 只有日期部分：yyyy-MM-dd
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                // 只有日期部分，月份和日期可以是1位数：yyyy-M-d
                DateTimeFormatter.ofPattern("yyyy-M-d")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                // 如果只有日期部分，则设置为当天的00:00:00
                if (timeStr.length() <= 10) {
                    return LocalDate.parse(timeStr, formatter).atStartOfDay();
                } else {
                    return LocalDateTime.parse(timeStr, formatter);
                }
            } catch (Exception e) {
                // 继续尝试下一个格式
                continue;
            }
        }

        log.warn("无法解析时间字符串: {}", timeStr);
        return null;
    }

    /**
     * 解析预报时间字符串（UTC时间转换为北京时间）
     * <p>
     * API返回的时间都是UTC时间，需要转换为北京时间（UTC+8）
     * <p>
     * 支持的格式：
     * - 2023-05-17T03:00Z（UTC时间，带Z后缀）
     * - 2021-02-16T15:00+08:00（带时区偏移）
     * - 2021-02-16T15:00-05:00（带时区偏移）
     * - 2021-02-16T15:00（默认视为UTC时间）
     *
     * @param timeStr 时间字符串
     * @return LocalDateTime 对象（北京时间），解析失败返回 null
     */
    public static LocalDateTime parseForecastTime(String timeStr) {
        if (!StringUtils.hasText(timeStr)) {
            return null;
        }
        try {
            ZonedDateTime utcTime = null;

            // 尝试多种时间格式
            if (timeStr.endsWith("Z")) {
                // UTC时间格式：2023-05-17T03:00Z
                String withoutZ = timeStr.substring(0, timeStr.length() - 1);
                LocalDateTime localDateTime = LocalDateTime.parse(withoutZ, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                utcTime = localDateTime.atZone(ZoneId.of("UTC"));
            } else if (timeStr.contains("+") || (timeStr.contains("-") && timeStr.length() > 16)) {
                // 带时区的时间格式：2021-02-16T15:00+08:00 或 2021-02-16T15:00-05:00
                // 使用 ISO 8601 格式解析
                try {
                    utcTime = ZonedDateTime.parse(timeStr);
                    // 如果已经有时区信息，先转换为UTC
                    utcTime = utcTime.withZoneSameInstant(ZoneId.of("UTC"));
                } catch (Exception e) {
                    // 如果解析失败，尝试手动解析
                    int timezoneIndex = timeStr.lastIndexOf("+");
                    if (timezoneIndex == -1) {
                        // 查找时区部分的减号（不是日期中的减号）
                        for (int i = timeStr.length() - 1; i >= 16; i--) {
                            if (timeStr.charAt(i) == '-') {
                                timezoneIndex = i;
                                break;
                            }
                        }
                    }
                    if (timezoneIndex > 0) {
                        String dateTimePart = timeStr.substring(0, timezoneIndex);
                        String timezonePart = timeStr.substring(timezoneIndex);
                        LocalDateTime localDateTime = LocalDateTime.parse(dateTimePart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                        // 解析时区偏移
                        ZoneId zoneId = ZoneId.of("UTC" + timezonePart);
                        utcTime = localDateTime.atZone(zoneId).withZoneSameInstant(ZoneId.of("UTC"));
                    }
                }
            } else {
                // 默认格式：yyyy-MM-dd'T'HH:mm（假设是UTC时间）
                LocalDateTime localDateTime = LocalDateTime.parse(timeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                utcTime = localDateTime.atZone(ZoneId.of("UTC"));
            }

            if (utcTime == null) {
                log.warn("无法解析预报时间: {}", timeStr);
                return null;
            }

            // 转换为北京时间（UTC+8）
            ZonedDateTime beijingTime = utcTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai"));
            log.debug("UTC时间 {} 转换为北京时间: {}", utcTime, beijingTime);
            return beijingTime.toLocalDateTime();

        } catch (Exception e) {
            log.warn("解析预报时间失败: {}", timeStr, e);
            return null;
        }
    }
}
