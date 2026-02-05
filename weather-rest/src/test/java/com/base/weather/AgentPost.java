package com.base.weather;

import com.base.common.util.HttpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent POST 请求示例
 */
public class AgentPost {

    public static void main(String[] args) {
        try {
            // 构建请求体
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", new HashMap<>());
            request.put("query", "告诉我北京地区近7天内附近地区当前的实时温度、体感温度、风力风向、相对湿度、大气压强、降水量、能见度、露点温度、云量、pm2.5、pm10的历史值");
            request.put("response_mode", "streaming");
            request.put("conversation_id", "");
            request.put("user", "abc-123");

            // 构建 files 数组
            List<Map<String, String>> files = new ArrayList<>();
            Map<String, String> file = new HashMap<>();
            file.put("type", "image");
            file.put("transfer_method", "remote_url");
            file.put("url", "https://cloud.dify.ai/logo/logo-site.png");
            files.add(file);
            request.put("files", files);

            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(request);
            System.out.println("请求体: " + requestBody);

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer app-fY5DL0NCkRF3QggXTr9bhCx0");
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "*/*");

            // 发送 POST 请求
            String url = "http://localhost/v1/chat-messages";
            String response = HttpUtils.post(url, requestBody, headers);

            System.out.println("响应内容: " + response);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("请求失败", e);
        }
    }
}
