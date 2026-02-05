package com.base.weather.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 逐小时天气预报响应实体
 * 对应和风天气 HOURLY_FORECAST_WEATHER API 的响应数据
 */
@Data
public class HourlyForecastWeatherResponse {

    /**
     * 状态码，200表示成功
     */
    private String code;

    /**
     * 数据更新时间
     */
    private String updateTime;

    /**
     * 和风天气官网链接
     */
    private String fxLink;

    /**
     * 逐小时天气预报数据列表
     */
    private List<Hourly> hourly;

    /**
     * 数据来源和许可信息
     */
    private Refer refer;

    /**
     * 逐小时天气预报数据
     */
    @Data
    public static class Hourly {
        /**
         * 预报时间（ISO 8601格式，例如：2021-02-16T15:00+08:00）
         */
        private String fxTime;

        /**
         * 温度，单位：摄氏度
         */
        private String temp;

        /**
         * 天气图标代码
         */
        private String icon;

        /**
         * 天气文字描述，例如：晴、多云
         */
        private String text;

        /**
         * 风向360度角度
         */
        private String wind360;

        /**
         * 风向，例如：西北风、北风
         */
        private String windDir;

        /**
         * 风力等级，例如：3-4、4-5
         */
        private String windScale;

        /**
         * 风速，单位：公里/小时
         */
        private String windSpeed;

        /**
         * 相对湿度，单位：百分比
         */
        private String humidity;

        /**
         * 降水概率，单位：百分比
         */
        private String pop;

        /**
         * 降水量，单位：毫米
         */
        private String precip;

        /**
         * 大气压强，单位：百帕
         */
        private String pressure;

        /**
         * 云量，单位：百分比
         */
        private String cloud;

        /**
         * 露点温度，单位：摄氏度
         */
        private String dew;
    }

    /**
     * 数据来源和许可信息
     */
    @Data
    public static class Refer {
        /**
         * 数据来源列表
         */
        private List<String> sources;

        /**
         * 许可信息列表
         */
        private List<String> license;
    }
}
