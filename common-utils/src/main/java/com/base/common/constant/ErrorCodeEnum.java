package com.base.common.constant;

import com.base.common.exception.IErrorType;
import com.base.common.util.MessageUtils;

public enum ErrorCodeEnum implements IErrorType {

    /**
     * 操作成功 [GET]
     */
    SUCCESS(200, true, "操作成功"),
    /**
     * 创建成功 [POST/PUT/PATCH]
     */
    SUCCESS_CREATED(201, true, "创建成功"),
    /**
     * 请求已进入队列（异步任务） [*]
     */
    SUCCESS_ACCEPTED(202, true, "请求已进入队列"),
    /**
     * 删除成功 [DELETE]
     */
    SUCCESS_NO_CONTENT(204, true, "删除成功"),
    /**
     * 操作失败 [POST/PUT/PATCH]
     */
    FAILURE(400, false, "请求失败，请检查参数"),
    /**
     * 当前请求需要用户验证 [*]
     */
    FAILURE_UNAUTHORIZED(401, false, "当前请求需要用户验证"),
    /**
     * 当前用户无权限 [*]
     */
    FAILURE_FORBIDDEN(403, false, "当前请求需要用户权限"),
    /**
     * 当前请求未匹配到数据 [*]
     */
    FAILURE_NOT_FOUND(404, false, "当前请求未找到路径"),
    /**
     * 当前请求令牌已过期
     */
    FAILURE_TOKEN_EXPIRED(405, false, "当前请求TOKEN已过期"),
    /**
     * 当前请求格式不匹配 [GET]
     */
    FAILURE_NOT_ACCEPTABLE(406, false, "当前请求格式不匹配"),
    /**
     * 当前请求资源不存在 [GET]
     */
    FAILURE_GONE(410, false, "当前请求资源不存在"),
    /**
     * 创建对象时出现验证错误 [POST/PUT/PATCH]
     */
    FAILURE_UNPROCESABLE_ENTITY(422, false, "创建对象时出现验证错误"),
    /**
     * 内部错误 [*]
     */
    ERROR(500, false, "服务器繁忙，请联系管理员"),

    /****** portal服务错误 *****/
    PORTAL_PRDT_PUBLIC_FAIL_ERROR(2000, false, "产品发布失败"),
    PORTAL_TYPE_NOT_EXIST_ERROR(2001, false, "类型不存在"),

    ;;
    /**
     * 返回编码
     */
    public Integer CODE;
    /**
     * 是否成功
     */
    public boolean IS_SUCCESS;
    /**
     * 中文提示语
     */
    public String MESSAGE;

    ErrorCodeEnum(Integer CODE, boolean isSuccess, String MESSAGE) {
        this.CODE = CODE;
        this.IS_SUCCESS = isSuccess;
        this.MESSAGE = MESSAGE;
    }

    public int getCode() {
        return this.CODE;
    }

    public boolean getIsSuccess() {
        return this.IS_SUCCESS;
    }

    public String getCNMessage() {
        return this.MESSAGE;
    }

    /**
     * 获取错误编码
     *
     * @return
     */
    @Override
    public Integer getErrorCode() {
        return this.CODE;
    }

    /**
     * 获取错误编码的描述信息
     *
     * @return
     */
    @Override
    public String getErrorMessage() {
        return this.MESSAGE;
    }

    /**
     * 是否成功
     *
     * @return
     */
    @Override
    public Boolean isSuccess() {
        return this.IS_SUCCESS;
    }
}
