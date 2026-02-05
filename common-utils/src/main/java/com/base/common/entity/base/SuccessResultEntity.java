package com.base.common.entity.base;

/**
 * 成功
 *
 * @author GISirFive
 */
public class SuccessResultEntity<T> extends BaseResultEntity<T> {

    public SuccessResultEntity(T data) {
        super.setSuccess(true);
        super.setData(data);
    }

    public SuccessResultEntity(T data, String message) {
        super.setSuccess(true);
        super.setData(data);
        super.setMessage(message);
    }
}
