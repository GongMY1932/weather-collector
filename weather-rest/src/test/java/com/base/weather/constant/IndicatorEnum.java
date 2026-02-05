package com.base.weather.constant;

import lombok.Getter;

/**
 * 天气预报指标枚举
 */
@Getter
public enum IndicatorEnum {

    Temperature("","温度",ApiEnum.REAL_TIME_WEATHER),

    Perceived_temperature("","体感温度",ApiEnum.REAL_TIME_WEATHER),

    Wind_speed("","风力",ApiEnum.REAL_TIME_WEATHER),

    Wind_direction("","风向",ApiEnum.REAL_TIME_WEATHER),

    Relative_humidify("","相对湿度",ApiEnum.REAL_TIME_WEATHER),

    Atmospheric_pressure("","大气压强",ApiEnum.REAL_TIME_WEATHER),

    Precipitation("","降水量",ApiEnum.REAL_TIME_WEATHER),

    Visibility("","能见度",ApiEnum.REAL_TIME_WEATHER),

    Dew_point_temperature("","露点温度",ApiEnum.REAL_TIME_WEATHER),

    Cloud_cover("","云量",ApiEnum.REAL_TIME_WEATHER),

    PM25("","pm2.5",ApiEnum.REAL_TIME_AIR_QUALITY),

    PM10("","pm10",ApiEnum.REAL_TIME_AIR_QUALITY),

    ;

    private final String responseField;
    private final String description;
    private final ApiEnum apiEnum;

    IndicatorEnum(String responseField, String description, ApiEnum apiEnum) {
        this.responseField = responseField;
        this.description = description;
        this.apiEnum = apiEnum;
    }

}
