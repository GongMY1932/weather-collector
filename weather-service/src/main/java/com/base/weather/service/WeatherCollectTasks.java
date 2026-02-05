package com.base.weather.service;

import com.base.weather.entity.WeatherStrategy;
import com.base.weather.constant.CollectStatusEnum;
import com.base.weather.service.impl.WeatherStrategyServiceImpl;
import com.base.weather.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 天气采集定时任务（合并：实时采集 + 预报采集）
 */
@Slf4j
@Component
public class WeatherCollectTasks {

    @Autowired
    private WeatherStrategyServiceImpl weatherStrategyService;

    @Autowired
    private WeatherDataService weatherDataService;

    /**
     * 定时采集实时天气数据
     * 默认每天执行一次（凌晨1点执行）
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void collectWeatherData() {
        log.info("开始执行天气数据采集任务");

        try {
            // 查询所有有效的天气策略
            List<WeatherStrategy> strategies = weatherStrategyService.list();

            if (strategies == null || strategies.isEmpty()) {
                log.info("没有需要采集的天气策略");
                return;
            }

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayStr = today.format(formatter);

            int successCount = 0;
            int failCount = 0;

            for (WeatherStrategy strategy : strategies) {
                // 到期检查：只有定时任务判断结束时间是否小于当前时间，如果是则状态更改为采集完成
                if (markSuccessIfExpired(strategy)) {
                    continue;
                }
                // 检查策略是否在有效期内
                if (!isStrategyValid(strategy, todayStr)) {
                    log.debug("策略 {} 不在有效期内，跳过", strategy.getId());
                    continue;
                }

                try {
                    // 采集天气数据
                    weatherDataService.collectWeatherData(strategy);
                    successCount++;
                    log.info("策略 {} 天气数据采集成功", strategy.getId());
                } catch (Exception e) {
                    failCount++;
                    log.error("策略 {} 天气数据采集异常", strategy.getId(), e);
                }
            }

            log.info("天气数据采集任务完成，成功: {}, 失败: {}", successCount, failCount);
        } catch (Exception e) {
            log.error("天气数据采集任务执行失败", e);
        }
    }

    /**
     * 预报数据采集定时任务（紧急优先级）
     * <p>
     * 每6小时执行一次，扫描优先级为0（紧急）的策略并触发按时间范围采集预报数据并入库
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void collectForecastByStrategyTimeRangeUrgent() {
        LocalDateTime now = LocalDateTime.now();
        log.info("开始执行紧急优先级预报数据采集任务，当前时间: {}", now);
        collectForecastByStrategyTimeRange(0, "紧急");
    }

    /**
     * 预报数据采集定时任务（普通优先级）
     * <p>
     * 每12小时执行一次，扫描优先级为1（普通）的策略并触发按时间范围采集预报数据并入库
     */
    @Scheduled(cron = "0 0 */12 * * ?")
    public void collectForecastByStrategyTimeRangeNormal() {
        LocalDateTime now = LocalDateTime.now();
        log.info("开始执行普通优先级预报数据采集任务，当前时间: {}", now);
        collectForecastByStrategyTimeRange(1, "普通");
    }

    /**
     * 预报数据采集定时任务（通用方法）
     * <p>
     * 根据优先级扫描策略表并触发按时间范围采集预报数据并入库
     *
     * @param priority 优先级（0-紧急，1-普通，null-默认普通）
     * @param priorityDesc 优先级描述（用于日志）
     */
    private void collectForecastByStrategyTimeRange(Integer priority, String priorityDesc) {
        try {
            List<WeatherStrategy> strategies = weatherStrategyService.list();
            if (strategies == null || strategies.isEmpty()) {
                log.info("没有可用的天气策略，结束{}优先级预报采集任务", priorityDesc);
                return;
            }

            int triggered = 0;
            int skipped = 0;
            int failed = 0;

            for (WeatherStrategy strategy : strategies) {
                // 跳过删除的策略
                if (strategy.getDelFlag() != 0) {
                    skipped++;
                    continue;
                }
                // 根据优先级过滤：如果策略的优先级为null，默认为普通（1）
                Integer strategyPriority = strategy.getTargetPriority();
                if (strategyPriority == null) {
                    strategyPriority = 1; // 默认为普通
                }
                if (!strategyPriority.equals(priority)) {
                    skipped++;
                    continue;
                }
                // 到期检查：只有定时任务判断结束时间是否小于当前时间，如果是则状态更改为采集完成
                if (markSuccessIfExpired(strategy)) {
                    skipped++;
                    continue;
                }
                // 已取消 / 已完成 的策略不再采集
                String status = strategy.getCollectStatus();
                if (String.valueOf(CollectStatusEnum.CANCELLED.getCode()).equals(status)
                        || String.valueOf(CollectStatusEnum.SUCCESS.getCode()).equals(status)) {
                    skipped++;
                    continue;
                }

                if (!StringUtils.hasText(strategy.getCollectStart()) || !StringUtils.hasText(strategy.getCollectEnd())) {
                    log.debug("策略 {} 未配置采集开始/结束时间，跳过", strategy.getId());
                    skipped++;
                    continue;
                }

                try {
                    weatherDataService.collectForecastDataByTimeRange(strategy);
                    triggered++;
                    log.info("策略 {} ({})预报数据采集触发完成", strategy.getId(), priorityDesc);
                } catch (Exception e) {
                    failed++;
                    log.error("策略 {} ({})预报数据采集触发失败", strategy.getId(), priorityDesc, e);
                }
            }

            log.info("{}优先级预报数据采集任务完成，触发: {}, 跳过: {}, 失败: {}", priorityDesc, triggered, skipped, failed);
        } catch (Exception e) {
            log.error("{}优先级预报数据采集任务执行失败", priorityDesc, e);
        }
    }

    /**
     * 检查策略是否在有效期内
     */
    private boolean isStrategyValid(WeatherStrategy strategy, String todayStr) {
        // 检查删除标识
        if (strategy.getDelFlag() != 0) {
            return false;
        }
        // 已取消 / 已完成 的策略不再采集
        String status = strategy.getCollectStatus();
        if (String.valueOf(CollectStatusEnum.CANCELLED.getCode()).equals(status)
                || String.valueOf(CollectStatusEnum.SUCCESS.getCode()).equals(status)) {
            return false;
        }
        // 检查采集开始时间
        if (strategy.getCollectStart() != null && todayStr.compareTo(strategy.getCollectStart()) < 0) {
            return false;
        }
        // 检查采集结束时间
        if (strategy.getCollectEnd() != null && todayStr.compareTo(strategy.getCollectEnd()) > 0) {
            return false;
        }
        return true;
    }

    /**
     * 结束时间到期检查：
     * 只有定时任务判断结束时间是否小于当前时间，如果是则状态更改为采集完成
     *
     * @return true 表示已到期并已尝试更新为采集完成（调用方应跳过后续采集）
     */
    private boolean markSuccessIfExpired(WeatherStrategy strategy) {
        if (strategy == null) {
            return false;
        }
        if (!StringUtils.hasText(strategy.getCollectEnd())) {
            return false;
        }
        // 已取消/已完成的不处理
        String status = strategy.getCollectStatus();
        if (String.valueOf(CollectStatusEnum.CANCELLED.getCode()).equals(status)
                || String.valueOf(CollectStatusEnum.SUCCESS.getCode()).equals(status)) {
            return false;
        }
        LocalDateTime endTime = DateTimeUtils.parseDateTime(strategy.getCollectEnd());
        if (endTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (endTime.isBefore(now)) {
            try {
                WeatherStrategy update = new WeatherStrategy();
                update.setId(strategy.getId());
                update.setCollectStatus(String.valueOf(CollectStatusEnum.SUCCESS.getCode()));
                update.setUpdateTime(new java.util.Date());
                weatherStrategyService.updateById(update);
                log.info("策略 {} 已到期(collectEnd: {} < now: {})，状态更新为采集完成", strategy.getId(), endTime, now);
            } catch (Exception e) {
                log.warn("策略 {} 到期状态更新失败，将在下次任务重试", strategy.getId(), e);
            }
            return true;
        }
        return false;
    }
}

