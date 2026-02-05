package com.base.weather.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 监测站数据响应实体
 * 对应和风天气 MONITORING_STATION_DATA API 的响应数据
 *
 * @author system
 */
@Data
public class MonitoringStationDataResponse {
    
    /**
     * 元数据信息
     */
    private Metadata metadata;
    
    /**
     * 污染物数据列表
     */
    private List<Pollutant> pollutants;
    
    /**
     * 元数据信息
     */
    @Data
    public static class Metadata {
        /**
         * 数据标签
         */
        private String tag;
        
        /**
         * 是否为零结果
         */
        private Boolean zeroResult;
        
        /**
         * 数据来源列表
         */
        private List<String> sources;
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
}
