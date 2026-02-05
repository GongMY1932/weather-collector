package com.base.common.util;


import com.base.common.constant.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.base.common.exception.MyException;

import java.util.List;

/**
 * 业务异常工具类,语法类似junit断言语法
 *
 * @author GISirFive
 * @date 2017-12-13 18:01:17
 */
@Slf4j
public class AssertUtils {
    /**
     * 断定目标值如果为false.则抛出业务异常
     *
     * @param expression
     * @param errorMessage
     * @throws MyException
     */
    public static void throwFalse(boolean expression, String errorMessage) {
        if (!expression) {
            MyException myException = new MyException(ErrorCodeEnum.FAILURE);
            if (StringUtils.isNotBlank(errorMessage)) {
                myException.setErrorMessage(errorMessage);
            }
            throw myException;
        }
    }


    /**
     * 断定目标值如果为true.则抛出业务异常
     *
     * @param expression
     * @param errorMessage
     * @throws MyException
     */
    public static void throwTrue(boolean expression, String errorMessage) {
        if (expression) {
            MyException myException = new MyException(ErrorCodeEnum.FAILURE);
            if (StringUtils.isNotBlank(errorMessage)) {
                myException.setErrorMessage(errorMessage);
            }
            throw myException;
        }
    }

    /**
     * 断定目标值如果为null，则抛出业务异常
     *
     * @param obj
     * @param errorMessage
     * @throws MyException
     */
    public static void throwNull(Object obj, String errorMessage) {
        if (obj == null) {
            MyException myException = new MyException(ErrorCodeEnum.FAILURE);
            if (StringUtils.isNotBlank(errorMessage)) {
                myException.setErrorMessage(errorMessage);
            }
            throw myException;
        }
    }

    /**
     * 断定目标集合如果为空，则抛出业务异常
     *
     * @param list
     * @param errorMessage
     * @throws MyException
     */
    public static void throwEmpty(List list, String errorMessage) {
        if (list.isEmpty()) {
            MyException myException = new MyException(ErrorCodeEnum.FAILURE);
            if (StringUtils.isNotBlank(errorMessage)) {
                myException.setErrorMessage(errorMessage);
            }
            throw myException;
        }
    }

    /**
     * 断定目标字符串如果为空，则抛出业务异常
     *
     * @param string
     * @param errorMessage
     */
    public static void throwBlank(String string, String errorMessage) {
        if (StringUtils.isBlank(string)) {
            MyException myException = new MyException(ErrorCodeEnum.FAILURE);
            if (StringUtils.isNotBlank(errorMessage)) {
                myException.setErrorMessage(errorMessage);
            }
            throw myException;
        }
    }

    /**
     * 断定目标字符串如果为空，则抛出业务异常
     *
     * @param errorMessage
     */
    public static void throwException(String errorMessage) {
        MyException myException = new MyException(ErrorCodeEnum.FAILURE);
        if (StringUtils.isNotBlank(errorMessage)) {
            myException.setErrorMessage(errorMessage);
        }
        throw myException;
    }

}
