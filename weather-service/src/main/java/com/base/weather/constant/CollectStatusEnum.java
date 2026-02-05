package com.base.weather.constant;

import lombok.Getter;

/**
 * 采集状态枚举
 */
@Getter
public enum CollectStatusEnum {
    PENDING(0, "待采集"),
    COLLECTING(1, "采集中"),
    SUCCESS(2, "采集完成"),
    CANCELLED(4, "已取消");

    private final int code;
    private final String desc;

    CollectStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
