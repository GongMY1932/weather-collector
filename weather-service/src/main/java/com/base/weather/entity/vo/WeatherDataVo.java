package com.base.weather.entity.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 天气数据前端展示类
 * 按 collectTime 分组，每个指标作为独立字段
 */
@Data
public class WeatherDataVo {
    /**
     * 天气策略ID（关联weather_strategy表）
     */
    private String strategyId;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 采集时间（分组字段）
     */
    private LocalDateTime collectTime;

    // ========== 实时天气指标 ==========
    /**
     * 温度（Temperature）
     */
    private String temperature;

    /**
     * 体感温度（Perceived_temperature）
     */
    private String perceivedTemperature;

    /**
     * 风力（Wind_speed）
     */
    private String windSpeed;

    /**
     * 风向（Wind_direction）
     */
    private String windDirection;

    /**
     * 相对湿度（Relative_humidify）
     */
    private String relativeHumidify;

    /**
     * 大气压强（Atmospheric_pressure）
     */
    private String atmosphericPressure;

    /**
     * 降水量（Precipitation）
     */
    private String precipitation;

    /**
     * 能见度（Visibility）
     */
    private String visibility;

    /**
     * 露点温度（Dew_point_temperature）
     */
    private String dewPointTemperature;

    /**
     * 云量（Cloud_cover）
     */
    private String cloudCover;

    // ========== 空气质量指标 ==========
    /**
     * PM2.5（PM2p5）
     */
    private String pm2p5;

    /**
     * PM10
     */
    private String pm10;

    /**
     * 一氧化碳（CO）
     */
    private String co;

    /**
     * 二氧化硫（SO2）
     */
    private String so2;

    /**
     * 臭氧（O3）
     */
    private String o3;

    /**
     * 二氧化氮（NO2）
     */
    private String no2;
}
