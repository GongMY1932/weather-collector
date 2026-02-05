package com.base.common.entity.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 功能描述：
 *
 * @Author: shigf
 * @Date: 2020/9/1 15:20
 */
@Data
@Component
@ConfigurationProperties(prefix = "referer")
public class RefererProperties {
    // 白名单域名
    private List<String> refererDomain;
}
