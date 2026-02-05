package com.base.common.entity.base;

import lombok.Data;

import java.util.Date;

/**
 * @Auther: gongmy
 * @Date: 2023/4/4
 * @Description: 基础用户类
 * @version: 1.0
 */
@Data
public class BaseUser {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 密码
     */
    private String password;

    /**
     * md5密码盐
     */
    private String salt;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 电话
     */
    private String phone;


    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 用户在线状态
     */
    private Integer onlineStatus;
    /**
     * 最后登录地址
     */
    private String lastLoginIp;
}
