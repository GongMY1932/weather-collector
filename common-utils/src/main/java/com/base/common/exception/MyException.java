package com.base.common.exception;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 自定义异常，facade接口按需抛出此异常，有前端rest层统一捕获
 * </p>
 *
 * @author GISirFive
 * @date Create on 2017/11/13 0:10
 */
@Data
public class MyException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误编码
     */
    private Integer errorCode;

    /**
     * 错误编码的描述信息
     */
    private String errorMessage;

    /**
     * 错误枚举
     */
    private IErrorType errorType;

    /**
     * 抛出异常时，返回给前端的数据
     */
    private Map<String, Object> data = new HashMap<>();

    public MyException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public MyException(String errorMessage, Throwable e) {
        super(e);
        this.errorMessage = errorMessage;
    }

    public MyException(IErrorType errorType) {
        super(errorType.getErrorMessage());
        this.errorCode = errorType.getErrorCode();
        this.errorMessage = errorType.getErrorMessage();
        this.errorType = errorType;
    }

    public MyException(IErrorType errorType, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorType.getErrorCode();
        this.errorMessage = errorMessage;
        this.errorType = errorType;
    }

}