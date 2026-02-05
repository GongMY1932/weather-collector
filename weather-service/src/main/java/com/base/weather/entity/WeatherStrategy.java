package com.base.weather.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 天气策略实体类
 */
@Data
@TableName("weather_strategy")
public class WeatherStrategy {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 需求名称
     */
    @TableField("demand_name")
    private String demandName;

    /**
     * 目标纬度
     */
    @TableField("target_latitude")
    private Double targetLatitude;

    /**
     * 目标经度
     */
    @TableField("target_longitude")
    private Double targetLongitude;

    /**
     * 采集内容
     */
    @TableField("collect_content")
    private String collectContent;

    /**
     * 采集开始时间
     */
    @TableField("collect_start")
    private String collectStart;

    /**
     * 采集结束时间
     */
    @TableField("collect_end")
    private String collectEnd;

    /**
     * 删除标识
     */
    @TableField("del_flag")
    private int delFlag;

    /**
     * 城市名称
     */
    @TableField("city_name")
    private String cityName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 收集状态（varchar类型，存储状态码字符串：0-待采集，1-采集中，2-采集完成，4-已取消）
     */
    @TableField("collect_status")
    private String collectStatus;

    /**
     * 策略优先级（0-紧急，1-普通）
     */
    @TableField("target_priority")
    private Integer targetPriority;
}
