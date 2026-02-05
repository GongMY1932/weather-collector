package com.base.weather.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.base.common.entity.base.PaginationEntity;
import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.dto.BatchImportResult;
import com.base.weather.entity.req.WeatherStrategyAddReq;
import com.base.weather.entity.req.WeatherStrategyQueryReq;
import com.base.weather.entity.vo.WeatherStrategyVo;

import java.util.List;

/**
 * 天气策略 Service 接口
 */
public interface WeatherStrategyService extends IService<WeatherStrategy> {


    boolean add(WeatherStrategyAddReq weatherStrategyAddReq);

    boolean batchAdd(List<WeatherStrategyAddReq> weatherStrategys);

    boolean update(WeatherStrategyAddReq weatherStrategyAddReq);

    boolean delete(String id);

    /**
     * 取消策略（不删除记录，只将 collect_status 置为已取消）
     *
     * @param id 策略ID
     * @return 取消结果
     */
    boolean cancel(String id);


    /**
     * 获取天气策略列表
     *
     * @param weatherStrategyQueryReq 查询请求参数
     * @return 天气策略列表
     */
    List<WeatherStrategyVo> getList(WeatherStrategyQueryReq weatherStrategyQueryReq);

    /**
     * 分页查询天气策略
     *
     * @param queryReq 查询请求参数
     * @return 分页结果
     */
    PaginationEntity<WeatherStrategyVo> pageQuery(WeatherStrategyQueryReq queryReq);


    /**
     * 批量导入天气策略
     *
     * @param strategyList 策略列表
     * @return 导入结果，包含成功数量、重复信息等
     */
    BatchImportResult batchImportWithDetails(List<WeatherStrategyAddReq> strategyList);
}
