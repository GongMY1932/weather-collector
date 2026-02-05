package com.base.common.annotation;

import java.lang.annotation.*;

/**
 * @Auther: gongmy
 * @Date: 2022/11/2
 * @Description: com.sdsat.common.annotation
 * @version: 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumDict {

    String value() default "";

}
