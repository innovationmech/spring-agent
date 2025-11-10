package dev.jackelyj.spring_agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Arrays;

@Component
public class CalculatorTools {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##########");

    @Tool(description = "执行基本数学运算（加、减、乘、除）")
    public String calculate(
            @ToolParam(description = "第一个数字") double num1,
            @ToolParam(description = "运算符：+, -, *, /") String operator,
            @ToolParam(description = "第二个数字") double num2) {
        try {
            double result;
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        return "错误：除数不能为零";
                    }
                    result = num1 / num2;
                    break;
                default:
                    return "错误：不支持的运算符。支持的运算符：+, -, *, /";
            }
            return String.format("%s %s %s = %s",
                formatNumber(num1), operator, formatNumber(num2), formatNumber(result));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算数字的幂")
    public String power(
            @ToolParam(description = "基数") double base,
            @ToolParam(description = "指数") double exponent) {
        try {
            double result = Math.pow(base, exponent);
            return String.format("%s ^ %s = %s",
                formatNumber(base), formatNumber(exponent), formatNumber(result));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算平方根")
    public String squareRoot(
            @ToolParam(description = "要计算平方根的数字") double number) {
        try {
            if (number < 0) {
                return "错误：不能计算负数的平方根";
            }
            double result = Math.sqrt(number);
            return String.format("√%s = %s", formatNumber(number), formatNumber(result));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算对数")
    public String logarithm(
            @ToolParam(description = "数字") double number,
            @ToolParam(description = "底数，例如：10表示常用对数，2.71828表示自然对数") double base) {
        try {
            if (number <= 0 || base <= 0 || base == 1) {
                return "错误：数字和底数都必须大于0，且底数不能等于1";
            }
            double result = Math.log(number) / Math.log(base);
            return String.format("log_%s(%s) = %s",
                formatNumber(base), formatNumber(number), formatNumber(result));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算三角函数")
    public String trigonometric(
            @ToolParam(description = "角度（度）") double angle,
            @ToolParam(description = "函数类型：sin, cos, tan") String function) {
        try {
            double radians = Math.toRadians(angle);
            double result;
            String funcName;

            switch (function.toLowerCase()) {
                case "sin":
                    result = Math.sin(radians);
                    funcName = "sin";
                    break;
                case "cos":
                    result = Math.cos(radians);
                    funcName = "cos";
                    break;
                case "tan":
                    result = Math.tan(radians);
                    funcName = "tan";
                    break;
                default:
                    return "错误：不支持的三角函数。支持的函数：sin, cos, tan";
            }

            return String.format("%s(%.2f°) = %s", funcName, angle, formatNumber(result));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算一组数字的平均值")
    public String average(
            @ToolParam(description = "数字列表，用逗号分隔，例如：1,2,3,4,5") String numbers) {
        try {
            double[] nums = parseNumbers(numbers);
            if (nums.length == 0) {
                return "错误：请提供至少一个数字";
            }

            double sum = Arrays.stream(nums).sum();
            double avg = sum / nums.length;

            return String.format("数字 [%s] 的平均值是 %s",
                numbers, formatNumber(avg));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算一组数字的总和")
    public String sum(
            @ToolParam(description = "数字列表，用逗号分隔，例如：1,2,3,4,5") String numbers) {
        try {
            double[] nums = parseNumbers(numbers);
            if (nums.length == 0) {
                return "错误：请提供至少一个数字";
            }

            double sum = Arrays.stream(nums).sum();

            return String.format("数字 [%s] 的总和是 %s",
                numbers, formatNumber(sum));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "找到一组数字中的最大值和最小值")
    public String minMax(
            @ToolParam(description = "数字列表，用逗号分隔，例如：1,2,3,4,5") String numbers) {
        try {
            double[] nums = parseNumbers(numbers);
            if (nums.length == 0) {
                return "错误：请提供至少一个数字";
            }

            double min = Arrays.stream(nums).min().orElse(Double.NaN);
            double max = Arrays.stream(nums).max().orElse(Double.NaN);

            return String.format("数字 [%s] 中，最小值是 %s，最大值是 %s",
                numbers, formatNumber(min), formatNumber(max));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算阶乘")
    public String factorial(
            @ToolParam(description = "非负整数") int n) {
        try {
            if (n < 0) {
                return "错误：不能计算负数的阶乘";
            }
            if (n > 20) {
                return "错误：数字太大，可能导致溢出。请使用20以下的数字";
            }

            long result = 1;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }

            return String.format("%d! = %d", n, result);
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "计算百分比")
    public String percentage(
            @ToolParam(description = "部分值") double part,
            @ToolParam(description = "总值") double total) {
        try {
            if (total == 0) {
                return "错误：总值不能为零";
            }
            double percentage = (part / total) * 100;
            return String.format("%s 占 %s 的百分比是 %s%%",
                formatNumber(part), formatNumber(total), formatNumber(percentage));
        } catch (Exception e) {
            return "计算错误：" + e.getMessage();
        }
    }

    @Tool(description = "单位转换：长度单位")
    public String convertLength(
            @ToolParam(description = "原始数值") double value,
            @ToolParam(description = "原始单位：m, km, cm, mm, inch, ft, yard") String fromUnit,
            @ToolParam(description = "目标单位：m, km, cm, mm, inch, ft, yard") String toUnit) {
        try {
            // 先转换为米
            double meters = convertToMeters(value, fromUnit.toLowerCase());
            if (Double.isNaN(meters)) {
                return "错误：不支持的原始单位 " + fromUnit;
            }

            // 再从米转换为目标单位
            double result = convertFromMeters(meters, toUnit.toLowerCase());
            if (Double.isNaN(result)) {
                return "错误：不支持的目标单位 " + toUnit;
            }

            return String.format("%s %s = %s %s",
                formatNumber(value), fromUnit, formatNumber(result), toUnit);
        } catch (Exception e) {
            return "转换错误：" + e.getMessage();
        }
    }

    @Tool(description = "单位转换：温度单位")
    public String convertTemperature(
            @ToolParam(description = "原始温度值") double value,
            @ToolParam(description = "原始单位：C, F, K") String fromUnit,
            @ToolParam(description = "目标单位：C, F, K") String toUnit) {
        try {
            double celsius = convertToCelsius(value, fromUnit.toUpperCase());
            if (Double.isNaN(celsius)) {
                return "错误：不支持的原始单位 " + fromUnit;
            }

            double result = convertFromCelsius(celsius, toUnit.toUpperCase());
            if (Double.isNaN(result)) {
                return "错误：不支持的目标单位 " + toUnit;
            }

            return String.format("%.1f°%s = %.1f°%s",
                value, fromUnit, result, toUnit);
        } catch (Exception e) {
            return "转换错误：" + e.getMessage();
        }
    }

    // 辅助方法
    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.format("%d", (long) number);
        }
        return DECIMAL_FORMAT.format(number);
    }

    private double[] parseNumbers(String numbers) {
        return Arrays.stream(numbers.split(","))
                .map(String::trim)
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    private double convertToMeters(double value, String unit) {
        switch (unit) {
            case "m": return value;
            case "km": return value * 1000;
            case "cm": return value / 100;
            case "mm": return value / 1000;
            case "inch": return value * 0.0254;
            case "ft": return value * 0.3048;
            case "yard": return value * 0.9144;
            default: return Double.NaN;
        }
    }

    private double convertFromMeters(double meters, String unit) {
        switch (unit) {
            case "m": return meters;
            case "km": return meters / 1000;
            case "cm": return meters * 100;
            case "mm": return meters * 1000;
            case "inch": return meters / 0.0254;
            case "ft": return meters / 0.3048;
            case "yard": return meters / 0.9144;
            default: return Double.NaN;
        }
    }

    private double convertToCelsius(double value, String unit) {
        switch (unit) {
            case "C": return value;
            case "F": return (value - 32) * 5 / 9;
            case "K": return value - 273.15;
            default: return Double.NaN;
        }
    }

    private double convertFromCelsius(double celsius, String unit) {
        switch (unit) {
            case "C": return celsius;
            case "F": return celsius * 9 / 5 + 32;
            case "K": return celsius + 273.15;
            default: return Double.NaN;
        }
    }
}