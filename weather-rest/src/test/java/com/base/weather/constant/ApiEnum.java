package com.base.weather.constant;

import lombok.Getter;

/**
 * 天气预报api枚举类
 */
@Getter
public enum ApiEnum {

    REAL_TIME_WEATHER("实时天气", "get", "/v7/weather/now"),

    REAL_TIME_AIR_QUALITY("实时空气质量", "get", "/airquality/v1/current/{latitude}/{longitude}"),

    SOLAR_RADIATION_FORECAST("太阳辐射预报", "get", "/solarradiation/v1/forecast/{latitude}/{longitude}");

    private final String description;
    private final String request;
    private final String url;


    ApiEnum(String description, String request, String url) {
        this.description = description;
        this.request = request;
        this.url = url;
    }

}
