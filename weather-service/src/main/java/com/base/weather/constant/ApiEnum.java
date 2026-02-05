package com.base.weather.constant;

import lombok.Getter;

/**
 * 天气预报api枚举类
 */
@Getter
public enum ApiEnum {

    REAL_TIME_WEATHER("实时天气", "get", "/v7/weather/now?"),//https://p43qqr6wkg.re.qweatherapi.com//v7/weather/now?location=116.41,39.92

    REAL_TIME_AIR_QUALITY("实时空气质量", "get", "/airquality/v1/current/"),//https://p43qqr6wkg.re.qweatherapi.com/airquality/v1/current/39.92/116.41

    HOURLY_FORECAST_WEATHER("逐小时天气预报", "get", "/v7/weather/"),//https://p43qqr6wkg.re.qweatherapi.com/v7/weather/{hours}?location=116.41,39.92

    HOURLY_AIR_QUALITY("空气质量小时预报", "get", "/airquality/v1/hourly/"),//https://p43qqr6wkg.re.qweatherapi.com/airquality/v1/hourly/{latitude}/{longitude}

    MONITORING_STATION_DATA("监测站数据", "get", "/airquality/v1/station/"),//https://p43qqr6wkg.re.qweatherapi.com//airquality/v1/station/P53763

    SOLAR_RADIATION_FORECAST("太阳辐射预报", "get", "/solarradiation/v1/forecast/{latitude}/{longitude}"),



    GEO_CITY("城市查询", "get", "/geo/v2/city/lookup");

    private final String description;
    private final String request;
    private final String url;


    ApiEnum(String description, String request, String url) {
        this.description = description;
        this.request = request;
        this.url = url;
    }

}
