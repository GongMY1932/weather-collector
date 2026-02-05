package com.base.weather.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 实时空气质量响应实体
 * 对应和风天气 REAL_TIME_AIR_QUALITY API 的响应数据
 */
@Data
public class RealTimeAirQualityResponse {

    /**
     * 元数据信息
     */
    private Metadata metadata;

    /**
     * 空气质量指数列表
     */
    private List<Index> indexes;

    /**
     * 污染物数据列表
     */
    private List<Pollutant> pollutants;

    /**
     * 监测站列表
     */
    private List<Station> stations;

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
        private Integer aqi;
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
         * 污染物全称，例如：颗粒物（粒径小于等于2.5µm）
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
         * 单位，例如：μg/m³, mg/m³
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
        private Integer aqi;
        /**
         * AQI显示值
         */
        private String aqiDisplay;
    }

    /**
     * 监测站信息
     */
    @Data
    public static class Station {
        /**
         * 监测站ID
         */
        private String id;
        /**
         * 监测站名称
         */
        private String name;
    }
}
