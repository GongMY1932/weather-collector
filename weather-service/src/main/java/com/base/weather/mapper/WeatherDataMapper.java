package com.base.weather.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.base.weather.entity.WeatherData;
import com.base.weather.entity.vo.WeatherDataVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 天气数据Mapper
 */
@Mapper
public interface WeatherDataMapper extends BaseMapper<WeatherData> {

    /**
     * 根据策略ID查询历史天气数据
     *
     * @param strategyId 策略ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 天气数据VO列表
     */
    List<WeatherDataVo> getHistoryByStrategyId(@Param("strategyId") String strategyId,
                                                @Param("startTime") Date startTime,
                                                @Param("endTime") Date endTime);

    /**
     * 根据策略ID和指标名称查询历史天气数据
     *
     * @param strategyId    策略ID
     * @param indicatorName 指标名称
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @return 天气数据VO列表
     */
    List<WeatherDataVo> getHistoryByStrategyIdAndIndicator(@Param("strategyId") String strategyId,
                                                            @Param("indicatorName") String indicatorName,
                                                            @Param("startTime") Date startTime,
                                                            @Param("endTime") Date endTime);

    /**
     * 物理删除旧的预报数据（同一策略、同一时间范围、同一批指标）
     * <p>
     * 说明：预报数据与实时采集共用 weather_data 表，为避免误删实时数据，
     * 这里仅对"本次预报采集涉及的指标集合"且"collect_time 在策略时间范围内"的数据做物理删除。
     *
     * @param strategyId      策略ID
     * @param startTime       开始时间（包含）
     * @param endTime         结束时间（包含）
     * @param indicatorNames  指标名称集合（IndicatorEnum.name）
     * @return 影响行数
     */
    int deleteByStrategyAndTimeRangeAndIndicators(@Param("strategyId") String strategyId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("indicatorNames") List<String> indicatorNames);
}
