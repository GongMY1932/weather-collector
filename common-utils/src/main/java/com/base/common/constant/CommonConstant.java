package com.base.common.constant;

public interface CommonConstant {

	/**
	 * 系统日志类型： 登录
	 */
	public static final int LOG_TYPE_1 = 1;

	/**
	 * 系统日志类型： 操作
	 */
	public static final int LOG_TYPE_2 = 2;

	/**
	 * 日志事件类型
	 */
	interface LOG_EVENT_TYPE{
		int SYSTEM = 1;  //系统
		int SAFE = 2;  //安全
		int APPLICATION = 3;  //应用
	}
    
}
