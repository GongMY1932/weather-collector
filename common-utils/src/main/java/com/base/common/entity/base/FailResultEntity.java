package com.base.common.entity.base;

import com.base.common.exception.IErrorType;

/**
 * 失败
 *
 * @author GISirFive
 */
public class FailResultEntity<T> extends BaseResultEntity<T> {

    public FailResultEntity() {
        super.setSuccess(false);
    }

    public FailResultEntity(String message) {
        super.setSuccess(false);
        super.setMessage(message);
    }

    public FailResultEntity(IErrorType errorType) {
        super.setSuccess(false);
        if (errorType != null) {
            super.setErrorCode(errorType.getErrorCode());
            super.setMessage(errorType.getErrorMessage());
        }
    }

}
