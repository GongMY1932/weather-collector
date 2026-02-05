package com.base.weather.entity.req;

import lombok.Data;

/**
 * 天气策略新增请求类
 */
@Data
public class WeatherStrategyAddReq {
    /**
     * 主键ID
     */
    private String id;

    /**
     * 需求名称
     */
    private String demandName;

    /**
     * 目标纬度
     */
    private Double targetLatitude;

    /**
     * 目标经度
     */
    private Double targetLongitude;

    /**
     * 采集内容
     */
    private String collectContent;

    /**
     * 采集开始时间
     */
    private String collectStart;

    /**
     * 采集结束时间
     */
    private String collectEnd;

    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 策略优先级（0-紧急，1-普通）
     */
    private Integer targetPriority;
}
