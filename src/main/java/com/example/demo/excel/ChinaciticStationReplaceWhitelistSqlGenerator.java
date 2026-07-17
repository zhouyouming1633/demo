package com.example.demo.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 中信电站置换白名单INSERT语句生成工具类。
 * 读取包含incoming_id、old_station_no、new_station_no、new_order_id的Excel，并生成白名单表INSERT语句。
 *
 * @author Youming.Zhou
 * @date 2026/07/17
 */
public class ChinaciticStationReplaceWhitelistSqlGenerator {

    /**
     * 中信电站置换白名单Excel文件路径。
     */
    private static final String EXCEL_FILE_PATH =
            "C:\\Users\\zhouy\\Desktop\\中信电站置换需求\\晶科-21户置换0622.xlsx";

    /**
     * 中信电站置换白名单表名。
     */
    private static final String TABLE_NAME = "chinacitic_station_replace_whitelist";

    /**
     * 读取Excel并打印INSERT语句。
     *
     * @param excelFilePath Excel文件路径
     */
    public static void printInsertSql(String excelFilePath) {
        List<StationReplaceWhitelistData> dataList = readWhitelistData(excelFilePath);

        if (dataList.isEmpty()) {
            System.out.println("未读取到任何有效的中信电站置换白名单数据");
            return;
        }

        System.out.println(buildBatchInsertSql(dataList));
    }

    /**
     * 读取Excel数据，并过滤必填字段为空或订单id格式无效的行。
     *
     * @param excelFilePath Excel文件路径
     * @return 有效的白名单数据列表
     */
    private static List<StationReplaceWhitelistData> readWhitelistData(String excelFilePath) {
        List<StationReplaceWhitelistData> dataList = new ArrayList<>();
        List<StationReplaceWhitelistData> invalidDataList = new ArrayList<>();

        EasyExcel.read(excelFilePath, StationReplaceWhitelistData.class, new ReadListener<StationReplaceWhitelistData>() {
            @Override
            public void invoke(StationReplaceWhitelistData data, AnalysisContext context) {
                if (data == null) {
                    return;
                }

                // 业务逻辑：清理字段后，校验4个必填字段和bigint订单id。
                data.trimField();
                if (data.isRequiredFieldComplete() && data.normalizeNewOrderId()) {
                    dataList.add(data);
                } else {
                    invalidDataList.add(data);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，有效数据：" + dataList.size()
                        + " 条，无效数据：" + invalidDataList.size() + " 条");
            }
        }).sheet().doRead();

        printInvalidData(invalidDataList);
        return dataList;
    }

    /**
     * 打印无效Excel数据，便于核对被跳过的行。
     *
     * @param invalidDataList 无效Excel行数据列表
     */
    private static void printInvalidData(List<StationReplaceWhitelistData> invalidDataList) {
        if (invalidDataList.isEmpty()) {
            return;
        }

        System.out.println("========== 以下数据字段不完整或订单id格式无效，未生成SQL ==========");
        for (StationReplaceWhitelistData data : invalidDataList) {
            System.out.println("incoming_id=" + data.getIncomingId()
                    + ", old_station_no=" + data.getOldStationNo()
                    + ", new_station_no=" + data.getNewStationNo()
                    + ", new_order_id=" + data.getNewOrderId());
        }
        System.out.println("========== 无效数据打印结束 ==========");
    }

    /**
     * 根据白名单数据生成批量INSERT语句。
     *
     * @param dataList 白名单数据列表
     * @return 批量INSERT语句
     */
    private static String buildBatchInsertSql(List<StationReplaceWhitelistData> dataList) {
        String valuesSql = dataList.stream()
                .map(data -> "("
                        + wrapSqlString(data.getIncomingId()) + ", "
                        + wrapSqlString(data.getOldStationNo()) + ", "
                        + wrapSqlString(data.getNewStationNo()) + ", "
                        + data.getNewOrderId()
                        + ")")
                .collect(Collectors.joining(",\n"));

        return "INSERT INTO " + TABLE_NAME + " (incoming_id, old_station_no, new_station_no, new_order_id) VALUES\n"
                + valuesSql + ";";
    }

    /**
     * 将文本包装为SQL字符串，并转义文本中的单引号。
     *
     * @param value 原始文本
     * @return SQL字符串字面量
     */
    private static String wrapSqlString(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 主方法，用于本地执行Excel读取和SQL生成。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        printInsertSql(EXCEL_FILE_PATH);
    }

    /**
     * 中信电站置换白名单Excel行数据。
     * 用于承载incoming_id、old_station_no、new_station_no、new_order_id四个字段。
     */
    @Data
    public static class StationReplaceWhitelistData {

        /**
         * 置换前电站对应进件Id，对应incoming_id字段。
         */
        @ExcelProperty("incoming_id")
        private String incomingId;

        /**
         * 置换前电站编号，对应old_station_no字段。
         */
        @ExcelProperty("old_station_no")
        private String oldStationNo;

        /**
         * 置换后电站编号，对应new_station_no字段。
         */
        @ExcelProperty("new_station_no")
        private String newStationNo;

        /**
         * 置换后电站订单id，对应new_order_id字段。
         */
        @ExcelProperty("new_order_id")
        private String newOrderId;

        /**
         * 去除Excel字段前后空白字符。
         */
        public void trimField() {
            this.incomingId = StringUtils.trimToNull(this.incomingId);
            this.oldStationNo = StringUtils.trimToNull(this.oldStationNo);
            this.newStationNo = StringUtils.trimToNull(this.newStationNo);
            this.newOrderId = StringUtils.trimToNull(this.newOrderId);
        }

        /**
         * 判断当前行是否包含生成INSERT所需的全部字段。
         *
         * @return true表示字段完整，false表示字段不完整
         */
        public boolean isRequiredFieldComplete() {
            return StringUtils.isNoneBlank(this.incomingId, this.oldStationNo, this.newStationNo, this.newOrderId);
        }

        /**
         * 规范化订单id，避免Excel数字单元格读取后出现小数点或科学计数法。
         *
         * @return true表示订单id可作为bigint写入，false表示订单id格式无效
         */
        public boolean normalizeNewOrderId() {
            try {
                BigDecimal normalizedOrderId = new BigDecimal(this.newOrderId).stripTrailingZeros();
                if (normalizedOrderId.scale() > 0) {
                    return false;
                }
                this.newOrderId = normalizedOrderId.toPlainString();
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
}
