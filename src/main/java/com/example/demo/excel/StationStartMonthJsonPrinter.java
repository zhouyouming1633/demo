package com.example.demo.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 电站首日分组JSON打印工具类。
 * 用于读取包含“电站编号”和“首日”表头的Excel，并按首日分组打印JSON。
 *
 * @author Youming.Zhou
 * @date 2026/05/29
 */
public class StationStartMonthJsonPrinter {

    /**
     * 固定结束年月。
     */
    private static final Integer END_YEAR_MONTH = 202606;

    /**
     * 按首日分组读取Excel，并将每个分组打印为JSON。
     *
     * @param excelFilePath Excel文件路径
     */
    public static void printGroupJson(String excelFilePath) {
        Map<Integer, List<String>> map1 = readStationGroupByStartYearMonth(excelFilePath);

        for (Map.Entry<Integer, List<String>> entry : map1.entrySet()) {
            StationMonthJson stationMonthJson = new StationMonthJson();
            stationMonthJson.setStationNos(entry.getValue());
            stationMonthJson.setStartYearMonth(entry.getKey());
            stationMonthJson.setEndYearMonth(END_YEAR_MONTH);
            System.out.println(JSON.toJSONString(stationMonthJson));
        }
    }

    /**
     * 读取Excel数据，并按首日年月分组电站编号。
     *
     * @param excelFilePath Excel文件路径
     * @return 首日年月和电站编号列表的分组结果
     */
    private static Map<Integer, List<String>> readStationGroupByStartYearMonth(String excelFilePath) {
        Map<Integer, List<String>> map1 = new LinkedHashMap<>();

        EasyExcel.read(excelFilePath, StationStartExcelData.class, new ReadListener<StationStartExcelData>() {
            @Override
            public void invoke(StationStartExcelData data, AnalysisContext context) {
                if (data == null || StringUtils.isBlank(data.getStationNo())) {
                    return;
                }

                Integer startYearMonth = Integer.valueOf(data.getStartDay());
                map1.computeIfAbsent(startYearMonth, key -> new ArrayList<>()).add(data.getStationNo().trim());
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，共生成 " + map1.size() + " 个首日分组");
            }
        }).sheet().doRead();

        return map1;
    }


    /**
     * 主方法，用于本地执行Excel读取和JSON打印。
     *
     * @param args 命令行参数，第一个参数为Excel文件路径
     */
    public static void main(String[] args) {
        printGroupJson("C:\\Users\\zhouy\\Desktop\\电站首日数据_验证.xlsx");
    }

    /**
     * Excel行数据实体类。
     * 用于承载Excel中的电站编号和首日字段。
     */
    @Data
    public static class StationStartExcelData {

        /**
         * 电站编号。
         */
        @ExcelProperty("电站编号")
        private String stationNo;

        /**
         * 首日。
         */
        @ExcelProperty("首日")
        private String startDay;
    }

    /**
     * 电站月份JSON输出实体类。
     * 用于生成指定格式的JSON打印内容。
     */
    @Data
    private static class StationMonthJson {

        /**
         * 电站编号列表。
         */
        @JSONField(ordinal = 1)
        private List<String> stationNos;

        /**
         * 开始年月，格式为yyyyMM。
         */
        @JSONField(ordinal = 2)
        private Integer startYearMonth;

        /**
         * 结束年月，固定为202606。
         */
        @JSONField(ordinal = 3)
        private Integer endYearMonth;
    }
}
