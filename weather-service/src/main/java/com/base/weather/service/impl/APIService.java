package com.base.weather.service.impl;

import com.alibaba.fastjson.JSON;
import com.base.common.util.HttpUtils;
import com.base.weather.constant.ApiEnum;
import com.base.weather.entity.dto.GeoCityResponse;
import com.base.weather.entity.dto.HourlyAirQualityResponse;
import com.base.weather.entity.dto.HourlyForecastWeatherResponse;
import com.base.weather.entity.dto.MonitoringStationDataResponse;
import com.base.weather.entity.dto.RealTimeAirQualityResponse;
import com.base.weather.entity.dto.RealTimeWeatherResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 和风天气 API 服务
 */
@Slf4j
@Service
public class APIService {

    @Value("${api.hefeng.base}")
    private String baseUrl;

    @Value("${api.hefeng.api-key}")
    private String apiKey;

    /**
     * 获取监测站数据
     *
     * @param location 位置信息，例如：beij（城市名称或拼音）
     * @return 监测站数据响应
     */
    @Deprecated
    public MonitoringStationDataResponse getMonitoringStationData(String location) {
        if (!StringUtils.hasText(location)) {
            throw new IllegalArgumentException("位置信息不能为空");
        }

        try {
            // 1. 先通过 GEO_CITY API 获取 stationId
            String stationId = Objects.requireNonNull(getGeoLocation(location)).get(0).getId();
            log.info("从城市查询结果中获取到 stationId: {}", stationId);
            if (!StringUtils.hasText(stationId)) {
                throw new RuntimeException("未找到对应的城市ID，location: " + location);
            }

            log.info("通过 location [{}] 获取到 stationId: {}", location, stationId);

            // 2. 使用 stationId 调用监测站数据 API
            String url = baseUrl + ApiEnum.MONITORING_STATION_DATA.getUrl() + stationId;

            // 设置请求头（API Key 放在请求头中）
            Map<String, String> headers = new HashMap<>();
            headers.put("X-QW-Api-Key", apiKey);

            // 发送 GET 请求（无URL参数）
            String response = HttpUtils.get(url, null, headers);

            log.info("监测站数据API响应: {}", response);

            // 解析响应
            return JSON.parseObject(response, MonitoringStationDataResponse.class);

        } catch (Exception e) {
            log.error("获取监测站数据失败，location: {}", location, e);
            throw new RuntimeException("获取监测站数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过位置信息获取城市位置信息
     *
     * @param location 位置信息，例如：beij（城市名称或拼音）
     * @return 城市位置信息列表
     */
    public List<GeoCityResponse.Location> getGeoLocation(String location) throws IOException {
        // 构建请求URL
        String url = baseUrl + ApiEnum.GEO_CITY.getUrl();

        // 添加URL参数
        Map<String, String> params = new HashMap<>();
        params.put("location", location);

        // 设置请求头（API Key 放在请求头中）
        Map<String, String> headers = new HashMap<>();
        headers.put("X-QW-Api-Key", apiKey);

        // 发送 GET 请求
        String response = HttpUtils.get(url, params, headers);

        log.info("城市查询API响应: {}", response);

        // 解析响应
        GeoCityResponse cityResponse = JSON.parseObject(response, GeoCityResponse.class);
        // 检查响应码
        if (!"200".equals(cityResponse.getCode())) {
            log.warn("城市查询API返回非200状态码: {}", cityResponse.getCode());
            return null;
        }
        // 获取第一个 location 的 id
        List<GeoCityResponse.Location> locations = cityResponse.getLocation();
        if (locations == null || locations.isEmpty()) {
            log.warn("城市查询API返回的location列表为空，location: {}", location);
            return null;
        }
        return locations;
    }

    /**
     * 获取实时天气数据
     *
     * @param location 位置信息，格式：经度,纬度，例如：116.41,39.92
     * @return 实时天气响应
     */
    public RealTimeWeatherResponse getRealTimeWeather(String location) {
        if (!StringUtils.hasText(location)) {
            throw new IllegalArgumentException("位置信息不能为空");
        }

        try {
            // 构建请求URL
            String url = baseUrl + ApiEnum.REAL_TIME_WEATHER.getUrl();

            // 添加URL参数
            Map<String, String> params = new HashMap<>();
            params.put("location", location);

            // 设置请求头（API Key 放在请求头中）
            Map<String, String> headers = new HashMap<>();
            headers.put("X-QW-Api-Key", apiKey);

            // 发送 GET 请求
            String response = HttpUtils.get(url, params, headers);

            log.info("实时天气API响应: {}", response);

            // 解析响应
            RealTimeWeatherResponse weatherResponse = JSON.parseObject(response, RealTimeWeatherResponse.class);

            // 检查响应码
            if (!"200".equals(weatherResponse.getCode())) {
                log.warn("实时天气API返回非200状态码: {}", weatherResponse.getCode());
            }

            return weatherResponse;

        } catch (Exception e) {
            log.error("获取实时天气数据失败，location: {}", location, e);
            throw new RuntimeException("获取实时天气数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取实时空气质量数据
     *
     * @param latitude  纬度，例如：39.92
     * @param longitude 经度，例如：116.41
     * @return 实时空气质量响应
     */
    public RealTimeAirQualityResponse getRealTimeAirQuality(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("纬度和经度不能为空");
        }

        try {
            // 构建请求URL，格式：/airquality/v1/current/{latitude}/{longitude}
            String url = baseUrl + ApiEnum.REAL_TIME_AIR_QUALITY.getUrl() + latitude + "/" + longitude;

            // 设置请求头（API Key 放在请求头中）
            Map<String, String> headers = new HashMap<>();
            headers.put("X-QW-Api-Key", apiKey);

            // 发送 GET 请求（无URL参数）
            String response = HttpUtils.get(url, null, headers);

            log.info("实时空气质量API响应: {}", response);

            // 解析响应
            return JSON.parseObject(response, RealTimeAirQualityResponse.class);

        } catch (Exception e) {
            log.error("获取实时空气质量数据失败，latitude: {}, longitude: {}", latitude, longitude, e);
            throw new RuntimeException("获取实时空气质量数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取空气质量小时预报数据
     *
     * @param latitude  纬度，例如：39.92
     * @param longitude 经度，例如：116.41
     * @return 空气质量小时预报响应
     */
    public HourlyAirQualityResponse getHourlyAirQuality(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("纬度和经度不能为空");
        }

        try {
            // 构建请求URL，格式：/airquality/v1/hourly/{latitude}/{longitude}
            String url = baseUrl + ApiEnum.HOURLY_AIR_QUALITY.getUrl() + latitude + "/" + longitude;

            // 设置请求头（API Key 放在请求头中）
            Map<String, String> headers = new HashMap<>();
            headers.put("X-QW-Api-Key", apiKey);

            // 发送 GET 请求（无URL参数）
            String response = HttpUtils.get(url, null, headers);

            log.info("空气质量小时预报API响应: {}", response);

            // 解析响应
            return JSON.parseObject(response, HourlyAirQualityResponse.class);

        } catch (Exception e) {
            log.error("获取空气质量小时预报数据失败，latitude: {}, longitude: {}", latitude, longitude, e);
            throw new RuntimeException("获取空气质量小时预报数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取逐小时天气预报数据
     *
     * @param location 位置信息，格式：经度,纬度，例如：116.41,39.92
     * @param hours    预报小时数，可选值：24h、72h、168h
     * @return 逐小时天气预报响应
     */
    public HourlyForecastWeatherResponse getHourlyForecastWeather(String location, String hours) {
        if (!StringUtils.hasText(location)) {
            throw new IllegalArgumentException("位置信息不能为空");
        }
        if (!StringUtils.hasText(hours)) {
            throw new IllegalArgumentException("预报小时数不能为空，可选值：24h、72h、168h");
        }

        // 验证 hours 参数
        if (!hours.equals("24h") && !hours.equals("72h") && !hours.equals("168h")) {
            throw new IllegalArgumentException("预报小时数无效，可选值：24h、72h、168h");
        }

        try {
            // 构建请求URL，格式：/v7/weather/{hours}
            String url = baseUrl + ApiEnum.HOURLY_FORECAST_WEATHER.getUrl() + hours;

            // 添加URL参数
            Map<String, String> params = new HashMap<>();
            params.put("location", location);

            // 设置请求头（API Key 放在请求头中）
            Map<String, String> headers = new HashMap<>();
            headers.put("X-QW-Api-Key", apiKey);

            // 发送 GET 请求
            String response = HttpUtils.get(url, params, headers);

            log.info("逐小时天气预报API响应: {}", response);

            // 解析响应
            HourlyForecastWeatherResponse weatherResponse = JSON.parseObject(response, HourlyForecastWeatherResponse.class);

            // 检查响应码
            if (!"200".equals(weatherResponse.getCode())) {
                log.warn("逐小时天气预报API返回非200状态码: {}", weatherResponse.getCode());
            }

            return weatherResponse;

        } catch (Exception e) {
            log.error("获取逐小时天气预报数据失败，location: {}, hours: {}", location, hours, e);
            throw new RuntimeException("获取逐小时天气预报数据失败: " + e.getMessage(), e);
        }
    }

}
