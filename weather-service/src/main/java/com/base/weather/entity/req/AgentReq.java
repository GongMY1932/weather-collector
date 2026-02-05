package com.base.weather.entity.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AgentReq {

    private Map<String, String> input;

    private String query;

    List<Map<String, String>> files;
}
