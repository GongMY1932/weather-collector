package com.base.common.entity.base;

import java.io.Serializable;

import com.base.common.constant.ErrorCodeEnum;
import com.base.common.exception.IErrorType;
import com.base.common.exception.MyException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 接口返回数据格式
 *
 * @author scott
 * @email jeecgos@163.com
 * @date 2019年1月19日
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 成功标志
     */
    private boolean success = true;
    /**
     * 返回处理消息
     */
    private String message = "操作成功!";
    /**
     * 返回代码
     */
    private Integer code = 200;
    /**
     * 返回数据对象 data
     */
    private T result;
    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    public Result() {

    }

    /**
     * 通过错误码的方式生成返回值
     *
     * @param errorCodeEnum
     */
    public Result<T> errorByCode(ErrorCodeEnum errorCodeEnum) {
        this.errorByCode(errorCodeEnum, null);
        return this;
    }

    public Result<T> errorByCode(ErrorCodeEnum errorCodeEnum, T result) {
        this.message = errorCodeEnum.getCNMessage();
        this.code = errorCodeEnum.getCode();
        this.result = result;
        this.success = errorCodeEnum.getIsSuccess();
        return this;
    }

    public Result<T> error500(String message) {
        this.message = message;
        this.code = ErrorCodeEnum.ERROR.CODE;
        this.success = false;
        return this;
    }

    public Result<T> success(String message) {
        this.message = message;
        this.code = ErrorCodeEnum.SUCCESS.CODE;
        this.success = true;
        return this;
    }

    /**
     * 请求无结果-请求已执行但是没有返回预期结果
     *
     * @param result
     * @param message
     * @return
     */
    public Result<T> failure(T result, String message) {
        this.message = message;
        this.code = ErrorCodeEnum.SUCCESS.CODE;
        this.success = false;
        this.setResult(result);
        return this;
    }

    public Result<T> ok(T result) {
        this.result = result;
        this.code = ErrorCodeEnum.SUCCESS.CODE;
        this.success = true;
        return this;
    }

    public static <T> Result<T> ok() {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(ErrorCodeEnum.SUCCESS.CODE);
        r.setMessage("成功");
        return r;
    }

    public static <T> Result<T> ok(String msg) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(ErrorCodeEnum.SUCCESS.CODE);
        r.setMessage(msg);
        return r;
    }

    public static <T> Result<T> ok(T data, String msg) {
        Result<T> r = new Result<T>();
        r.setSuccess(true);
        r.setCode(ErrorCodeEnum.SUCCESS.CODE);
        r.setResult(data);
        r.setMessage(msg);
        return r;
    }

    public static <T> Result<T> error(String msg) {
        return error(ErrorCodeEnum.ERROR.CODE, msg);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<T>();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public static <T> Result<T> result(String message) {
        Result<T> r = new Result<T>();
        r.setCode(ErrorCodeEnum.FAILURE.getErrorCode());
        r.setMessage(message);
        r.setSuccess(ErrorCodeEnum.FAILURE.getIsSuccess());
        return r;
    }

    public static <T> Result<T> result(IErrorType errorCodeEnum) {
        Result<T> r = new Result<T>();
        r.setCode(errorCodeEnum.getErrorCode());
        r.setMessage(errorCodeEnum.getErrorMessage());
        r.setSuccess(errorCodeEnum.isSuccess());
        return r;
    }

    public static <T> Result<T> result(MyException e) {
        Result<T> r = new Result<T>();
        r.setCode(e.getErrorType().getErrorCode());
        if (StringUtils.isNotBlank(e.getErrorMessage())) {
            r.setMessage(e.getErrorMessage());
        } else {
            r.setMessage(e.getErrorType().getErrorMessage());
        }
        r.setSuccess(e.getErrorType().isSuccess());
        return r;
    }

    public static <T> Result<T> result(IErrorType errorCodeEnum, T result) {
        Result<T> r = new Result<T>();
        r.setCode(errorCodeEnum.getErrorCode());
        r.setMessage(errorCodeEnum.getErrorMessage());
        r.setSuccess(errorCodeEnum.isSuccess());
        r.setResult(result);
        return r;
    }

    /**
     * 无权限访问返回结果
     */
    public static <T> Result<T> noauth(String msg) {
        return error(ErrorCodeEnum.FAILURE_UNAUTHORIZED.CODE, msg);
    }
}