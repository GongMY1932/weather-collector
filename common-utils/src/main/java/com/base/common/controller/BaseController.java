package com.base.common.controller;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Author: zhang zhanghenggoog@gmail.com
 * @Date: Created on 11:27 2020/1/20.
 */
public class BaseController {
    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpServletResponse response;

    public HttpServletRequest getRequest() {
        return request;
    }
}
