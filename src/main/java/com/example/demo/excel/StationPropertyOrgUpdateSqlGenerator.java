package com.example.demo.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 电站归属公司UPDATE语句生成工具类。
 * 用于读取包含“电站编号”和“公司Id”表头的Excel，并按公司Id分组生成ord_order表的UPDATE语句。
 *
 * @author Youming.Zhou
 * @date 2026/05/31
 */
public class StationPropertyOrgUpdateSqlGenerator {

    /**
     * 读取Excel并按公司Id分组打印UPDATE语句。
     *
     * @param excelFilePath Excel文件路径
     */
    public static void printUpdateSql(String excelFilePath) {
        Map<String, List<String>> companyStationMap = readStationGroupByCompanyId(excelFilePath);

        if (companyStationMap.isEmpty()) {
            System.out.println("未读取到任何有效的电站编号和公司Id数据");
            return;
        }

        for (Map.Entry<String, List<String>> entry : companyStationMap.entrySet()) {
            System.out.println(buildUpdateSql(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * 读取Excel数据，并按公司Id分组电站编号。
     *
     * @param excelFilePath Excel文件路径
     * @return 公司Id和电站编号列表的分组结果
     */
    private static Map<String, List<String>> readStationGroupByCompanyId(String excelFilePath) {
        Map<String, List<String>> companyStationMap = new LinkedHashMap<>();

        // 使用EasyExcel按表头读取Excel，过滤电站编号或公司Id为空的数据行。
        EasyExcel.read(excelFilePath, StationCompanyExcelData.class, new ReadListener<StationCompanyExcelData>() {
            @Override
            public void invoke(StationCompanyExcelData data, AnalysisContext context) {
                if (data == null || StringUtils.isBlank(data.getStationNo()) || StringUtils.isBlank(data.getCompanyId())) {
                    return;
                }

                companyStationMap.computeIfAbsent(data.getCompanyId().trim(), key -> new ArrayList<>())
                        .add(data.getStationNo().trim());
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，共生成 " + companyStationMap.size() + " 个公司Id分组");
            }
        }).sheet().doRead();

        return companyStationMap;
    }

    /**
     * 根据公司Id和电站编号列表生成UPDATE语句。
     *
     * @param companyId 公司Id，对应PROPERTYORG字段
     * @param stationNoList 电站编号列表，对应STATIONNO字段
     * @return ord_order表的UPDATE语句
     */
    private static String buildUpdateSql(String companyId, List<String> stationNoList) {
        String stationNoSql = stationNoList.stream()
                .map(StationPropertyOrgUpdateSqlGenerator::wrapSqlString)
                .collect(Collectors.joining(","));

        return "UPDATE ord_order SET PROPERTYORG = " + companyId + " WHERE STATIONNO IN(" + stationNoSql + ");";
    }

    /**
     * 将文本包装为SQL字符串，并转义文本中的单引号。
     *
     * @param value 原始文本
     * @return 包装后的SQL字符串
     */
    private static String wrapSqlString(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 主方法，用于本地执行Excel读取和SQL打印。
     *
     * @param args 命令行参数，第一个参数为Excel文件路径
     */
    public static void main(String[] args) {

        printUpdateSql("C:\\Users\\zhouy\\Desktop\\需要改公司编码的.xlsx");
    }

    /**
     * Excel行数据实体类。
     * 用于承载Excel中的电站编号和公司Id字段。
     */
    @Data
    public static class StationCompanyExcelData {

        /**
         * 电站编号。
         */
        @ExcelProperty("电站编号")
        private String stationNo;

        /**
         * 公司Id。
         */
        @ExcelProperty("公司Id")
        private String companyId;
    }
}
