package com.base.weather.controller;

import com.base.common.entity.base.Result;
import com.base.common.util.AssertUtils;
import com.base.common.util.HttpUtils;
import com.base.weather.entity.req.AgentReq;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent")
public class AgentController {

    @Value("api.agent.base")
    private String agentUrl;

    @PostMapping("/ask")
    public Result<String> getAgent(@RequestBody AgentReq agentReq) {
        try {
            // 构建请求体
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", new HashMap<>());
            request.put("query", agentReq.getQuery());
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
            String url = agentUrl;

            String response = HttpUtils.post(url, requestBody, headers);
            System.out.println("响应内容: " + response);
            return Result.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            AssertUtils.throwTrue(true, "请求失败");
        }
        return null;
    }


}
