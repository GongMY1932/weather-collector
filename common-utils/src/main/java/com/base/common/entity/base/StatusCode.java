package com.base.common.entity.base;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：异常代码
 *
 * @Author: shigf
 * @Date: 2020/8/6 18:03
 */
@Deprecated
public enum StatusCode {
    @CodeAnnot("成功!") OK(0,true),
    /**系统错误代码:**/

    /**门户错误代码:**/
    @CodeAnnot("门户") PORTAL_(1001,false),

    @CodeAnnot("冗余") TEMP(10000,false);


    /**｛服务名｝服务错误代码:**/


    private static final Map<String, String> hMap = new HashMap<>();
    static {
        Field[] fields = StatusCode.class.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(CodeAnnot.class)) {
                hMap.put(field.getName(), field.getAnnotation(CodeAnnot.class).value());
            }
        }
    }

    private final int value;

    private final boolean IS_SUCCESS;

    // 构造器默认也只能是private, 从而保证构造函数只能在内部使用
    StatusCode(int value,boolean IS_SUCCESS) {
        this.value = value;
        this.IS_SUCCESS = false;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return hMap.get(this.toString());
    }

    public String GetDescription() {
        return hMap.get(this.toString());
    }
}
