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
 * 结算卡UPDATE语句生成工具类。
 * 读取Excel中"结算卡"sheet页的数据，按电站orderId分组后生成3条UPDATE语句：
 * 1. 更新 ord_contractlinkordercustomer 表的银行名称和银行卡号
 * 2. 更新 ord_ordercustomer 表的银行名称和银行卡号（ownertype=10）
 * 3. 更新 icbc_accountopen 表的绑定介质和开户行（根据aopenId）
 *
 * @author Youming.Zhou
 * @date 2026/06/11
 */
public class SettlementCardSqlGenerator {



    /**
     * 结算卡Excel文件路径
     */
    private static final String EXCEL_FILE_PATH =
            "C:\\Users\\zhouy\\Desktop\\晶科刷数据\\日常刷数据-手机号与结算卡.xlsx";

    /**
     * 结算卡sheet名称
     */
    private static final String SHEET_NAME = "结算卡";

    /**
     * 手机号sheet名称
     */
    private static final String PHONE_SHEET_NAME = "手机号";

    /**
     * 读取Excel并按orderId分组生成UPDATE语句
     *
     * @param excelFilePath Excel文件路径
     */
    public static void generateBankNoUpdateSql(String excelFilePath) {
        // 按orderId分组读取Excel数据
        Map<String, List<SettlementCardData>> orderDataMap = readSettlementCardData(excelFilePath);

        if (orderDataMap.isEmpty()) {
            System.out.println("未读取到任何有效的结算卡数据");
            return;
        }

        System.out.println("========== 开始生成SQL ==========");

        // 遍历每个orderId分组，生成3条SQL语句
        for (Map.Entry<String, List<SettlementCardData>> entry : orderDataMap.entrySet()) {
            String orderId = entry.getKey();
            List<SettlementCardData> dataList = entry.getValue();

            // 取第一条数据的bankName和bankNo（同一orderId下银行信息一致）
            SettlementCardData firstData = dataList.get(0);
            String bankName = firstData.getBankName();
            String bankNo = firstData.getBankNo();

            // 收集该orderId下所有的aopenId，去重后拼接（数字类型，不加单引号）
            String aopenIdInClause = dataList.stream()
                    .map(SettlementCardData::getAopenId)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.joining(","));

            // 第1条SQL：更新 ord_contractlinkordercustomer 表
            String sql1 = "update ord_contractlinkordercustomer set BANKNAME = "
                    + wrapSqlString(bankName) + ", BANKNO = " + wrapSqlString(bankNo)
                    + " where orderid = " + orderId + ";";

            // 第2条SQL：更新 ord_ordercustomer 表（ownertype=10）
            String sql2 = "update ord_ordercustomer set bankname = "
                    + wrapSqlString(bankName) + ", BANKNO = " + wrapSqlString(bankNo)
                    + " where orderid = " + orderId + " and ownertype = 10;";

            // 第3条SQL：更新 icbc_accountopen 表（根据aopenId）
            String sql3 = "update icbc_accountopen set bindmedium = "
                    + wrapSqlString(bankNo) + ", bindopenbank = " + wrapSqlString(bankName)
                    + " where aopenid in(" + aopenIdInClause + ");";

            // 打印3条SQL，每条以分号结尾并换行
            System.out.println(sql1);
            System.out.println(sql2);
            System.out.println(sql3);
            System.out.println();
        }

        System.out.println("========== SQL生成完毕，共 " + orderDataMap.size() + " 个orderId分组 ==========");
    }

    /**
     * 读取Excel并按stationNo分组生成手机号UPDATE语句。
     * 生成3条UPDATE语句：
     * 1. 更新 ord_order 表的 guestphone（根据STATIONNO）
     * 2. 更新 base_customer 表的 phone（根据CUSID，同一stationNo下cusId相同）
     * 3. 更新 icbc_accountopen 表的 mobileno（根据aopenId）
     *
     * @param excelFilePath Excel文件路径
     */
    public static void generatePhoneUpdateSql(String excelFilePath) {
        // 按stationNo分组读取Excel数据
        Map<String, List<PhoneData>> stationDataMap = readPhoneData(excelFilePath);

        if (stationDataMap.isEmpty()) {
            System.out.println("未读取到任何有效的手机号数据");
            return;
        }

        System.out.println("========== 开始生成手机号UPDATE SQL ==========");

        // 遍历每个stationNo分组，生成3条SQL语句
        for (Map.Entry<String, List<PhoneData>> entry : stationDataMap.entrySet()) {
            String stationNo = entry.getKey();
            List<PhoneData> dataList = entry.getValue();

            // 取第一条数据的newPhoneNo和cusId（同一stationNo下cusId一致）
            PhoneData firstData = dataList.get(0);
            String newPhoneNo = firstData.getNewPhoneNo();
            String cusId = firstData.getCusId();

            // 收集该stationNo下所有的aopenId，去重后拼接（数字类型，不加单引号）
            String aopenIdInClause = dataList.stream()
                    .map(PhoneData::getAopenId)
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .distinct()
                    .collect(Collectors.joining(","));

            // 第1条SQL：更新 ord_order 表的 guestphone（根据STATIONNO）
            String sql1 = "update ord_order set guestphone = "
                    + wrapSqlString(newPhoneNo) + " where STATIONNO = " + wrapSqlString(stationNo) + ";";

            // 第2条SQL：更新 base_customer 表的 phone（根据CUSID，数字类型不加单引号）
            String sql2 = "update base_customer set phone = "
                    + wrapSqlString(newPhoneNo) + " where CUSID = " + cusId + ";";

            // 第3条SQL：更新 icbc_accountopen 表的 mobileno（根据aopenId）
            String sql3 = "update icbc_accountopen set mobileno = "
                    + wrapSqlString(newPhoneNo) + " where aopenid in(" + aopenIdInClause + ");";

            // 打印3条SQL，每条以分号结尾并换行
            System.out.println(sql1);
            System.out.println(sql2);
            System.out.println(sql3);
            System.out.println();
        }

        System.out.println("========== SQL生成完毕，共 " + stationDataMap.size() + " 个stationNo分组 ==========");
    }

    /**
     * 读取Excel中"手机号"sheet页的数据，并按stationNo分组
     *
     * @param excelFilePath Excel文件路径
     * @return 按stationNo分组的手机号数据
     */
    private static Map<String, List<PhoneData>> readPhoneData(String excelFilePath) {
        Map<String, List<PhoneData>> stationDataMap = new LinkedHashMap<>();

        EasyExcel.read(excelFilePath, PhoneData.class, new ReadListener<PhoneData>() {
            @Override
            public void invoke(PhoneData data, AnalysisContext context) {
                // 过滤掉stationNo为空的数据行
                if (data == null || StringUtils.isBlank(data.getStationNo())) {
                    return;
                }

                stationDataMap.computeIfAbsent(data.getStationNo().trim(), key -> new ArrayList<>())
                        .add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，sheet=[" + PHONE_SHEET_NAME + "]，共生成 "
                        + stationDataMap.size() + " 个stationNo分组");
            }
        }).sheet(PHONE_SHEET_NAME).doRead();

        return stationDataMap;
    }

    /**
     * 读取Excel中"结算卡"sheet页的数据，并按orderId分组
     *
     * @param excelFilePath Excel文件路径
     * @return 按orderId分组的结算卡数据
     */
    private static Map<String, List<SettlementCardData>> readSettlementCardData(String excelFilePath) {
        Map<String, List<SettlementCardData>> orderDataMap = new LinkedHashMap<>();

        EasyExcel.read(excelFilePath, SettlementCardData.class, new ReadListener<SettlementCardData>() {
            @Override
            public void invoke(SettlementCardData data, AnalysisContext context) {
                // 过滤掉orderId为空的数据行
                if (data == null || StringUtils.isBlank(data.getOrderId())) {
                    return;
                }

                orderDataMap.computeIfAbsent(data.getOrderId().trim(), key -> new ArrayList<>())
                        .add(data);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，sheet=[" + SHEET_NAME + "]，共生成 "
                        + orderDataMap.size() + " 个orderId分组");
            }
        }).sheet(SHEET_NAME).doRead();

        return orderDataMap;
    }

    /**
     * 将文本包装为SQL字符串，并转义文本中的单引号
     *
     * @param value 原始文本
     * @return 包装后的SQL字符串
     */
    private static String wrapSqlString(String value) {
        if (value == null) {
            return "''";
        }
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 主方法，用于本地执行Excel读取和SQL生成
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 生成结算卡银行卡号UPDATE语句
        generateBankNoUpdateSql(EXCEL_FILE_PATH);

        // 生成手机号UPDATE语句
        generatePhoneUpdateSql(EXCEL_FILE_PATH);
    }

    /**
     * 结算卡Excel行数据实体类。
     * 用于承载Excel中"结算卡"sheet页的字段：stationNo、orderId、aopenId、bankNo、bankName。
     */
    @Data
    public static class SettlementCardData {

        /**
         * 电站编号
         */
        @ExcelProperty("stationNo")
        private String stationNo;

        /**
         * 订单编号，用于分组
         */
        @ExcelProperty("orderId")
        private String orderId;

        /**
         * 开户ID，用于icbc_accountopen表的aopenid条件
         */
        @ExcelProperty("aopenId")
        private String aopenId;

        /**
         * 银行卡号
         */
        @ExcelProperty("bankNo")
        private String bankNo;

        /**
         * 银行名称
         */
        @ExcelProperty("bankName")
        private String bankName;
    }

    /**
     * 手机号Excel行数据实体类。
     * 用于承载Excel中"手机号"sheet页的字段：stationNo、orderId、aopenId、newPhoneNo、cusId。
     */
    @Data
    public static class PhoneData {

        /**
         * 电站编号，用于分组
         */
        @ExcelProperty("stationNo")
        private String stationNo;

        /**
         * 订单编号
         */
        @ExcelProperty("orderId")
        private String orderId;

        /**
         * 开户ID，用于icbc_accountopen表的aopenid条件
         */
        @ExcelProperty("aopenId")
        private String aopenId;

        /**
         * 新手机号
         */
        @ExcelProperty("newPhoneNo")
        private String newPhoneNo;

        /**
         * 客户ID，用于base_customer表的CUSID条件（同一stationNo下cusId相同）
         */
        @ExcelProperty("cusId")
        private String cusId;
    }
}
