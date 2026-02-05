package com.base.weather.controller;

import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.req.WeatherDataQueryReq;
import com.base.weather.entity.vo.WeatherDataVo;
import com.base.weather.service.WeatherDataService;
import com.base.weather.service.WeatherStrategyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 天气数据 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/weather-data")
public class WeatherDataController {

    @Resource
    private WeatherDataService weatherDataService;

    @Resource
    private WeatherStrategyService weatherStrategyService;

    /**
     * 查询天气数据
     * <p>
     * 根据策略ID查询天气数据（包括历史数据和预报数据），支持按指标名称、时间范围等条件筛选
     * 如果指定了indicatorName，则查询特定指标的数据
     * 如果未指定indicatorName，则查询该策略下所有指标的数据
     *
     * @param queryReq 查询请求对象，包含策略ID、指标名称、时间范围等
     * @return 天气数据VO列表
     */
    @PostMapping("/weather-data")
    public ResponseEntity<List<WeatherDataVo>> queryWeatherData(@RequestBody WeatherDataQueryReq queryReq) {
        log.info("查询天气数据，请求参数: {}", queryReq);
        if (!StringUtils.hasText(queryReq.getStrategyId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // 先查询数据
        List<WeatherDataVo> list = weatherDataService.queryWeatherData(queryReq);
        
        // 如果数据库没有数据，手动触发一次预报采集
        if (list == null || list.isEmpty()) {
            log.info("策略 {} 查询结果为空，尝试手动触发预报数据采集", queryReq.getStrategyId());
            try {
                WeatherStrategy strategy = weatherStrategyService.getById(queryReq.getStrategyId());
                if (strategy != null 
                        && StringUtils.hasText(strategy.getCollectStart()) 
                        && StringUtils.hasText(strategy.getCollectEnd())) {
                    log.info("策略 {} 具备采集条件，开始触发预报数据采集", queryReq.getStrategyId());
                    weatherDataService.collectForecastDataByTimeRange(strategy);
                    // 采集完成后再次查询
                    list = weatherDataService.queryWeatherData(queryReq);
                    log.info("策略 {} 预报数据采集完成，重新查询到 {} 条数据", queryReq.getStrategyId(), 
                            list != null ? list.size() : 0);
                } else {
                    log.warn("策略 {} 不具备采集条件（缺少 collectStart 或 collectEnd），跳过预报采集", 
                            queryReq.getStrategyId());
                }
            } catch (Exception e) {
                log.error("策略 {} 手动触发预报数据采集失败", queryReq.getStrategyId(), e);
                // 采集失败不影响查询接口返回，继续返回空列表
            }
        }
        
        return ResponseEntity.ok(list);
    }

    /**
     * 根据策略查询天气数据
     * <p>
     * 通过策略ID查询该策略关联的所有历史天气数据
     * 不限制时间范围，返回该策略的所有历史数据
     *
     * @param queryReq 查询请求对象，包含策略ID
     * @return 天气数据VO列表
     */
    @PostMapping("/strategy")
    public ResponseEntity<List<WeatherDataVo>> getWeatherByStrategy(@RequestBody WeatherDataQueryReq queryReq) {
        log.info("通过策略查询天气数据，请求参数: {}", queryReq);
        if (!StringUtils.hasText(queryReq.getStrategyId())) {
            return ResponseEntity.badRequest().build();
        }
        WeatherStrategy strategy = weatherStrategyService.getById(queryReq.getStrategyId());
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        List<WeatherDataVo> result = weatherDataService.queryWeatherByStrategy(strategy);
        return ResponseEntity.ok(result);
    }

    /**
     * 手动触发天气数据采集（根据策略ID）
     *
     * @param strategyId 策略ID
     * @return 采集的天气数据VO列表（按 collectTime 分组）
     */
    @PostMapping("/collect/{strategyId}")
    public ResponseEntity<List<WeatherDataVo>> collectWeatherData(@PathVariable String strategyId) {
        log.info("手动触发策略 [{}] 的天气数据采集", strategyId);
        WeatherStrategy strategy = weatherStrategyService.getById(strategyId);
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        List<WeatherDataVo> result = weatherDataService.collectWeatherData(strategy);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据策略的时间范围采集预报数据并入库
     * <p>
     * 根据策略的 collectStart 和 collectEnd 时间范围采集预报数据
     * 对于逐小时天气预报，最长168小时，超出则使用168小时
     * 对于空气质量小时预报，固定24小时，超出则使用24小时
     * 自动判断需要调用哪些预报API（根据策略的采集内容）
     * 只保存时间范围内的数据
     *
     * @param strategyId 策略ID
     * @return 采集的天气数据VO列表（按 collectTime 分组）
     */
    @PostMapping("/collect-forecast/{strategyId}")
    public ResponseEntity<List<WeatherDataVo>> collectForecastDataByTimeRange(@PathVariable String strategyId) {
        log.info("手动触发策略 [{}] 的预报数据采集（按时间范围）", strategyId);
        WeatherStrategy strategy = weatherStrategyService.getById(strategyId);
        if (strategy == null) {
            return ResponseEntity.notFound().build();
        }
        if (!StringUtils.hasText(strategy.getCollectStart()) || !StringUtils.hasText(strategy.getCollectEnd())) {
            return ResponseEntity.badRequest().build();
        }
        List<WeatherDataVo> result = weatherDataService.collectForecastDataByTimeRange(strategy);
        return ResponseEntity.ok(result);
    }
}
