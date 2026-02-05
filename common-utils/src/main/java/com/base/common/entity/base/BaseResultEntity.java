package com.base.common.entity.base;

import java.io.Serializable;

import lombok.Data;

/**
 * app响应对象
 *
 * @author GISirFive
 */
@Data
public class BaseResultEntity<T> implements Serializable {

    /**
     * (必填)请求成功/失败
     */
    private Boolean success;
    /**
     * (选填)提示信息
     */
    private String message;
    /**
     * (选填)错误码，当success为false时，对应{@link com.chargerlink.common.exception.IErrorType}.getErrorCode()
     */
    private Integer errorCode;
    /**
     * (选填)返回数据
     */
    private T data;

    /**
     * 请求结果
     *
     * @return 成功-失败
     */
    public boolean getSuccess() {
        return success;
    }

    /**
     * 二轮请求结果
     *
     * @return
     */
    @Deprecated
    public Integer getStatus() {
        return getSuccess() ? 1 : 0;
    }

    /**
     * 四轮请求结果
     *
     * @return
     */
    @Deprecated
    public Integer getErrcode() {
        return getSuccess() ? 0 : 1;
    }

    /**
     * 【兼容前端框架逻辑】时间戳
     *
     * @return
     */
    public Long getTimestamp() {
        return System.currentTimeMillis();
    }

}
