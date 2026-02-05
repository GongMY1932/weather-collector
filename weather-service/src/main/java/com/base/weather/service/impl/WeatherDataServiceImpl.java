package com.base.weather.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.base.weather.constant.IndicatorEnum;
import com.base.weather.entity.WeatherData;
import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.dto.GeoCityResponse;
import com.base.weather.entity.dto.HourlyAirQualityResponse;
import com.base.weather.entity.dto.HourlyForecastWeatherResponse;
import com.base.weather.entity.dto.RealTimeAirQualityResponse;
import com.base.weather.entity.dto.RealTimeWeatherResponse;
import com.base.weather.entity.req.WeatherDataQueryReq;
import com.base.weather.entity.vo.WeatherDataVo;
import com.base.weather.mapper.WeatherDataMapper;
import com.base.weather.service.WeatherDataService;
import com.base.weather.service.WeatherStrategyService;
import com.base.weather.util.DateTimeUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气数据服务实现类
 */
@Slf4j
@Service
public class WeatherDataServiceImpl extends ServiceImpl<WeatherDataMapper, WeatherData> implements WeatherDataService {

    @Resource
    private APIService apiService;

    @Resource
    private WeatherStrategyService weatherStrategyService;

    /**
     * 采集天气数据（根据策略采集）
     * <p>
     * 解析策略中的采集内容（多个IndicatorEnum以","分隔，如："Temperature,PM2p5,PM10"）
     * 根据策略的位置信息（经纬度或城市名称）确定查询位置
     * 按API类型分组指标，避免重复调用同一API（优化：每个API类型只调用一次）
     * 根据指标类型调用对应的API（实时天气API或实时空气质量API）
     * 从API响应中提取该API类型下的所有指标值
     * 批量保存到数据库
     * <p>
     *
     * - 如果策略中包含多个相同API类型的指标（如：Temperature, Wind_speed都使用REAL_TIME_WEATHER），
     * 只会调用一次API，然后从响应中提取所有相关指标，避免重复请求浪费资源
     *
     * @param strategy 天气策略对象，包含采集内容、位置信息等
     * @return 采集到的天气数据列表
     */
    @Override
    public List<WeatherDataVo> collectWeatherData(WeatherStrategy strategy) {
        List<WeatherData> weatherDataList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // 解析采集内容（多个IndicatorEnum以","分隔）
        String collectContent = strategy.getCollectContent();
        if (!StringUtils.hasText(collectContent)) {
            log.warn("策略 {} 的采集内容为空", strategy.getId());
            return new ArrayList<>();
        }
        String[] indicatorNames = collectContent.split(",");
        String location;

        if (strategy.getTargetLatitude() != null && strategy.getTargetLongitude() != null) {
            location = strategy.getTargetLongitude() + "," + strategy.getTargetLatitude();
        } else {
            log.warn("策略 {} 没有有效的位置信息", strategy.getId());
            return new ArrayList<>();
        }
        // 按API类型分组采集
        // 先按API类型分组，避免重复调用同一个API
        Map<com.base.weather.constant.ApiEnum, List<IndicatorEnum>> apiGroupMap = new HashMap<>();
        for (String indicatorName : indicatorNames) {
            indicatorName = indicatorName.trim();
            IndicatorEnum indicator = IndicatorEnum.getByName(indicatorName);
            if (indicator == null) {
                log.warn("未找到指标: {}", indicatorName);
                continue;
            }
            apiGroupMap.computeIfAbsent(indicator.getApiEnum(), k -> new ArrayList<>()).add(indicator);
        }

        // 按API类型批量采集
        for (Map.Entry<com.base.weather.constant.ApiEnum, List<IndicatorEnum>> entry : apiGroupMap.entrySet()) {
            com.base.weather.constant.ApiEnum apiEnum = entry.getKey();
            List<IndicatorEnum> indicators = entry.getValue();

            try {
                switch (apiEnum) {
                    case REAL_TIME_WEATHER:
                        // 采集实时天气数据
                        String weatherLocation = location;
                        if (StringUtils.hasText(strategy.getCityName())) {
                            // 如果有城市名称，需要先转换为经纬度
                            List<GeoCityResponse.Location> locations = apiService.getGeoLocation(strategy.getCityName());
                            if (locations != null && !locations.isEmpty()) {
                                GeoCityResponse.Location cityLocation = locations.get(0);
                                weatherLocation = cityLocation.getLon() + "," + cityLocation.getLat();
                            }
                        }
                        RealTimeWeatherResponse weatherResponse = apiService.getRealTimeWeather(weatherLocation);
                        for (IndicatorEnum indicator : indicators) {
                            weatherDataList.addAll(extractWeatherDataFromRealTime(strategy, weatherResponse, indicator, now));
                        }
                        break;

                    case REAL_TIME_AIR_QUALITY:
                        // 采集实时空气质量数据（使用经纬度）
                        Double latitude = strategy.getTargetLatitude();
                        Double longitude = strategy.getTargetLongitude();

                        // 如果有城市名称但没有经纬度，先通过城市名称获取经纬度
                        if ((latitude == null || longitude == null) && StringUtils.hasText(strategy.getCityName())) {
                            List<GeoCityResponse.Location> locations = apiService.getGeoLocation(strategy.getCityName());
                            if (locations != null && !locations.isEmpty()) {
                                GeoCityResponse.Location cityLocation = locations.get(0);
                                latitude = Double.parseDouble(cityLocation.getLat());
                                longitude = Double.parseDouble(cityLocation.getLon());
                            }
                        }

                        if (latitude != null && longitude != null) {
                            RealTimeAirQualityResponse airQualityResponse = apiService.getRealTimeAirQuality(latitude, longitude);
                            for (IndicatorEnum indicator : indicators) {
                                weatherDataList.addAll(extractWeatherDataFromAirQuality(strategy, airQualityResponse, indicator, now));
                            }
                        } else {
                            log.warn("策略 {} 没有有效的经纬度信息，无法调用实时空气质量API", strategy.getId());
                        }
                        break;

                    default:
                        log.warn("不支持的API类型: {}", apiEnum);
                        break;
                }
            } catch (Exception e) {
                log.error("采集API类型 {} 失败，策略ID: {}", apiEnum, strategy.getId(), e);
            }
        }
        // 批量保存
        if (!weatherDataList.isEmpty()) {
            saveBatch(weatherDataList);
            log.info("策略 {} 采集了 {} 条天气数据", strategy.getId(), weatherDataList.size());
            // 注意：除“新增策略/取消/定时到期”外，不允许修改策略采集状态
        }
        // 转换为 WeatherDataVo（按 collectTime 分组）
        return convertToWeatherDataVoList(weatherDataList);
    }

    /**
     * 根据策略的开始时间和结束时间采集预报数据并入库
     * <p>
     * 解析策略的 collectStart 和 collectEnd 时间范围
     * 计算需要采集的小时数
     * 对于逐小时天气预报，最长168小时，超出则使用168小时
     * 对于空气质量小时预报，固定24小时，超出则使用24小时
     * 调用对应的API获取预报数据
     * 解析响应并入库
     *
     * @param strategy 天气策略对象
     * @return 采集到的天气数据VO列表（按 collectTime 分组）
     */
    public List<WeatherDataVo> collectForecastDataByTimeRange(WeatherStrategy strategy) {
        List<WeatherData> weatherDataList = new ArrayList<>();

        // 检查策略的时间范围
        if (!StringUtils.hasText(strategy.getCollectStart()) || !StringUtils.hasText(strategy.getCollectEnd())) {
            log.warn("策略 {} 的采集开始时间或结束时间为空", strategy.getId());
            return new ArrayList<>();
        }

        // 解析时间字符串
        LocalDateTime startTime = DateTimeUtils.parseDateTime(strategy.getCollectStart());
        LocalDateTime endTime = DateTimeUtils.parseDateTime(strategy.getCollectEnd());

        if (startTime == null || endTime == null) {
            log.error("策略 {} 的时间格式解析失败，collectStart: {}, collectEnd: {}",
                    strategy.getId(), strategy.getCollectStart(), strategy.getCollectEnd());
            return new ArrayList<>();
        }

        // 检查时间范围是否有效
        if (endTime.isBefore(startTime)) {
            log.warn("策略 {} 的结束时间早于开始时间", strategy.getId());
            return new ArrayList<>();
        }

        // 计算时间范围（小时数）
        long hoursBetween = ChronoUnit.HOURS.between(startTime, endTime);
        if (hoursBetween <= 0) {
            log.warn("策略 {} 的时间范围无效或为0", strategy.getId());
            return new ArrayList<>();
        }

        // 检查位置信息
        if (strategy.getTargetLatitude() == null || strategy.getTargetLongitude() == null) {
            log.warn("策略 {} 没有有效的位置信息", strategy.getId());
            return new ArrayList<>();
        }
        String location = strategy.getTargetLongitude() + "," + strategy.getTargetLatitude();
        Double latitude = strategy.getTargetLatitude();
        Double longitude = strategy.getTargetLongitude();

        // 解析采集内容，判断需要调用哪些API
        String collectContent = strategy.getCollectContent();
        if (!StringUtils.hasText(collectContent)) {
            log.warn("策略 {} 的采集内容为空", strategy.getId());
            return new ArrayList<>();
        }

        String[] indicatorNames = collectContent.split(",");
        boolean needWeatherForecast = false;
        boolean needAirQualityForecast = false;
        // 本次预报采集涉及的指标集合（用于覆盖删除旧数据）
        List<String> forecastIndicatorNames = new ArrayList<>();

        // 判断需要哪些预报API
        for (String indicatorName : indicatorNames) {
            indicatorName = indicatorName.trim();
            IndicatorEnum indicator = IndicatorEnum.getByName(indicatorName);
            if (indicator == null) {
                log.warn("策略 {} 中未找到指标: {}", strategy.getId(), indicatorName);
                continue;
            }
            forecastIndicatorNames.add(indicator.name());
            log.debug("策略 {} 找到指标: {} (API类型: {})", strategy.getId(), indicatorName, indicator.getApiEnum());
            // 实时天气指标需要逐小时天气预报
            if (indicator.getApiEnum() == com.base.weather.constant.ApiEnum.REAL_TIME_WEATHER) {
                needWeatherForecast = true;
                log.debug("策略 {} 需要逐小时天气预报", strategy.getId());
            }
            // 空气质量指标需要空气质量小时预报
            if (indicator.getApiEnum() == com.base.weather.constant.ApiEnum.REAL_TIME_AIR_QUALITY) {
                needAirQualityForecast = true;
                log.debug("策略 {} 需要空气质量小时预报", strategy.getId());
            }
        }

        log.info("策略 {} 需要采集的API类型 - 逐小时天气预报: {}, 空气质量小时预报: {}",
                strategy.getId(), needWeatherForecast, needAirQualityForecast);

        // 采集逐小时天气预报数据
        if (needWeatherForecast) {
            try {
                // 计算需要的小时数（最长168小时）
                long forecastHours = Math.min(hoursBetween, 168);
                // 选择最接近的API参数（24h、72h、168h）
                String hoursParam;
                if (forecastHours <= 24) {
                    hoursParam = "24h";
                } else if (forecastHours <= 72) {
                    hoursParam = "72h";
                } else {
                    hoursParam = "168h";
                }

                log.info("策略 {} 开始采集逐小时天气预报，时间范围: {} 到 {}，预报小时数: {}",
                        strategy.getId(), startTime, endTime, hoursParam);

                HourlyForecastWeatherResponse response = apiService.getHourlyForecastWeather(location, hoursParam);
                if (response != null && response.getHourly() != null) {
                    for (HourlyForecastWeatherResponse.Hourly hourly : response.getHourly()) {
                        // 解析预报时间
                        LocalDateTime forecastTime = DateTimeUtils.parseForecastTime(hourly.getFxTime());
                        if (forecastTime == null) {
                            continue;
                        }
                        // 只保存时间范围内的数据
                        if (forecastTime.isBefore(startTime) || forecastTime.isAfter(endTime)) {
                            continue;
                        }

                        // 提取天气指标数据
                        extractHourlyWeatherData(strategy, hourly, forecastTime, indicatorNames, weatherDataList);
                    }
                }
            } catch (Exception e) {
                log.error("策略 {} 采集逐小时天气预报失败", strategy.getId(), e);
            }
        }

        // 采集空气质量小时预报数据
        if (needAirQualityForecast) {
            try {
                // 空气质量小时预报固定24小时
                log.info("策略 {} 开始采集空气质量小时预报，时间范围: {} 到 {}",
                        strategy.getId(), startTime, endTime);

                HourlyAirQualityResponse response = apiService.getHourlyAirQuality(latitude, longitude);
                if (response != null && response.getHours() != null) {
                    for (HourlyAirQualityResponse.Hour hour : response.getHours()) {
                        // 解析预报时间
                        LocalDateTime forecastTime = DateTimeUtils.parseForecastTime(hour.getForecastTime());
                        if (forecastTime == null) {
                            continue;
                        }
                        // 只保存时间范围内的数据（空气质量预报固定24小时，可能超出策略时间范围）
                        if (forecastTime.isBefore(startTime) || forecastTime.isAfter(endTime)) {
                            continue;
                        }

                        // 提取空气质量指标数据
                        extractHourlyAirQualityData(strategy, hour, forecastTime, indicatorNames, weatherDataList);
                    }
                }
            } catch (Exception e) {
                log.error("策略 {} 采集空气质量小时预报失败", strategy.getId(), e);
            }
        }

        // 入库前：先物理删除旧预报数据（同策略、同时间范围、同一批指标），避免重复和数据库数据过多
        if (!forecastIndicatorNames.isEmpty()) {
            try {
                int affected = baseMapper.deleteByStrategyAndTimeRangeAndIndicators(
                        strategy.getId(), startTime, endTime, forecastIndicatorNames);
                log.info("策略 {} 预报入库前物理删除旧数据完成，影响行数: {}", strategy.getId(), affected);
            } catch (Exception e) {
                log.error("策略 {} 预报入库前物理删除旧数据失败（将继续尝试入库，可能产生重复数据）", strategy.getId(), e);
            }
        }

        // 批量保存（新预报数据）
        if (!weatherDataList.isEmpty()) {
            saveBatch(weatherDataList);
            log.info("策略 {} 采集了 {} 条预报天气数据", strategy.getId(), weatherDataList.size());
            // 注意：除“新增策略/取消/定时到期”外，不允许修改策略采集状态
        }

        // 转换为 WeatherDataVo（按 collectTime 分组）
        return convertToWeatherDataVoList(weatherDataList);
    }

    // 注意：根据当前业务约束，除“新增策略/取消/定时到期”外，不允许在采集流程中修改策略采集状态。

    /**
     * 从逐小时天气预报响应中提取天气数据
     *
     * @param strategy        天气策略对象
     * @param hourly          逐小时天气预报数据
     * @param forecastTime    预报时间
     * @param indicatorNames  需要采集的指标名称列表
     * @param weatherDataList 天气数据列表（用于收集数据）
     */
    private void extractHourlyWeatherData(WeatherStrategy strategy,
                                          HourlyForecastWeatherResponse.Hourly hourly,
                                          LocalDateTime forecastTime,
                                          String[] indicatorNames,
                                          List<WeatherData> weatherDataList) {
        for (String indicatorName : indicatorNames) {
            indicatorName = indicatorName.trim();
            IndicatorEnum indicator = IndicatorEnum.getByName(indicatorName);
            if (indicator == null || indicator.getApiEnum() != com.base.weather.constant.ApiEnum.REAL_TIME_WEATHER) {
                continue;
            }

            String value = null;
            switch (indicator) {
                case Temperature:
                    value = hourly.getTemp();
                    break;
                case Perceived_temperature:
                    // 逐小时预报中没有体感温度，跳过
                    continue;
                case Wind_speed:
                    value = hourly.getWindSpeed();
                    break;
                case Wind_direction:
                    value = hourly.getWindDir();
                    break;
                case Relative_humidify:
                    value = hourly.getHumidity();
                    break;
                case Atmospheric_pressure:
                    value = hourly.getPressure();
                    break;
                case Precipitation:
                    value = hourly.getPrecip();
                    break;
                case Visibility:
                    // 逐小时预报中没有能见度，跳过
                    continue;
                case Dew_point_temperature:
                    value = hourly.getDew();
                    break;
                case Cloud_cover:
                    value = hourly.getCloud();
                    break;
                default:
                    continue;
            }

            if (value != null) {
                String unit = indicator.getUnit();
                WeatherData data = createWeatherData(strategy, indicator.name(), value, unit, forecastTime);
                weatherDataList.add(data);
            }
        }
    }

    /**
     * 从空气质量小时预报响应中提取天气数据
     *
     * @param strategy        天气策略对象
     * @param hour            空气质量小时预报数据
     * @param forecastTime    预报时间
     * @param indicatorNames  需要采集的指标名称列表
     * @param weatherDataList 天气数据列表（用于收集数据）
     */
    private void extractHourlyAirQualityData(WeatherStrategy strategy,
                                             HourlyAirQualityResponse.Hour hour,
                                             LocalDateTime forecastTime,
                                             String[] indicatorNames,
                                             List<WeatherData> weatherDataList) {
        if (hour.getPollutants() == null || hour.getPollutants().isEmpty()) {
            log.warn("策略 {} 预报时间 {} 的污染物数据为空", strategy.getId(), forecastTime);
            return;
        }

        // 创建污染物代码到污染物对象的映射
        Map<String, HourlyAirQualityResponse.Pollutant> pollutantMap = new HashMap<>();
        for (HourlyAirQualityResponse.Pollutant pollutant : hour.getPollutants()) {
            if (pollutant.getCode() != null) {
                pollutantMap.put(pollutant.getCode().toLowerCase(), pollutant);
                log.debug("策略 {} 找到污染物: {} (code: {})", strategy.getId(), pollutant.getName(), pollutant.getCode());
            }
        }

        log.debug("策略 {} 预报时间 {} 的污染物映射: {}", strategy.getId(), forecastTime, pollutantMap.keySet());

        // 指标名称到污染物代码的映射
        Map<IndicatorEnum, String> indicatorCodeMap = new HashMap<>();
        indicatorCodeMap.put(IndicatorEnum.PM2p5, "pm2p5");
        indicatorCodeMap.put(IndicatorEnum.PM10, "pm10");
        indicatorCodeMap.put(IndicatorEnum.CO, "co");
        indicatorCodeMap.put(IndicatorEnum.SO2, "so2");
        indicatorCodeMap.put(IndicatorEnum.O3, "o3");
        indicatorCodeMap.put(IndicatorEnum.NO2, "no2");

        int extractedCount = 0;
        for (String indicatorName : indicatorNames) {
            indicatorName = indicatorName.trim();
            IndicatorEnum indicator = IndicatorEnum.getByName(indicatorName);
            if (indicator == null) {
                log.debug("策略 {} 未找到指标: {}", strategy.getId(), indicatorName);
                continue;
            }
            if (indicator.getApiEnum() != com.base.weather.constant.ApiEnum.REAL_TIME_AIR_QUALITY) {
                log.debug("策略 {} 指标 {} 不是空气质量指标，跳过", strategy.getId(), indicatorName);
                continue;
            }

            String code = indicatorCodeMap.get(indicator);
            if (code == null) {
                log.warn("策略 {} 指标 {} 没有对应的污染物代码映射", strategy.getId(), indicatorName);
                continue;
            }

            HourlyAirQualityResponse.Pollutant pollutant = pollutantMap.get(code);
            if (pollutant == null) {
                log.warn("策略 {} 预报时间 {} 未找到污染物代码: {} (指标: {})",
                        strategy.getId(), forecastTime, code, indicatorName);
                continue;
            }
            if (pollutant.getConcentration() == null) {
                log.warn("策略 {} 预报时间 {} 污染物 {} 的浓度数据为空",
                        strategy.getId(), forecastTime, code);
                continue;
            }
            if (pollutant.getConcentration().getValue() == null) {
                log.warn("策略 {} 预报时间 {} 污染物 {} 的浓度值为空",
                        strategy.getId(), forecastTime, code);
                continue;
            }

            String value = String.valueOf(pollutant.getConcentration().getValue());
            // 优先使用枚举中定义的单位，如果枚举中没有则使用API返回的单位
            String unit = indicator.getUnit() != null ? indicator.getUnit() : pollutant.getConcentration().getUnit();
            WeatherData data = createWeatherData(strategy, indicator.name(), value, unit, forecastTime);
            weatherDataList.add(data);
            extractedCount++;
            log.debug("策略 {} 提取空气质量指标: {} = {} {}", strategy.getId(), indicatorName, value, unit);
        }

        if (extractedCount == 0) {
            log.warn("策略 {} 预报时间 {} 没有提取到任何空气质量数据，指标列表: {}",
                    strategy.getId(), forecastTime, String.join(", ", indicatorNames));
        } else {
            log.debug("策略 {} 预报时间 {} 提取了 {} 条空气质量数据", strategy.getId(), forecastTime, extractedCount);
        }
    }

    /**
     * 从实时天气响应中提取天气数据
     * <p>
     * 根据指定的指标枚举，从实时天气API的响应中提取对应的指标值
     * 支持的指标包括：温度、体感温度、风速、风向、湿度、气压、降水量、能见度、露点温度、云量
     *
     * @param strategy    天气策略对象
     * @param response    实时天气API响应对象
     * @param indicator   要提取的指标枚举（如：Temperature, Wind_speed等）
     * @param collectTime 采集时间
     * @return 提取到的天气数据列表（通常只有一条数据）
     */
    private List<WeatherData> extractWeatherDataFromRealTime(WeatherStrategy strategy,
                                                             RealTimeWeatherResponse response,
                                                             IndicatorEnum indicator,
                                                             LocalDateTime collectTime) {
        List<WeatherData> dataList = new ArrayList<>();
        if (response == null || response.getNow() == null) {
            return dataList;
        }
        RealTimeWeatherResponse.Now now = response.getNow();
        String value;
        // 根据指标获取对应的值
        switch (indicator) {
            case Temperature:
                value = now.getTemp();
                break;
            case Perceived_temperature:
                value = now.getFeelsLike();
                break;
            case Wind_speed:
                value = now.getWindSpeed();
                break;
            case Wind_direction:
                value = now.getWindDir();
                break;
            case Relative_humidify:
                value = now.getHumidity();
                break;
            case Atmospheric_pressure:
                value = now.getPressure();
                break;
            case Precipitation:
                value = now.getPrecip();
                break;
            case Visibility:
                value = now.getVis();
                break;
            case Dew_point_temperature:
                value = now.getDew();
                break;
            case Cloud_cover:
                value = now.getCloud();
                break;
            default:
                log.warn("实时天气API不支持指标: {}", indicator.name());
                return dataList;
        }
        // 从枚举中获取单位
        String unit = indicator.getUnit();
        if (value != null) {
            WeatherData data = createWeatherData(strategy, indicator.name(), value, unit, collectTime);
            dataList.add(data);
        }
        return dataList;
    }

    /**
     * 从实时空气质量响应中提取天气数据
     * <p>
     * 从实时空气质量API的响应中提取污染物指标值
     * 支持的指标包括：PM2.5、PM10、CO、SO2、O3、NO2
     * 通过匹配污染物代码（code）来找到对应的指标值
     *
     * @param strategy    天气策略对象
     * @param response    实时空气质量API响应对象，包含污染物列表
     * @param indicator   要提取的指标枚举（PM2p5、PM10、CO、SO2、O3、NO2）
     * @param collectTime 采集时间
     * @return 提取到的天气数据列表（通常只有一条数据）
     */
    private List<WeatherData> extractWeatherDataFromAirQuality(WeatherStrategy strategy,
                                                               RealTimeAirQualityResponse response,
                                                               IndicatorEnum indicator,
                                                               LocalDateTime collectTime) {
        List<WeatherData> dataList = new ArrayList<>();
        if (response == null || response.getPollutants() == null) {
            return dataList;
        }

        // 指标名称到污染物代码的映射
        Map<IndicatorEnum, String> indicatorCodeMap = new HashMap<>();
        indicatorCodeMap.put(IndicatorEnum.PM2p5, "pm2p5");
        indicatorCodeMap.put(IndicatorEnum.PM10, "pm10");
        indicatorCodeMap.put(IndicatorEnum.CO, "co");
        indicatorCodeMap.put(IndicatorEnum.SO2, "so2");
        indicatorCodeMap.put(IndicatorEnum.O3, "o3");
        indicatorCodeMap.put(IndicatorEnum.NO2, "no2");

        String targetCode = indicatorCodeMap.get(indicator);
        if (targetCode == null) {
            log.warn("实时空气质量API不支持指标: {}", indicator.name());
            return dataList;
        }

        // 查找匹配的污染物
        for (RealTimeAirQualityResponse.Pollutant pollutant : response.getPollutants()) {
            String code = pollutant.getCode();
            if (code == null) {
                continue;
            }
            // 匹配指标代码（不区分大小写）
            if (targetCode.equalsIgnoreCase(code) && pollutant.getConcentration() != null) {
                String value = String.valueOf(pollutant.getConcentration().getValue());
                // 优先使用枚举中定义的单位，如果枚举中没有则使用API返回的单位
                String unit = indicator.getUnit() != null ? indicator.getUnit() : pollutant.getConcentration().getUnit();
                WeatherData data = createWeatherData(strategy, indicator.name(), value, unit, collectTime);
                dataList.add(data);
                break;
            }
        }

        return dataList;
    }

    /**
     * 创建天气数据对象
     * <p>
     * 根据策略信息和指标数据，创建一个完整的WeatherData对象
     * 包含策略关联信息、位置信息、指标信息、时间信息等
     *
     * @param strategy       天气策略对象，用于获取策略ID、城市名称、经纬度等信息
     * @param indicatorName  指标名称（IndicatorEnum的name，如："Temperature"）
     * @param indicatorValue 指标值（字符串格式）
     * @param indicatorUnit  指标单位（如："℃"、"km/h"等）
     * @param collectTime    采集时间
     * @return 创建好的WeatherData对象
     */
    private WeatherData createWeatherData(WeatherStrategy strategy, String indicatorName,
                                          String indicatorValue, String indicatorUnit,
                                          LocalDateTime collectTime) {
        WeatherData data = new WeatherData();
        data.setStrategyId(strategy.getId());
        data.setCityName(strategy.getCityName());
        data.setLatitude(strategy.getTargetLatitude());
        data.setLongitude(strategy.getTargetLongitude());
        data.setCollectTime(collectTime);
        data.setIndicatorName(indicatorName);
        data.setIndicatorValue(indicatorValue);
        data.setIndicatorUnit(indicatorUnit);
        data.setCreateTime(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        data.setDelFlag(0);
        return data;
    }

    /**
     * 查询天气数据
     * <p>
     * 根据查询请求对象查询天气数据（包括历史数据和预报数据）
     * 如果指定了indicatorName，则查询特定指标的数据
     * 如果未指定indicatorName，则查询该策略下所有指标的数据
     *
     * @param queryReq 查询请求对象，包含策略ID、指标名称、时间范围等
     * @return 符合条件的天气数据VO列表
     */
    @Override
    public List<WeatherDataVo> queryWeatherData(WeatherDataQueryReq queryReq) {
        if (queryReq == null || !StringUtils.hasText(queryReq.getStrategyId())) {
            return new ArrayList<>();
        }
        if (StringUtils.hasText(queryReq.getIndicatorName())) {
            // 查询特定指标的历史数据
            return baseMapper.getHistoryByStrategyIdAndIndicator(queryReq.getStrategyId(), queryReq.getIndicatorName(),
                    queryReq.getStartTime(), queryReq.getEndTime());
        } else {
            // 查询所有指标的历史数据
            return baseMapper.getHistoryByStrategyId(queryReq.getStrategyId(), queryReq.getStartTime(), queryReq.getEndTime());
        }
    }

    /**
     * 根据策略查询天气数据
     * <p>
     * 通过天气策略对象查询该策略关联的所有历史天气数据
     * 不限制时间范围，返回该策略的所有历史数据
     *
     * @param strategy 天气策略对象
     * @return 该策略关联的所有天气数据VO列表，按采集时间倒序排列
     */
    @Override
    public List<WeatherDataVo> queryWeatherByStrategy(WeatherStrategy strategy) {
        if (strategy == null || !StringUtils.hasText(strategy.getId())) {
            return new ArrayList<>();
        }
        return baseMapper.getHistoryByStrategyId(strategy.getId(), null, null);
    }

    /**
     * 将 WeatherData 列表转换为 WeatherDataVo 列表（按 collectTime 分组）
     * <p>
     * 将多个 WeatherData 对象（同一时间点的不同指标）合并为一个 WeatherDataVo 对象
     * 每个 WeatherDataVo 代表一个 collectTime 的所有指标值
     *
     * @param weatherDataList 天气数据列表
     * @return 天气数据VO列表（按 collectTime 分组）
     */
    private List<WeatherDataVo> convertToWeatherDataVoList(List<WeatherData> weatherDataList) {
        if (weatherDataList == null || weatherDataList.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 collectTime 分组
        Map<LocalDateTime, WeatherDataVo> voMap = new HashMap<>();
        for (WeatherData data : weatherDataList) {
            LocalDateTime collectTime = data.getCollectTime();
            WeatherDataVo vo = voMap.computeIfAbsent(collectTime, k -> {
                WeatherDataVo newVo = new WeatherDataVo();
                newVo.setStrategyId(data.getStrategyId());
                newVo.setCityName(data.getCityName());
                newVo.setLatitude(data.getLatitude());
                newVo.setLongitude(data.getLongitude());
                newVo.setCollectTime(collectTime);
                return newVo;
            });

            // 根据指标名称设置对应的字段值
            String indicatorName = data.getIndicatorName();
            String indicatorValue = data.getIndicatorValue();
            if (indicatorName != null && indicatorValue != null) {
                switch (indicatorName) {
                    case "Temperature":
                        vo.setTemperature(indicatorValue);
                        break;
                    case "Perceived_temperature":
                        vo.setPerceivedTemperature(indicatorValue);
                        break;
                    case "Wind_speed":
                        vo.setWindSpeed(indicatorValue);
                        break;
                    case "Wind_direction":
                        vo.setWindDirection(indicatorValue);
                        break;
                    case "Relative_humidify":
                        vo.setRelativeHumidify(indicatorValue);
                        break;
                    case "Atmospheric_pressure":
                        vo.setAtmosphericPressure(indicatorValue);
                        break;
                    case "Precipitation":
                        vo.setPrecipitation(indicatorValue);
                        break;
                    case "Visibility":
                        vo.setVisibility(indicatorValue);
                        break;
                    case "Dew_point_temperature":
                        vo.setDewPointTemperature(indicatorValue);
                        break;
                    case "Cloud_cover":
                        vo.setCloudCover(indicatorValue);
                        break;
                    case "PM2p5":
                        vo.setPm2p5(indicatorValue);
                        break;
                    case "PM10":
                        vo.setPm10(indicatorValue);
                        break;
                    case "CO":
                        vo.setCo(indicatorValue);
                        break;
                    case "SO2":
                        vo.setSo2(indicatorValue);
                        break;
                    case "O3":
                        vo.setO3(indicatorValue);
                        break;
                    case "NO2":
                        vo.setNo2(indicatorValue);
                        break;
                    default:
                        log.warn("未知的指标名称: {}", indicatorName);
                        break;
                }
            }
        }

        // 转换为列表并按 collectTime 降序排列
        List<WeatherDataVo> result = new ArrayList<>(voMap.values());
        result.sort((a, b) -> {
            if (a.getCollectTime() == null && b.getCollectTime() == null) {
                return 0;
            }
            if (a.getCollectTime() == null) {
                return 1;
            }
            if (b.getCollectTime() == null) {
                return -1;
            }
            return b.getCollectTime().compareTo(a.getCollectTime());
        });
        return result;
    }
}
