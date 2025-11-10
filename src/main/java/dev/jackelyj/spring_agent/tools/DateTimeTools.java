package dev.jackelyj.spring_agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

@Component
public class DateTimeTools {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Tool(description = "获取当前日期和时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(ISO_FORMATTER);
    }

    @Tool(description = "获取当前日期")
    public String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    @Tool(description = "获取指定时区的当前时间")
    public String getCurrentTimeInTimeZone(
            @ToolParam(description = "时区ID，例如：Asia/Shanghai, America/New_York") String timeZoneId) {
        try {
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
            ZoneId zoneId = timeZone.toZoneId();
            return ZonedDateTime.now(zoneId).format(ISO_FORMATTER);
        } catch (Exception e) {
            return "错误：无效的时区ID " + timeZoneId + "。请使用有效的时区ID，例如：Asia/Shanghai";
        }
    }

    @Tool(description = "计算两个日期之间的天数差")
    public String calculateDaysBetween(
            @ToolParam(description = "开始日期，格式：yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式：yyyy-MM-dd") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            long days = ChronoUnit.DAYS.between(start, end);
            return String.format("从 %s 到 %s 相差 %d 天", startDate, endDate, Math.abs(days));
        } catch (Exception e) {
            return "错误：日期格式不正确。请使用 yyyy-MM-dd 格式，例如：2024-01-01";
        }
    }

    @Tool(description = "在指定日期上添加天数")
    public String addDaysToDate(
            @ToolParam(description = "基础日期，格式：yyyy-MM-dd") String date,
            @ToolParam(description = "要添加的天数，可以是负数") int days) {
        try {
            LocalDate baseDate = LocalDate.parse(date, DATE_FORMATTER);
            LocalDate resultDate = baseDate.plusDays(days);
            return String.format("%s %s %d 天是 %s",
                date, days > 0 ? "加" : "减", Math.abs(days), resultDate.format(DATE_FORMATTER));
        } catch (Exception e) {
            return "错误：日期格式不正确。请使用 yyyy-MM-dd 格式，例如：2024-01-01";
        }
    }

    @Tool(description = "格式化日期时间")
    public String formatDateTime(
            @ToolParam(description = "日期时间字符串，格式：yyyy-MM-dd HH:mm:ss") String dateTime,
            @ToolParam(description = "输出格式，例如：yyyy年MM月dd日 HH:mm") String format) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return localDateTime.format(formatter);
        } catch (Exception e) {
            return "错误：日期时间格式不正确。请使用 yyyy-MM-dd HH:mm:ss 格式，例如：2024-01-01 14:30:00";
        }
    }

    @Tool(description = "获取当前时间戳（毫秒）")
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    @Tool(description = "将时间戳转换为日期时间字符串")
    public String timestampToDateTime(
            @ToolParam(description = "时间戳（毫秒）") long timestamp) {
        try {
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(ISO_FORMATTER);
        } catch (Exception e) {
            return "错误：无效的时间戳 " + timestamp;
        }
    }

    @Tool(description = "判断是否为闰年")
    public String isLeapYear(
            @ToolParam(description = "年份，例如：2024") int year) {
        boolean isLeap = Year.isLeap(year);
        return String.format("%d年%s闰年", year, isLeap ? "是" : "不是");
    }

    @Tool(description = "获取指定月份的天数")
    public String getDaysInMonth(
            @ToolParam(description = "年份，例如：2024") int year,
            @ToolParam(description = "月份，1-12") int month) {
        try {
            YearMonth yearMonth = YearMonth.of(year, month);
            int days = yearMonth.lengthOfMonth();
            return String.format("%d年%d月有%d天", year, month, days);
        } catch (Exception e) {
            return "错误：无效的年份或月份。年份应为正数，月份应为1-12之间的数字";
        }
    }
}