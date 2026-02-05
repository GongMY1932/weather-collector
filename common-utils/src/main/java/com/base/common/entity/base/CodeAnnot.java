package com.base.common.entity.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述：异常码对应信息注解
 *
 * @Author: shigf
 * @Date: 2020/8/6 18:05
 */
@Deprecated
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeAnnot {
    String value() default "";
}
