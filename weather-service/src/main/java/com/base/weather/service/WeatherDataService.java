package com.base.weather.service;

import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.req.WeatherDataQueryReq;
import com.base.weather.entity.vo.WeatherDataVo;

import java.util.List;

/**
 * 天气数据服务接口
 */
public interface WeatherDataService {

    /**
     * 查询天气数据
     * <p>
     * 根据查询请求对象查询天气数据（包括历史数据和预报数据）
     * 如果指定了indicatorName，则查询特定指标的数据
     * 如果未指定indicatorName，则查询该策略下所有指标的数据
     *
     * @param queryReq 查询请求对象，包含策略ID、指标名称、时间范围等
     * @return 天气数据VO列表
     */
    List<WeatherDataVo> queryWeatherData(WeatherDataQueryReq queryReq);

    /**
     * 根据策略查询天气数据
     * 
     * 功能说明：
     * 通过天气策略对象查询该策略关联的所有历史天气数据
     * 不限制时间范围，返回该策略的所有历史数据
     *
     * @param strategy 天气策略对象
     * @return 天气数据VO列表
     */
    List<WeatherDataVo> queryWeatherByStrategy(WeatherStrategy strategy);

    /**
     * 采集天气数据（根据策略采集）
     *
     * @param strategy 天气策略
     * @return 采集的天气数据VO列表（按 collectTime 分组）
     */
    List<WeatherDataVo> collectWeatherData(WeatherStrategy strategy);

    /**
     * 根据策略的开始时间和结束时间采集预报数据并入库
     * <p>
     * 功能说明：
     * 1. 解析策略的 collectStart 和 collectEnd 时间范围
     * 2. 计算需要采集的小时数
     * 3. 对于逐小时天气预报，最长168小时，超出则使用168小时
     * 4. 对于空气质量小时预报，固定24小时，超出则使用24小时
     * 5. 调用对应的API获取预报数据
     * 6. 解析响应并入库
     *
     * @param strategy 天气策略对象
     * @return 采集到的天气数据VO列表（按 collectTime 分组）
     */
    List<WeatherDataVo> collectForecastDataByTimeRange(WeatherStrategy strategy);
}
