package com.base.weather.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 实时天气响应实体
 * 对应和风天气 REAL_TIME_WEATHER API 的响应数据
 *
 * @author system
 */
@Data
public class RealTimeWeatherResponse {

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
     * 当前天气实况数据
     */
    private Now now;
    
    /**
     * 数据来源和许可信息
     */
    private Refer refer;

    /**
     * 当前天气实况数据
     */
    @Data
    public static class Now {
        /**
         * 观测时间
         */
        private String obsTime;
        
        /**
         * 温度，单位：摄氏度
         */
        private String temp;
        
        /**
         * 体感温度，单位：摄氏度
         */
        private String feelsLike;
        
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
         * 风向，例如：东风、南风
         */
        private String windDir;
        
        /**
         * 风力等级
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
         * 降水量，单位：毫米
         */
        private String precip;
        
        /**
         * 大气压强，单位：百帕
         */
        private String pressure;
        
        /**
         * 能见度，单位：公里
         */
        private String vis;
        
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
