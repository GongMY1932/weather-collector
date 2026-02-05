//package com.base.common.config;
//
//import com.base.common.interceptor.DeviceInterceptor;
//import com.base.common.interceptor.RefererInterceptor;
//import com.base.common.prometheus.MicrometerInterceptor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 功能描述：安全基线配置类
// *
// * @Author: shigf
// * @Date: 2020/9/1 15:07
// */
//@Configuration
//public class SafetyConfig implements WebMvcConfigurer {
//    @Autowired(required = false)
//    private RefererInterceptor refererInterceptor;
//
//    @Autowired(required = false)
//    private DeviceInterceptor deviceInterceptor;
//
//    @Autowired
//    private MicrometerInterceptor micrometerInterceptor;
//
//    /**
//     * 添加过滤器
//     *
//     * @param registry
//     */
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        if(refererInterceptor != null)
//            registry.addInterceptor(refererInterceptor);
//        if(deviceInterceptor != null)
//            registry.addInterceptor(deviceInterceptor);
//        registry.addInterceptor(micrometerInterceptor);
//    }
//
//}
