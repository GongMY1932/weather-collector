package com.base.weather.entity.vo;

import lombok.Data;

/**
 * 天气策略前端展示类
 */
@Data
public class WeatherStrategyVo {
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
     * 备注
     */
    private String remark;

    /**
     * 收集状态（varchar类型，存储状态码字符串：0-待采集，1-采集中，2-采集完成，4-已取消）
     */
    private String collectStatus;

    /**
     * 收集状态描述
     */
    private String collectStatusDesc;

    /**
     * 策略优先级（0-紧急，1-普通）
     */
    private Integer targetPriority;
}
