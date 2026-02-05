package com.base.weather.constant;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气预报指标枚举
 */
@Getter
public enum IndicatorEnum {

    Temperature("temp", "温度", ApiEnum.REAL_TIME_WEATHER, "℃"),

    Perceived_temperature("feelsLike", "体感温度", ApiEnum.REAL_TIME_WEATHER, "℃"),

    Wind_speed("windSpeed", "风力", ApiEnum.REAL_TIME_WEATHER, "km/h"),

    Wind_direction("windDir", "风向", ApiEnum.REAL_TIME_WEATHER, null),

    Relative_humidify("humidity", "相对湿度", ApiEnum.REAL_TIME_WEATHER, "%"),

    Atmospheric_pressure("pressure", "大气压强", ApiEnum.REAL_TIME_WEATHER, "hPa"),

    Precipitation("precip", "降水量", ApiEnum.REAL_TIME_WEATHER, "mm"),

    Visibility("vis", "能见度", ApiEnum.REAL_TIME_WEATHER, "km"),

    Dew_point_temperature("dew", "露点温度", ApiEnum.REAL_TIME_WEATHER, "℃"),

    Cloud_cover("cloud", "云量", ApiEnum.REAL_TIME_WEATHER, "%"),

    PM2p5(null, "pm2.5", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    PM10(null, "pm10", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    CO(null, "一氧化碳", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    SO2(null, "二氧化硫", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    O3(null, "臭氧", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    NO2(null, "二氧化氮", ApiEnum.REAL_TIME_AIR_QUALITY, "μg/m³"),

    ;

    private final String responseField;
    private final String description;
    private final ApiEnum apiEnum;
    private final String unit;

    IndicatorEnum(String responseField, String description, ApiEnum apiEnum, String unit) {
        this.responseField = responseField;
        this.description = description;
        this.apiEnum = apiEnum;
        this.unit = unit;
    }

    /**
     * 获取所有枚举的 name 和 description 列表，按 apiEnum 分组
     *
     * @return 按 apiEnum 分组的 List，每个 List 包含该 apiEnum 下的所有指标的 name 和 description 的 Map 列表
     */
    public static List<List<Map<String, String>>> getNameAndDescriptionList() {
        // 使用 Map 按 apiEnum 分组
        Map<ApiEnum, List<Map<String, String>>> groupedMap = new HashMap<>();

        // 遍历所有枚举值，按 apiEnum 分组
        for (IndicatorEnum indicator : IndicatorEnum.values()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", indicator.name());
            map.put("description", indicator.getDescription());

            // 获取该指标所属的 apiEnum
            ApiEnum apiEnum = indicator.getApiEnum();

            // 如果该 apiEnum 还没有对应的 list，创建一个新的
            groupedMap.computeIfAbsent(apiEnum, k -> new ArrayList<>()).add(map);
        }

        // 将所有分组后的 list 放到一个大 list 里
        List<List<Map<String, String>>> result = new ArrayList<>(groupedMap.values());

        return result;
    }

    /**
     * 根据枚举名称获取枚举实体（不区分大小写）
     *
     * @param name 枚举名称
     * @return 枚举实体，如果不存在则返回 null
     */
    public static IndicatorEnum getByName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return IndicatorEnum.valueOf(name);
        } catch (IllegalArgumentException e) {
            // 如果直接匹配失败，尝试忽略大小写匹配
            for (IndicatorEnum indicator : IndicatorEnum.values()) {
                if (indicator.name().equalsIgnoreCase(name)) {
                    return indicator;
                }
            }
            return null;
        }
    }

}
