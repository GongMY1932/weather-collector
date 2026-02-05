package com.base.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 天气数据实体类
 */
@Data
@TableName("weather_data")
public class WeatherData {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 天气策略ID（关联weather_strategy表）
     */
    @TableField("strategy_id")
    private String strategyId;

    /**
     * 城市名称
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 纬度
     */
    @TableField("latitude")
    private Double latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private Double longitude;

    /**
     * 采集时间
     */
    @TableField("collect_time")
    private LocalDateTime collectTime;

    /**
     * 指标名称（IndicatorEnum的name，如：Temperature, PM2p5）
     */
    @TableField("indicator_name")
    private String indicatorName;

    /**
     * 指标值
     */
    @TableField("indicator_value")
    private String indicatorValue;

    /**
     * 指标单位
     */
    @TableField("indicator_unit")
    private String indicatorUnit;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 删除标识（0-未删除，1-已删除）
     */
    @TableField("del_flag")
    private Integer delFlag;
}
