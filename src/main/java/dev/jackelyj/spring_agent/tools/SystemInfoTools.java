package dev.jackelyj.spring_agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class SystemInfoTools {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();

    @Tool(description = "获取系统健康状态")
    public String getSystemHealth() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("系统健康状态检查:\n");

            // 检查内存使用情况
            var heapMemory = memoryBean.getHeapMemoryUsage();
            long maxHeap = heapMemory.getMax();
            long usedHeap = heapMemory.getUsed();
            double heapUsagePercent = maxHeap > 0 ? (double) usedHeap / maxHeap * 100 : 0;

            result.append("  内存使用率: ").append(DECIMAL_FORMAT.format(heapUsagePercent)).append("%\n");

            // 检查系统负载
            double systemLoad = osBean.getSystemLoadAverage();
            if (systemLoad >= 0) {
                result.append("  系统负载: ").append(DECIMAL_FORMAT.format(systemLoad)).append("\n");
            } else {
                result.append("  系统负载: 不可用\n");
            }

            // 检查可用处理器
            result.append("  可用处理器: ").append(osBean.getAvailableProcessors()).append("\n");

            // 检查JVM运行时间
            long uptime = runtimeBean.getUptime();
            result.append("  JVM运行时间: ").append(formatUptime(uptime)).append("\n");

            // 简单的健康状态评估
            String healthStatus = "健康";
            if (heapUsagePercent > 90) {
                healthStatus = "内存使用率过高";
            } else if (systemLoad > osBean.getAvailableProcessors() * 2) {
                healthStatus = "系统负载过高";
            }

            result.append("  总体状态: ").append(healthStatus);

            return result.toString();
        } catch (Exception e) {
            return "获取系统健康状态失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取JVM内存使用情况")
    public String getMemoryUsage() {
        try {
            var heapMemory = memoryBean.getHeapMemoryUsage();
            var nonHeapMemory = memoryBean.getNonHeapMemoryUsage();

            long maxHeap = heapMemory.getMax();
            long usedHeap = heapMemory.getUsed();
            long maxNonHeap = nonHeapMemory.getMax();
            long usedNonHeap = nonHeapMemory.getUsed();

            double heapUsagePercent = maxHeap > 0 ? (double) usedHeap / maxHeap * 100 : 0;
            double nonHeapUsagePercent = maxNonHeap > 0 ? (double) usedNonHeap / maxNonHeap * 100 : 0;

            StringBuilder result = new StringBuilder();
            result.append("JVM内存使用情况:\n");
            result.append("堆内存:\n");
            result.append("  已使用: ").append(formatBytes(usedHeap)).append("\n");
            result.append("  最大值: ").append(formatBytes(maxHeap)).append("\n");
            result.append("  使用率: ").append(DECIMAL_FORMAT.format(heapUsagePercent)).append("%\n\n");

            result.append("非堆内存:\n");
            result.append("  已使用: ").append(formatBytes(usedNonHeap)).append("\n");
            result.append("  最大值: ").append(formatBytes(maxNonHeap)).append("\n");
            result.append("  使用率: ").append(DECIMAL_FORMAT.format(nonHeapUsagePercent)).append("%");

            return result.toString();
        } catch (Exception e) {
            return "获取内存信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取操作系统信息")
    public String getSystemInfo() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("操作系统信息:\n");
            result.append("  操作系统: ").append(System.getProperty("os.name")).append("\n");
            result.append("  版本: ").append(System.getProperty("os.version")).append("\n");
            result.append("  架构: ").append(System.getProperty("os.arch")).append("\n");
            result.append("  可用处理器数: ").append(osBean.getAvailableProcessors()).append("\n");
            result.append("  系统负载平均值: ").append(osBean.getSystemLoadAverage());

            return result.toString();
        } catch (Exception e) {
            return "获取系统信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取Java运行时信息")
    public String getJavaRuntimeInfo() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("Java运行时信息:\n");
            result.append("  Java版本: ").append(System.getProperty("java.version")).append("\n");
            result.append("  Java供应商: ").append(System.getProperty("java.vendor")).append("\n");
            result.append("  Java安装路径: ").append(System.getProperty("java.home")).append("\n");
            result.append("  启动时间: ").append(Instant.ofEpochMilli(runtimeBean.getStartTime())).append("\n");
            result.append("  运行时长: ").append(formatUptime(runtimeBean.getUptime())).append("\n");
            result.append("  JVM名称: ").append(runtimeBean.getVmName()).append("\n");
            result.append("  JVM版本: ").append(runtimeBean.getVmVersion());

            return result.toString();
        } catch (Exception e) {
            return "获取Java运行时信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取系统环境变量")
    public String getEnvironmentVariables(
            @ToolParam(description = "环境变量名称，如果不提供则返回所有环境变量的概要") String variableName) {
        try {
            if (variableName != null && !variableName.trim().isEmpty()) {
                String value = System.getenv(variableName);
                if (value != null) {
                    return variableName + " = " + value;
                } else {
                    return "环境变量 '" + variableName + "' 不存在";
                }
            } else {
                Map<String, String> env = System.getenv();
                StringBuilder result = new StringBuilder();
                result.append("系统环境变量概要:\n");
                result.append("  总数: ").append(env.size()).append("\n");
                result.append("  主要变量:\n");

                // 显示一些常用的环境变量
                String[] commonVars = {"PATH", "JAVA_HOME", "USER", "HOME", "TEMP", "TMP"};
                for (String var : commonVars) {
                    String value = env.get(var);
                    if (value != null) {
                        result.append("    ").append(var).append(" = ").append(value).append("\n");
                    }
                }

                return result.toString();
            }
        } catch (Exception e) {
            return "获取环境变量失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取系统属性")
    public String getSystemProperties(
            @ToolParam(description = "系统属性名称，如果不提供则返回所有系统属性的概要") String propertyName) {
        try {
            if (propertyName != null && !propertyName.trim().isEmpty()) {
                String value = System.getProperty(propertyName);
                if (value != null) {
                    return propertyName + " = " + value;
                } else {
                    return "系统属性 '" + propertyName + "' 不存在";
                }
            } else {
                StringBuilder result = new StringBuilder();
                result.append("系统属性概要:\n");
                result.append("  Java版本: ").append(System.getProperty("java.version")).append("\n");
                result.append("  Java主目录: ").append(System.getProperty("java.home")).append("\n");
                result.append("  用户目录: ").append(System.getProperty("user.home")).append("\n");
                result.append("  当前工作目录: ").append(System.getProperty("user.dir")).append("\n");
                result.append("  用户名: ").append(System.getProperty("user.name")).append("\n");
                result.append("  文件分隔符: ").append(System.getProperty("file.separator")).append("\n");
                result.append("  路径分隔符: ").append(System.getProperty("path.separator"));

                return result.toString();
            }
        } catch (Exception e) {
            return "获取系统属性失败: " + e.getMessage();
        }
    }

    @Tool(description = "执行垃圾回收")
    public String runGarbageCollection() {
        try {
            long beforeMemory = memoryBean.getHeapMemoryUsage().getUsed();
            System.gc();
            Thread.sleep(100); // 等待GC完成
            long afterMemory = memoryBean.getHeapMemoryUsage().getUsed();

            long freedMemory = beforeMemory - afterMemory;

            return String.format("垃圾回收执行完成\n" +
                               "  回收前堆内存使用: %s\n" +
                               "  回收后堆内存使用: %s\n" +
                               "  释放内存: %s",
                    formatBytes(beforeMemory),
                    formatBytes(afterMemory),
                    formatBytes(freedMemory));
        } catch (Exception e) {
            return "执行垃圾回收失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取磁盘使用情况")
    public String getDiskUsage(
            @ToolParam(description = "磁盘路径，例如：/ 或 C:，如果不提供则使用当前目录") String path) {
        try {
            if (path == null || path.trim().isEmpty()) {
                path = System.getProperty("user.dir");
            }

            java.io.File disk = new java.io.File(path);
            long totalSpace = disk.getTotalSpace();
            long freeSpace = disk.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;

            return String.format("磁盘使用情况 (%s):\n" +
                               "  总容量: %s\n" +
                               "  已使用: %s\n" +
                               "  可用空间: %s\n" +
                               "  使用率: %s%%",
                    path,
                    formatBytes(totalSpace),
                    formatBytes(usedSpace),
                    formatBytes(freeSpace),
                    DECIMAL_FORMAT.format(usagePercent));
        } catch (Exception e) {
            return "获取磁盘使用情况失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取CPU信息")
    public String getCpuInfo() {
        try {
            int processors = osBean.getAvailableProcessors();
            double systemLoad = osBean.getSystemLoadAverage();

            StringBuilder result = new StringBuilder();
            result.append("CPU信息:\n");
            result.append("  处理器核心数: ").append(processors).append("\n");
            result.append("  系统负载平均值: ");

            if (systemLoad >= 0) {
                result.append(DECIMAL_FORMAT.format(systemLoad)).append("\n");
                double loadPerCpu = systemLoad / processors;
                result.append("  每个核心负载: ").append(DECIMAL_FORMAT.format(loadPerCpu));
            } else {
                result.append("不可用");
            }

            return result.toString();
        } catch (Exception e) {
            return "获取CPU信息失败: " + e.getMessage();
        }
    }

    @Tool(description = "获取线程信息")
    public String getThreadInfo() {
        try {
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            while (rootGroup.getParent() != null) {
                rootGroup = rootGroup.getParent();
            }

            int threadCount = rootGroup.activeCount();
            Thread[] threads = new Thread[threadCount];
            rootGroup.enumerate(threads);

            StringBuilder result = new StringBuilder();
            result.append("线程信息:\n");
            result.append("  活跃线程数: ").append(threadCount).append("\n");

            // 统计不同状态的线程
            Map<String, Integer> stateCount = new HashMap<>();
            for (Thread thread : threads) {
                if (thread != null) {
                    String state = thread.getState().toString();
                    stateCount.put(state, stateCount.getOrDefault(state, 0) + 1);
                }
            }

            result.append("  线程状态分布:\n");
            stateCount.forEach((state, count) ->
                result.append("    ").append(state).append(": ").append(count).append("\n"));

            return result.toString();
        } catch (Exception e) {
            return "获取线程信息失败: " + e.getMessage();
        }
    }

    // 辅助方法
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        return String.format("%.2f %s", bytes / Math.pow(1024, exp), units[exp]);
    }

    private String formatUptime(long uptimeMs) {
        Duration uptime = Duration.ofMillis(uptimeMs);
        long days = uptime.toDays();
        long hours = uptime.toHoursPart();
        long minutes = uptime.toMinutesPart();
        long seconds = uptime.toSecondsPart();

        return String.format("%d天 %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}