package com.base.common.controller;

import com.base.common.entity.base.Result;
import com.base.common.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Auther: gongmy
 * @Date: 2022/11/2
 * @Description: com.sdsat.common.controller
 * @version: 1.0
 */
@Slf4j
@RestController
public class UtilController {
    @PostMapping("/sdsat/utils/enum")
    public Result<String> getEnumDict(@RequestParam String enumKey) {
        log.info("[接收到请求枚举字典的key:{}]",enumKey);
        return Result.ok(ResourceUtils.getEnumDictData(enumKey),"请求成功");
    }
}
