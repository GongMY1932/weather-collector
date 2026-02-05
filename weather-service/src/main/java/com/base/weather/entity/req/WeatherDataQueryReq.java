package com.base.weather.entity.req;

import com.base.common.entity.base.BaseRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 天气数据查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WeatherDataQueryReq extends BaseRequest {

    /**
     * 策略ID（必填）
     */
    private String strategyId;

    /**
     * 指标名称（可选，如：Temperature, PM2p5等）
     */
    private String indicatorName;

    /**
     * 开始时间（可选）
     */
    @JsonFormat(pattern = "yyyy-M-d HH:mm:ss", timezone = "Asia/Shanghai")
    private Date startTime;

    /**
     * 结束时间（可选）
     */
    @JsonFormat(pattern = "yyyy-M-d HH:mm:ss", timezone = "Asia/Shanghai")
    private Date endTime;

    /**
     * 城市名称（可选，模糊查询）
     */
    private String cityName;

    /**
     * 指标值（可选，用于精确查询）
     */
    private String indicatorValue;
}
