package com.base.weather.service;


import com.alibaba.fastjson.JSON;
import com.base.weather.entity.dto.RealTimeWeatherResponse;
import com.base.weather.service.impl.APIService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ApiTest {

    @Autowired
    private APIService apiService;

    @Test
    void testGetWeather() {
        RealTimeWeatherResponse response = apiService.getRealTimeWeather("116.41,39.92");
        // 使用 JSON 格式化输出，避免控制台编码问题
        String json = JSON.toJSONString(response, true);
        log.info("实时天气响应: \n{}", json);
        System.out.println("实时天气响应: \n" + json);
    }

}
