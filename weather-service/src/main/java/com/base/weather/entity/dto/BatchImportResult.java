package com.base.weather.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量导入结果
 */
@Data
public class BatchImportResult {
    /**
     * 成功导入的数量
     */
    private int successCount;

    /**
     * Excel文件内重复的需求名称列表
     */
    private List<String> excelDuplicates;

    /**
     * 数据库中已存在的需求名称列表
     */
    private List<String> existingDemandNames;

    /**
     * 跳过的数量（重复+已存在）
     */
    private int skipCount;

    public BatchImportResult() {
    }

    public BatchImportResult(int successCount, List<String> excelDuplicates, List<String> existingDemandNames) {
        this.successCount = successCount;
        this.excelDuplicates = excelDuplicates;
        this.existingDemandNames = existingDemandNames;
        this.skipCount = (excelDuplicates != null ? excelDuplicates.size() : 0) +
                (existingDemandNames != null ? existingDemandNames.size() : 0);
    }
}
