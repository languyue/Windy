package com.zj.common.adapter.monitor.collector;

import com.sun.management.OperatingSystemMXBean;
import com.zj.common.adapter.monitor.collector.PhysicsCollect.GarbageHistory;
import com.zj.common.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 实例信息收集
 *
 * @author guyuelan
 * @since 2023/7/4
 */
@Slf4j
public class InstanceCollector {

    public static PhysicsCollect collectPhysics() {
        PhysicsCollect physics = new PhysicsCollect();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        // 获取 CPU 使用率
        double cpuUsage = osBean.getProcessCpuLoad() * 100;
        physics.setCpu(getDoubleValue(cpuUsage));

        //获取线程数
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        physics.setThreads(threadCount);
        log.info("Thread Count: {}", threadCount);

        // 获取堆内存使用情况
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
        double heapMemoryUsagePercentage =
                heapMemoryUsage.getUsed() / (double) heapMemoryUsage.getMax() * 100;
        physics.setHeap(getDoubleValue(heapMemoryUsagePercentage));


        // 获取垃圾收集器信息
        List<GarbageHistory> histories = new ArrayList<>();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            GarbageHistory garbageHistory = new GarbageHistory();
            garbageHistory.setCollector(gcBean.getName());
            garbageHistory.setCollectCount(gcBean.getCollectionCount());
            garbageHistory.setCollectTime(gcBean.getCollectionTime() + " ms");
            histories.add(garbageHistory);
        }
        physics.setHistories(histories);
        physics.setIp(IpUtils.getLocalIP());
        return physics;
    }

    private static String getDoubleValue(double cpuUsage) {
        double value = BigDecimal.valueOf(cpuUsage).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return value + "%";
    }
}
