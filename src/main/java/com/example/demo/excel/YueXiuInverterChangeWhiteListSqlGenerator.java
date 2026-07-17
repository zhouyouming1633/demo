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
 * 越秀逆变器变更推送白名单INSERT语句生成工具类。
 * 读取包含“电站进件序号”和“电站编号”的Excel，并按电站编号去重后生成白名单表INSERT语句。
 *
 * @author Youming.Zhou
 * @date 2026/07/13
 */
public class YueXiuInverterChangeWhiteListSqlGenerator {

    /**
     * 越秀逆变器变更历史数据Excel文件路径。
     */
    private static final String EXCEL_FILE_PATH =
            "C:\\Users\\zhouy\\Desktop\\晶科需求\\越秀逆变器变更\\v2\\2026-07批次逆变器变更-历史数据-v2.xlsx";

    /**
     * 推送白名单表名。
     */
    private static final String TABLE_NAME = "inverter_change_push_white_list";

    /**
     * 固定业务编码。
     */
    private static final String BIZ_CODE = "YUE_XIU";

    /**
     * 读取Excel并打印INSERT语句。
     *
     * @param excelFilePath Excel文件路径
     */
    public static void printInsertSql(String excelFilePath) {
        List<InverterChangeWhiteListData> whiteListDataList = readWhiteListData(excelFilePath);

        if (whiteListDataList.isEmpty()) {
            System.out.println("未读取到任何有效的电站白名单数据");
            return;
        }

        System.out.println(buildBatchInsertSql(whiteListDataList));
    }

    /**
     * 读取Excel数据，并按电站编号去重。
     *
     * @param excelFilePath Excel文件路径
     * @return 去重后的白名单数据列表
     */
    private static List<InverterChangeWhiteListData> readWhiteListData(String excelFilePath) {
        Map<String, InverterChangeWhiteListData> stationDataMap = new LinkedHashMap<>();
        List<InverterChangeWhiteListData> duplicateDataList = new ArrayList<>();

        EasyExcel.read(excelFilePath, InverterChangeWhiteListData.class, new ReadListener<InverterChangeWhiteListData>() {
            @Override
            public void invoke(InverterChangeWhiteListData data, AnalysisContext context) {
                if (data == null) {
                    return;
                }

                data.trimField();
                if (StringUtils.isBlank(data.getStationNo())) {
                    return;
                }

                InverterChangeWhiteListData oldData = stationDataMap.putIfAbsent(data.getStationNo(), data);
                if (oldData != null) {
                    duplicateDataList.add(data);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，有效电站数：" + stationDataMap.size()
                        + "，重复电站数：" + duplicateDataList.size());
            }
        }).sheet().doRead();

        printDuplicateData(duplicateDataList);
        return new ArrayList<>(stationDataMap.values());
    }

    /**
     * 打印重复电站数据，重复数据不会生成INSERT语句。
     *
     * @param duplicateDataList 重复的Excel行数据列表
     */
    private static void printDuplicateData(List<InverterChangeWhiteListData> duplicateDataList) {
        if (duplicateDataList.isEmpty()) {
            return;
        }

        System.out.println("========== 以下电站重复，未生成SQL ==========");
        for (InverterChangeWhiteListData data : duplicateDataList) {
            System.out.println("电站编号：" + data.getStationNo() + "，电站进件序号：" + data.getIncomingId());
        }
        System.out.println("========== 重复电站打印结束 ==========");
    }

    /**
     * 根据白名单数据生成批量INSERT语句。
     *
     * @param whiteListDataList 白名单数据列表
     * @return 批量INSERT语句
     */
    private static String buildBatchInsertSql(List<InverterChangeWhiteListData> whiteListDataList) {
        String valuesSql = whiteListDataList.stream()
                .map(data -> "("
                        + wrapSqlString(data.getStationNo()) + ", "
                        + wrapSqlString(data.getIncomingId()) + ", "
                        + wrapSqlString(BIZ_CODE)
                        + ")")
                .collect(Collectors.joining(",\n"));

        return "INSERT INTO " + TABLE_NAME + " (station_no, incoming_id, biz_code) VALUES\n"
                + valuesSql + ";";
    }

    /**
     * 将文本包装为SQL字符串，并转义文本中的单引号。
     *
     * @param value 原始文本
     * @return 包装后的SQL字符串
     */
    private static String wrapSqlString(String value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 主方法，用于本地执行Excel读取和SQL打印。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        printInsertSql(EXCEL_FILE_PATH);
    }

    /**
     * 越秀逆变器变更白名单Excel行数据。
     * 用于承载Excel中的“电站进件序号”和“电站编号”字段。
     */
    @Data
    public static class InverterChangeWhiteListData {

        /**
         * 电站进件序号，对应白名单表incoming_id字段。
         */
        @ExcelProperty("电站进件序号")
        private String incomingId;

        /**
         * 电站编号，对应白名单表station_no字段，并作为去重依据。
         */
        @ExcelProperty("电站编号")
        private String stationNo;

        /**
         * 去除Excel字段前后空白字符。
         */
        public void trimField() {
            this.incomingId = StringUtils.trimToNull(this.incomingId);
            this.stationNo = StringUtils.trimToNull(this.stationNo);
        }
    }
}
