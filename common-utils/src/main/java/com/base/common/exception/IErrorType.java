package com.base.common.exception;

/**
 * 异常类型定义，所有需要自定义异常的类都需要实现此接口
 *
 * @author GISirFive
 * @date Create on 2018/9/28 16:06
 */
public interface IErrorType {

    /**
     * 获取错误编码
     *
     * @return
     */
    Integer getErrorCode();

    /**
     * 获取错误编码的描述信息
     *
     * @return
     */
    String getErrorMessage();

    /**
     * 是否成功
     * @return
     */
    Boolean isSuccess();

}
