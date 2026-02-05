package com.base.weather.entity.dto;

import lombok.Data;
import java.util.List;

/**
 * 城市查询响应实体
 * 对应和风天气 GEO_CITY API 的响应数据
 *
 * @author system
 */
@Data
public class GeoCityResponse {
    
    /**
     * 状态码，200表示成功
     */
    private String code;
    
    /**
     * 城市位置信息列表
     */
    private List<Location> location;
    
    /**
     * 数据来源和许可信息
     */
    private Refer refer;
    
    /**
     * 城市位置信息
     */
    @Data
    public static class Location {
        /**
         * 城市名称
         */
        private String name;
        
        /**
         * 城市ID，用于后续API调用
         */
        private String id;
        
        /**
         * 纬度
         */
        private String lat;
        
        /**
         * 经度
         */
        private String lon;
        
        /**
         * 城市级别（二级行政区）
         */
        private String adm2;
        
        /**
         * 省级行政区
         */
        private String adm1;
        
        /**
         * 国家
         */
        private String country;
        
        /**
         * 时区
         */
        private String tz;
        
        /**
         * UTC偏移量
         */
        private String utcOffset;
        
        /**
         * 是否夏令时，0表示否，1表示是
         */
        private String isDst;
        
        /**
         * 城市类型
         */
        private String type;
        
        /**
         * 城市排名
         */
        private String rank;
        
        /**
         * 和风天气官网链接
         */
        private String fxLink;
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
