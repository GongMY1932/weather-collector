package com.base.weather.controller;

import com.base.common.entity.base.PaginationEntity;
import com.base.weather.constant.IndicatorEnum;
import com.base.weather.entity.dto.BatchImportResult;
import com.base.weather.entity.req.WeatherStrategyAddReq;
import com.base.weather.entity.req.WeatherStrategyQueryReq;
import com.base.weather.entity.vo.WeatherStrategyVo;
import com.base.weather.service.WeatherStrategyService;
import com.base.weather.util.ExcelImportUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 天气策略 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/weather-strategy")
public class WeatherStrategyController {

    @Resource
    private WeatherStrategyService weatherStrategyService;

    @Resource
    private ExcelImportUtil excelImportUtil;

    /**
     * 新增天气策略
     */
    @PostMapping("/add")
    public ResponseEntity<Boolean> create(@RequestBody WeatherStrategyAddReq weatherStrategy) {
        log.info("接收到新增天气策略的请求[{}]", weatherStrategy);
        boolean result = weatherStrategyService.add(weatherStrategy);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量新增天气策略
     */
    @PostMapping("/batchAdd")
    public ResponseEntity<Boolean> batchAdd(@RequestBody List<WeatherStrategyAddReq> weatherStrategys) {
        log.info("接收到批量新增天气策略的请求,共准备新增[{}]个", weatherStrategys.size());
        boolean result = weatherStrategyService.batchAdd(weatherStrategys);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID删除天气策略
     */
    @GetMapping("/{id}")
    public ResponseEntity<Boolean> delete(@RequestParam String id) {
        log.info("接收到删除天气策略的请求,id=[{}]", id);
        boolean result = weatherStrategyService.delete(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 取消策略（不删除，仅置为已取消）
     */
    @PostMapping("/cancel")
    public ResponseEntity<Boolean> cancel(@RequestParam String id) {
        log.info("接收到取消天气策略的请求,id=[{}]", id);
        boolean result = weatherStrategyService.cancel(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新天气策略
     */
    @PostMapping("/update")
    public ResponseEntity<Boolean> update(@RequestBody WeatherStrategyAddReq weatherStrategy) {
        log.info("接收到更新天气策略的请求[{}]", weatherStrategy);
        boolean result = weatherStrategyService.update(weatherStrategy);
        return ResponseEntity.ok(result);
    }


    /**
     * 分页查询天气策略
     */
    @PostMapping("/page")
    public ResponseEntity<PaginationEntity<WeatherStrategyVo>> page(@RequestBody WeatherStrategyQueryReq queryReq) {
        log.info("接收到分页查询天气策略的请求[{}]", queryReq);
        PaginationEntity<WeatherStrategyVo> result = weatherStrategyService.pageQuery(queryReq);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有天气策略
     */
    @GetMapping("/list")
    public ResponseEntity<java.util.List<WeatherStrategyVo>> list(@RequestBody WeatherStrategyQueryReq queryReq) {
        java.util.List<WeatherStrategyVo> list = weatherStrategyService.getList(queryReq);
        return ResponseEntity.ok(list);
    }

    /**
     * 查询指标枚举（按 apiEnum 分组）
     *
     * @return 按 apiEnum 分组的指标枚举列表
     */
    @PostMapping("/getIndicatorList")
    public ResponseEntity<List<List<Map<String, String>>>> getNameAndDescriptionList() {
        return ResponseEntity.ok(IndicatorEnum.getNameAndDescriptionList());
    }

    /**
     * 从Excel文件导入天气策略
     * <p>
     * Excel格式要求：
     * 第一行为表头（会被跳过）：
     * 需求名称 | 目标地址 | 采集内容 | 采集开始时间 | 采集结束时间
     * <p>
     * 数据行示例：
     * 测试1 | (12,21) | 温度;PM2.5;云量;降水量 | 2026/1/1 12:00 | 2026/1/5 12:00
     * <p>
     * 说明：
     * - 目标地址格式：(经度,纬度)，例如：(116.41,39.92)
     * - 采集内容：多个指标用分号分隔，支持中文名称（如：温度、PM2.5、云量、降水量等）
     * - 时间格式：yyyy/MM/dd HH:mm 或 yyyy/M/d HH:mm
     *
     * @param file Excel文件（.xlsx格式）
     * @return 导入结果，包含成功导入的数量
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFromExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查文件
            if (file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(result);
            }

            // 检查文件格式
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                result.put("success", false);
                result.put("message", "文件格式不正确，请上传Excel文件（.xlsx或.xls）");
                return ResponseEntity.badRequest().body(result);
            }

            // 解析Excel文件
            List<WeatherStrategyAddReq> strategyAddReqList = excelImportUtil.importFromExcel(file);

            if (strategyAddReqList.isEmpty()) {
                result.put("success", false);
                result.put("message", "Excel文件中没有有效数据");
                return ResponseEntity.badRequest().body(result);
            }
            // 批量导入（带详细信息）
            BatchImportResult importResult = weatherStrategyService.batchImportWithDetails(strategyAddReqList);

            result.put("success", true);
            result.put("message", "导入完成");
            result.put("total", strategyAddReqList.size());
            result.put("successCount", importResult.getSuccessCount());
            result.put("skipCount", importResult.getSkipCount());
            result.put("failCount", strategyAddReqList.size() - importResult.getSuccessCount() - importResult.getSkipCount());
            result.put("excelDuplicates", importResult.getExcelDuplicates());  // Excel文件内重复的需求名称
            result.put("existingDemandNames", importResult.getExistingDemandNames());  // 数据库中已存在的需求名称
            result.put("data", strategyAddReqList);  // 返回解析出的策略列表

            log.info("Excel导入完成，总数: {}, 成功: {}, 跳过: {} (Excel内重复: {}, 数据库已存在: {}), 失败: {}",
                    strategyAddReqList.size(),
                    importResult.getSuccessCount(),
                    importResult.getSkipCount(),
                    importResult.getExcelDuplicates().size(),
                    importResult.getExistingDemandNames().size(),
                    strategyAddReqList.size() - importResult.getSuccessCount() - importResult.getSkipCount());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Excel导入失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
