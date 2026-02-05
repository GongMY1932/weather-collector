package com.base.common.entity.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 功能描述：允许的设备类型
 *
 * @Author: shigf
 * @Date: 2020/9/4 17:08
 */
@Data
@Component
@ConfigurationProperties(prefix = "device-discern")
public class DeviceProperties {
    // 白名单域名
    private List<String> deviceTypeDomain;
}
