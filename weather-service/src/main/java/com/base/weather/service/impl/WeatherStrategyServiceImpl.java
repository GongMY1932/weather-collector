package com.base.weather.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.base.common.constant.DelFlagConst;
import com.base.common.entity.base.PaginationEntity;
import com.base.common.util.AssertUtils;
import com.base.weather.constant.CollectStatusEnum;
import com.base.weather.entity.WeatherStrategy;
import com.base.weather.entity.dto.BatchImportResult;
import com.base.weather.entity.req.WeatherStrategyQueryReq;
import com.base.weather.entity.req.WeatherStrategyAddReq;
import com.base.weather.entity.vo.WeatherStrategyVo;
import com.base.weather.mapper.WeatherStrategyMapper;
import com.base.weather.service.WeatherStrategyService;
import com.base.weather.service.WeatherDataService;
import com.base.weather.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 天气策略 Service 实现类
 */
@Slf4j
@Service
public class WeatherStrategyServiceImpl extends ServiceImpl<WeatherStrategyMapper, WeatherStrategy> implements WeatherStrategyService {

    @Autowired
    @Lazy
    private WeatherDataService weatherDataService;

    /**
     * 新增
     *
     * @param weatherStrategyAddReq 新增请求
     * @return 新增结果
     */
    public boolean add(WeatherStrategyAddReq weatherStrategyAddReq) {
        WeatherStrategy weatherStrategy = new WeatherStrategy();
        BeanUtils.copyProperties(weatherStrategyAddReq, weatherStrategy);
        weatherStrategy.setDelFlag(DelFlagConst.NORMAL_INT);
        weatherStrategy.setCreateTime(new Date());

        // 仅在“新增策略”时设置采集状态：
        // 当前时间与采集结束时间相差7天以上：待采集
        // 相差7天以内：采集中
        boolean within7Days = isCollectEndWithinDays(weatherStrategyAddReq.getCollectEnd(), 7);
        weatherStrategy.setCollectStatus(String.valueOf(
                within7Days ? CollectStatusEnum.COLLECTING.getCode() : CollectStatusEnum.PENDING.getCode()
        ));

        boolean result = baseMapper.insert(weatherStrategy) > 0;

        // 如果需要立即采集，触发预报数据采集
        if (result && within7Days) {
            try {
                log.info("策略 {} 新增后立即触发预报数据采集", weatherStrategy.getId());
                weatherDataService.collectForecastDataByTimeRange(weatherStrategy);
            } catch (Exception e) {
                log.error("策略 {} 新增后立即采集预报数据失败", weatherStrategy.getId(), e);
                // 本次采集异常不影响策略新增的成功，状态会在 collectForecastDataByTimeRange 中被更新为待采集/采集中
            }
        }

        return result;
    }

    /**
     * 批量新增天气策略
     *
     * @param weatherStrategys 天气策略列表
     * @return 全部成功返回 true，否则返回 false（并回滚）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAdd(List<WeatherStrategyAddReq> weatherStrategys) {
        if (weatherStrategys == null || weatherStrategys.isEmpty()) {
            log.warn("批量新增天气策略：列表为空");
            return false;
        }
        try {
            // 转换为实体对象列表
            Date now = new Date(); // 统一使用当前时间，保证同一批数据的时间一致
            List<WeatherStrategy> strategyList = new ArrayList<>();
            List<WeatherStrategy> strategiesToCollect = new ArrayList<>(); // 需要立即采集的策略

            for (WeatherStrategyAddReq req : weatherStrategys) {
                WeatherStrategy strategy = new WeatherStrategy();
                BeanUtils.copyProperties(req, strategy);
                strategy.setDelFlag(DelFlagConst.NORMAL_INT);
                strategy.setCreateTime(now);

                // 仅在“新增策略”时设置采集状态：
                // 当前时间与采集结束时间相差7天以上：待采集
                // 相差7天以内：采集中
                boolean within7Days = isCollectEndWithinDays(req.getCollectEnd(), 7);
                strategy.setCollectStatus(String.valueOf(
                        within7Days ? CollectStatusEnum.COLLECTING.getCode() : CollectStatusEnum.PENDING.getCode()
                ));
                if (within7Days) {
                    strategiesToCollect.add(strategy);
                }

                strategyList.add(strategy);
            }

            // 使用 MyBatis-Plus 批量保存（默认每批1000条）
            boolean result = saveBatch(strategyList);

            // 如果需要立即采集，触发预报数据采集
            if (result && !strategiesToCollect.isEmpty()) {
                for (WeatherStrategy strategy : strategiesToCollect) {
                    try {
                        log.info("策略 {} 批量新增后立即触发预报数据采集", strategy.getId());
                        weatherDataService.collectForecastDataByTimeRange(strategy);
                    } catch (Exception e) {
                        log.error("策略 {} 批量新增后立即采集预报数据失败", strategy.getId(), e);
                        // 本次采集异常不影响策略新增的成功，状态会在 collectForecastDataByTimeRange 中被更新为待采集/采集中
                    }
                }
            }

            log.info("批量新增天气策略完成，总数: {}, 成功: {}, 立即采集: {}",
                    weatherStrategys.size(), result ? weatherStrategys.size() : 0, strategiesToCollect.size());
            return result;

        } catch (Exception e) {
            log.error("批量新增天气策略失败，总数: {}", weatherStrategys.size(), e);
            // 事务会自动回滚
            throw e;
        }
    }

    /**
     * 更新
     * <p>
     * 更新策略时，如果修改了采集结束时间，需要判断是否需要更新策略状态：
     * - 如果新的结束时间在7天内，且当前状态是"待采集"或"已取消"，则改为"采集中"并触发采集
     * - 如果新的结束时间超过7天，且当前状态是"采集中"，则改为"待采集"
     *
     * @param weatherStrategyAddReq 更新请求
     * @return 更新结果
     */
    public boolean update(WeatherStrategyAddReq weatherStrategyAddReq) {
        WeatherStrategy weatherStrategy = getById(weatherStrategyAddReq.getId());
        AssertUtils.throwNull(weatherStrategy, "没有查询到对应的实体信息");
        
        // 保存原始状态和时间，用于判断是否需要更新状态
        String originalCollectEnd = weatherStrategy.getCollectEnd();
        String originalCollectStatus = weatherStrategy.getCollectStatus();
        String newCollectEnd = weatherStrategyAddReq.getCollectEnd();
        
        // 复制属性
        BeanUtils.copyProperties(weatherStrategyAddReq, weatherStrategy);
        weatherStrategy.setUpdateTime(new Date());
        
        // 判断是否需要更新状态（仅当采集结束时间发生变化时）
        boolean collectEndChanged = !StringUtils.hasText(originalCollectEnd) 
                || !StringUtils.hasText(newCollectEnd) 
                || !originalCollectEnd.equals(newCollectEnd);
        
        // 标记是否需要触发采集
        boolean shouldTriggerCollection = false;
        
        if (collectEndChanged) {
            // 判断新的结束时间是否在7天内
            boolean within7Days = isCollectEndWithinDays(newCollectEnd, 7);
            // 使用原始状态进行判断（因为状态更新应该基于原始状态）
            String originalStatus = originalCollectStatus;
            
            // 如果新时间在7天内
            if (within7Days) {
                // 原始状态是"待采集"或"已取消"，改为"采集中"
                if (String.valueOf(CollectStatusEnum.PENDING.getCode()).equals(originalStatus)
                        || String.valueOf(CollectStatusEnum.CANCELLED.getCode()).equals(originalStatus)) {
                    weatherStrategy.setCollectStatus(String.valueOf(CollectStatusEnum.COLLECTING.getCode()));
                    shouldTriggerCollection = true;
                    log.info("策略 {} 采集结束时间更新后，状态从 {} 改为采集中", weatherStrategy.getId(), originalStatus);
                }
            } else {
                // 如果新时间超过7天，且原始状态是"采集中"，改为"待采集"
                if (String.valueOf(CollectStatusEnum.COLLECTING.getCode()).equals(originalStatus)) {
                    weatherStrategy.setCollectStatus(String.valueOf(CollectStatusEnum.PENDING.getCode()));
                    log.info("策略 {} 采集结束时间更新后，状态从采集中改为待采集", weatherStrategy.getId());
                }
            }
            // 注意：如果状态是"采集完成"（SUCCESS），保持原状态不变
        }
        
        boolean result = updateById(weatherStrategy);
        
        // 如果更新成功，且需要触发采集，则触发预报数据采集
        if (result && shouldTriggerCollection) {
            try {
                log.info("策略 {} 更新后立即触发预报数据采集", weatherStrategy.getId());
                weatherDataService.collectForecastDataByTimeRange(weatherStrategy);
            } catch (Exception e) {
                log.error("策略 {} 更新后立即采集预报数据失败", weatherStrategy.getId(), e);
                // 本次采集异常不影响策略更新的成功
            }
        }
        
        return result;
    }


    /**
     * 更新
     *
     * @param id id
     * @return 更新结果
     */
    public boolean delete(String id) {
        WeatherStrategy weatherStrategy = getById(id);
        AssertUtils.throwNull(weatherStrategy, "没有查询到对应的实体信息");
        weatherStrategy.setDelFlag(DelFlagConst.DELETED_INT);
        weatherStrategy.setCollectStatus(String.valueOf(CollectStatusEnum.CANCELLED.getCode()));
        return updateById(weatherStrategy);
    }

    /**
     * 取消策略（不删除记录）
     *
     * @param id 策略ID
     * @return 取消结果
     */
    @Override
    public boolean cancel(String id) {
        WeatherStrategy weatherStrategy = getById(id);
        AssertUtils.throwNull(weatherStrategy, "没有查询到对应的实体信息");
        weatherStrategy.setCollectStatus(String.valueOf(CollectStatusEnum.CANCELLED.getCode()));
        weatherStrategy.setUpdateTime(new Date());
        return updateById(weatherStrategy);
    }

    /**
     * 天气策略查询
     *
     * @param weatherStrategyQueryReq 天气策略查询请求
     * @return 天气策略列表
     */
    public List<WeatherStrategyVo> getList(WeatherStrategyQueryReq weatherStrategyQueryReq) {
        // 使用 SQL 查询（不分页）
        Page<WeatherStrategyVo> page = new Page<>(1, Integer.MAX_VALUE);
        Page<WeatherStrategyVo> result = baseMapper.getList(page, weatherStrategyQueryReq);
        List<WeatherStrategyVo> records = result.getRecords();
        fillCollectStatusDesc(records);
        return records;
    }


    /**
     * 分页查询天气策略
     *
     * @param request 查询请求参数
     * @return 分页结果
     */
    @Override
    public PaginationEntity<WeatherStrategyVo> pageQuery(WeatherStrategyQueryReq request) {
        // 确保分页参数有默认值
        Integer pageNum = request.getPage();
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        Integer pageSize = request.getPageSize();
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 构建 MyBatis-Plus 分页对象
        Page<WeatherStrategyVo> page = new Page<>(pageNum, pageSize);
        // 执行分页查询，MyBatis-Plus 会自动处理分页和总数
        Page<WeatherStrategyVo> result = baseMapper.getList(page, request);
        fillCollectStatusDesc(result.getRecords());
        // 转换为 PaginationEntity
        return new PaginationEntity<>(
                result.getRecords(),
                (int) result.getTotal(),
                pageNum,
                pageSize
        );
    }

    private void fillCollectStatusDesc(List<WeatherStrategyVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (WeatherStrategyVo vo : list) {
            String statusStr = vo.getCollectStatus();
            if (statusStr == null || statusStr.trim().isEmpty()) {
                vo.setCollectStatusDesc(CollectStatusEnum.PENDING.getDesc());
                continue;
            }
            try {
                int status = Integer.parseInt(statusStr.trim());
                if (status == CollectStatusEnum.PENDING.getCode()) {
                    vo.setCollectStatusDesc(CollectStatusEnum.PENDING.getDesc());
                } else if (status == CollectStatusEnum.COLLECTING.getCode()) {
                    vo.setCollectStatusDesc(CollectStatusEnum.COLLECTING.getDesc());
                } else if (status == CollectStatusEnum.SUCCESS.getCode()) {
                    vo.setCollectStatusDesc(CollectStatusEnum.SUCCESS.getDesc());
                } else if (status == CollectStatusEnum.CANCELLED.getCode()) {
                    vo.setCollectStatusDesc(CollectStatusEnum.CANCELLED.getDesc());
                } else {
                    vo.setCollectStatusDesc("未知");
                }
            } catch (NumberFormatException e) {
                log.warn("无法解析采集状态: {}", statusStr);
                vo.setCollectStatusDesc("未知");
            }
        }
    }


    /**
     * 批量导入天气策略（带详细信息）
     * <p>
     * <p>
     * 检查Excel文件内需求名称是否重复
     * 检查数据库中是否已存在相同的需求名称
     * 跳过重复的记录，继续处理其他数据
     * 返回详细的导入结果，包括重复信息
     *
     * @param strategyList 策略列表
     * @return 导入结果，包含成功数量、重复信息等
     */
    @Override
    public BatchImportResult batchImportWithDetails(List<WeatherStrategyAddReq> strategyList) {
        if (strategyList == null || strategyList.isEmpty()) {
            return new BatchImportResult(0, new ArrayList<>(), new ArrayList<>());
        }

        // 检查Excel文件内的重复（同一批导入数据中的重复）
        Set<String> excelDemandNames = new HashSet<>();
        List<String> excelDuplicates = new ArrayList<>();
        for (WeatherStrategyAddReq req : strategyList) {
            String demandName = req.getDemandName();
            if (demandName != null && !demandName.trim().isEmpty()) {
                if (excelDemandNames.contains(demandName.trim())) {
                    if (!excelDuplicates.contains(demandName.trim())) {
                        excelDuplicates.add(demandName.trim());
                    }
                } else {
                    excelDemandNames.add(demandName.trim());
                }
            }
        }

        if (!excelDuplicates.isEmpty()) {
            log.warn("Excel文件中存在重复的需求名称: {}", excelDuplicates);
        }

        // 批量查询数据库中已存在的需求名称（优化：减少数据库查询次数）
        Set<String> existingDemandNames = new HashSet<>();
        for (WeatherStrategyAddReq req : strategyList) {
            String demandName = req.getDemandName();
            if (demandName != null && !demandName.trim().isEmpty()) {
                String trimmedName = demandName.trim();
                // 跳过Excel内重复的，不需要再查数据库
                if (!excelDuplicates.contains(trimmedName)) {
                    // 检查数据库中是否已存在
                    int count = baseMapper.countByDemandName(trimmedName);
                    if (count > 0) {
                        existingDemandNames.add(trimmedName);
                    }
                }
            }
        }
        if (!existingDemandNames.isEmpty()) {
            log.warn("数据库中已存在以下需求名称，将跳过: {}", existingDemandNames);
        }

        // 过滤掉重复的记录，只导入有效的记录
        int successCount = 0;
        for (WeatherStrategyAddReq req : strategyList) {
            String demandName = req.getDemandName();
            if (demandName == null || demandName.trim().isEmpty()) {
                log.warn("跳过需求名称为空的记录");
                continue;
            }

            String trimmedName = demandName.trim();

            // 跳过Excel文件内的重复
            if (excelDuplicates.contains(trimmedName)) {
                log.warn("跳过Excel文件内重复的需求名称: {}", trimmedName);
                continue;
            }

            // 跳过数据库中已存在的
            if (existingDemandNames.contains(trimmedName)) {
                log.warn("跳过数据库中已存在的需求名称: {}", trimmedName);
                continue;
            }

            // 尝试添加
            try {
                if (add(req)) {
                    successCount++;
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他数据
                log.error("导入策略失败: {}", trimmedName, e);
            }
        }

        List<String> excelDuplicatesList = new ArrayList<>(excelDuplicates);
        List<String> existingDemandNamesList = new ArrayList<>(existingDemandNames);

        log.info("批量导入完成，总数: {}, 成功: {}, Excel内重复: {}, 数据库已存在: {}",
                strategyList.size(), successCount, excelDuplicatesList.size(), existingDemandNamesList.size());

        return new BatchImportResult(successCount, excelDuplicatesList, existingDemandNamesList);
    }

    /**
     * 判断是否需要立即采集预报数据
     * <p>
     * 判断逻辑：当前时间与采集结束时间相差小于168小时，则需要立即采集
     *
     * @param collectEnd 采集结束时间字符串
     * @return true 表示需要立即采集，false 表示待采集
     */
    private boolean isCollectEndWithinDays(String collectEnd, int days) {
        if (!StringUtils.hasText(collectEnd)) {
            // 没有结束时间：按“7天以上”处理（待采集）
            return false;
        }
        try {
            LocalDateTime endTime = DateTimeUtils.parseDateTime(collectEnd);
            if (endTime == null) {
                // 解析失败：按“7天以上”处理（待采集）
                return false;
            }
            LocalDateTime now = LocalDateTime.now();
            long hoursBetween = ChronoUnit.HOURS.between(now, endTime);
            // “相差7天以内”：<= 7天（168小时）。endTime<=now 时 hoursBetween 为负数，也视为“7天以内”
            return hoursBetween <= (long) days * 24;
        } catch (Exception e) {
            log.error("判断collectEnd是否在{}天内发生异常，collectEnd: {}", days, collectEnd, e);
            return false;
        }
    }
}
