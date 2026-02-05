package com.base.weather.entity.req;

import com.base.common.entity.base.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 天气策略查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class WeatherStrategyQueryReq extends BaseRequest {


    /**
     * 需求名称（模糊查询）
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
     * 采集内容（模糊查询）
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
     * 采集状态（varchar，存状态码字符串：0/1/2/3/4）
     */
    private String collectStatus;
}
