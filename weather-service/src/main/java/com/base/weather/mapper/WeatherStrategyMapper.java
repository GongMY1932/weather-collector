package com.base.weather.mapper;

import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.req.WeatherStrategyQueryReq;
import com.base.weather.entity.vo.WeatherStrategyVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 天气策略 Mapper 接口
 */
@Mapper
public interface WeatherStrategyMapper extends BaseMapper<WeatherStrategy> {

    /**
     * 查询天气策略（分页）
     *
     * @param page 分页对象
     * @param queryReq 查询请求参数
     * @return 天气策略列表
     */
    Page<WeatherStrategyVo> getList(Page<WeatherStrategyVo> page, @Param("queryReq") WeatherStrategyQueryReq queryReq);

    /**
     * 根据需求名称查询是否存在（排除已删除的记录）
     *
     * @param demandName 需求名称
     * @return 存在的数量
     */
    int countByDemandName(@Param("demandName") String demandName);
}
