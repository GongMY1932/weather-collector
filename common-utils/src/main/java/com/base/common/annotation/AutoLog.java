package com.base.common.annotation;


import com.base.common.constant.CommonConstant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 系统日志注解
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019年1月14日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {

	/**
	 * 日志内容描述
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * 日志类型
	 * 
	 * @return 0:操作日志;1:登录日志;2:定时任务;
	 */
	int logType() default CommonConstant.LOG_TYPE_2;

	/**
	 * 事件类型
	 *
	 * @return 0:系统;1:安全;2:应用;
	 */
	int eventType() default CommonConstant.LOG_EVENT_TYPE.SYSTEM;

	/**
	 * 模块
	 * @return
	 */
	String module() default "";

	/**
	 * 回调类路径(必须是spring的bean)
	 * @return
	 */
	String callbackClassPath() default "";

	/**
	 * 回调方法名
	 * @return
	 */
	String callbackFunc() default "";

	/**
	 * 参数名称
	 * @return
	 */
	String paramName() default "";

}
