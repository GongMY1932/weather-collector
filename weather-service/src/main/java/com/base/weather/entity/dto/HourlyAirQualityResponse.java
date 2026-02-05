package com.base.weather.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 空气质量小时预报响应实体
 * 对应和风天气 HOURLY_AIR_QUALITY API 的响应数据
 */
@Data
public class HourlyAirQualityResponse {

    /**
     * 元数据信息
     */
    private Metadata metadata;

    /**
     * 小时预报数据列表
     */
    private List<Hour> hours;

    /**
     * 元数据信息
     */
    @Data
    public static class Metadata {
        /**
         * 数据标签
         */
        private String tag;
    }

    /**
     * 小时预报数据
     */
    @Data
    public static class Hour {
        /**
         * 预报时间（ISO 8601格式，例如：2023-05-17T03:00Z）
         */
        private String forecastTime;

        /**
         * 空气质量指数列表
         */
        private List<Index> indexes;

        /**
         * 污染物数据列表
         */
        private List<Pollutant> pollutants;
    }

    /**
     * 空气质量指数
     */
    @Data
    public static class Index {
        /**
         * 指数代码
         */
        private String code;

        /**
         * 指数名称
         */
        private String name;

        /**
         * AQI值
         */
        private Double aqi;

        /**
         * AQI显示值
         */
        private String aqiDisplay;

        /**
         * 等级
         */
        private String level;

        /**
         * 类别
         */
        private String category;

        /**
         * 颜色信息
         */
        private Color color;

        /**
         * 主要污染物
         */
        private PrimaryPollutant primaryPollutant;

        /**
         * 健康建议
         */
        private Health health;
    }

    /**
     * 颜色信息
     */
    @Data
    public static class Color {
        /**
         * 红色分量
         */
        private Integer red;

        /**
         * 绿色分量
         */
        private Integer green;

        /**
         * 蓝色分量
         */
        private Integer blue;

        /**
         * 透明度
         */
        private Integer alpha;
    }

    /**
     * 主要污染物
     */
    @Data
    public static class PrimaryPollutant {
        /**
         * 污染物代码
         */
        private String code;

        /**
         * 污染物名称
         */
        private String name;

        /**
         * 污染物全称
         */
        private String fullName;
    }

    /**
     * 健康建议
     */
    @Data
    public static class Health {
        /**
         * 健康影响
         */
        private String effect;

        /**
         * 建议信息
         */
        private Advice advice;
    }

    /**
     * 建议信息
     */
    @Data
    public static class Advice {
        /**
         * 普通人群建议
         */
        private String generalPopulation;

        /**
         * 敏感人群建议
         */
        private String sensitivePopulation;
    }

    /**
     * 污染物信息
     */
    @Data
    public static class Pollutant {
        /**
         * 污染物代码，例如：pm2p5, pm10, no2, o3, so2, co
         */
        private String code;

        /**
         * 污染物名称，例如：PM 2.5, PM 10
         */
        private String name;

        /**
         * 污染物全称，例如：Fine particulate matter (<2.5µm)
         */
        private String fullName;

        /**
         * 浓度信息
         */
        private Concentration concentration;

        /**
         * 子指数列表
         */
        private List<SubIndex> subIndexes;
    }

    /**
     * 浓度信息
     */
    @Data
    public static class Concentration {
        /**
         * 浓度值
         */
        private Double value;

        /**
         * 单位，例如：μg/m3, mg/m³
         */
        private String unit;
    }

    /**
     * 子指数信息
     */
    @Data
    public static class SubIndex {
        /**
         * 指数代码
         */
        private String code;

        /**
         * AQI值
         */
        private Double aqi;

        /**
         * AQI显示值
         */
        private String aqiDisplay;
    }
}
