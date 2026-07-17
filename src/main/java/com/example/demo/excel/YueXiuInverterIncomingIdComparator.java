package com.example.demo.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 越秀逆变器变更电站进件序号比对工具类。
 * 用于比对历史数据Excel中存在、但线上已有Excel中不存在的电站进件序号数据。
 *
 * @author Youming.Zhou
 * @date 2026/07/13
 */
public class YueXiuInverterIncomingIdComparator {

    /**
     * 文件所在目录。
     */
    private static final String FILE_DIR = "C:\\Users\\zhouy\\Desktop\\晶科需求\\越秀逆变器变更\\v2";

    /**
     * 越秀逆变器变更历史数据Excel文件路径。
     */
    private static final String HISTORY_FILE_PATH = FILE_DIR + "\\2026-07批次逆变器变更-历史数据-v2.xlsx";

    /**
     * 线上已有数据Excel文件路径。
     */
    private static final String ONLINE_FILE_PATH = FILE_DIR + "\\线上已有的.xls";

    /**
     * 电站进件序号表头。
     */
    private static final String INCOMING_ID_HEADER = "电站进件序号";

    /**
     * 电站编号表头。
     */
    private static final String STATION_NO_HEADER = "电站编号";

    /**
     * 打印历史数据中存在、但线上已有数据中不存在的记录。
     *
     * @param historyFilePath 历史数据Excel文件路径
     * @param onlineFilePath 线上已有数据Excel文件路径
     */
    public static void printHistoryNotInOnline(String historyFilePath, String onlineFilePath) {
        List<IncomingRecord> historyRecordList = readIncomingRecordList(historyFilePath, true);
        Set<String> onlineIncomingIdSet = readIncomingIdSet(onlineFilePath);

        List<IncomingRecord> missingRecordList = historyRecordList.stream()
                .filter(record -> !onlineIncomingIdSet.contains(record.getIncomingId()))
                .collect(Collectors.toList());

        System.out.println("历史数据有效电站进件序号数：" + historyRecordList.size());
        System.out.println("线上已有有效电站进件序号数：" + onlineIncomingIdSet.size());
        System.out.println("历史数据中不在线上已有数据内的数量：" + missingRecordList.size());
        printMissingRecordList(missingRecordList);
    }

    /**
     * 读取Excel中的电站进件序号集合。
     *
     * @param excelFilePath Excel文件路径
     * @return 电站进件序号集合
     */
    private static Set<String> readIncomingIdSet(String excelFilePath) {
        List<IncomingRecord> incomingRecordList = readIncomingRecordList(excelFilePath, false);
        return incomingRecordList.stream()
                .map(IncomingRecord::getIncomingId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 读取Excel中的电站进件序号记录。
     *
     * @param excelFilePath Excel文件路径
     * @param readStationNo 是否读取电站编号
     * @return 电站进件序号记录列表
     */
    private static List<IncomingRecord> readIncomingRecordList(String excelFilePath, boolean readStationNo) {
        List<IncomingRecord> incomingRecordList = new ArrayList<>();
        List<String> duplicateIncomingIdList = new ArrayList<>();
        Set<String> incomingIdSet = new LinkedHashSet<>();
        Map<String, Integer> headerIndexMap = new LinkedHashMap<>();

        EasyExcel.read(excelFilePath, new AnalysisEventListener<Map<Integer, String>>() {
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                headerIndexMap.put(INCOMING_ID_HEADER, findColumnIndex(headMap, INCOMING_ID_HEADER));
                headerIndexMap.put(STATION_NO_HEADER, findColumnIndex(headMap, STATION_NO_HEADER));
            }

            @Override
            public void invoke(Map<Integer, String> rowDataMap, AnalysisContext context) {
                Integer incomingIdIndex = headerIndexMap.get(INCOMING_ID_HEADER);
                if (incomingIdIndex == null) {
                    throw new IllegalArgumentException("Excel缺少表头：" + INCOMING_ID_HEADER + "，文件：" + excelFilePath);
                }

                String incomingId = StringUtils.trimToNull(rowDataMap.get(incomingIdIndex));
                if (StringUtils.isBlank(incomingId)) {
                    return;
                }

                if (!incomingIdSet.add(incomingId)) {
                    duplicateIncomingIdList.add(incomingId);
                }

                IncomingRecord incomingRecord = new IncomingRecord();
                incomingRecord.setIncomingId(incomingId);
                incomingRecord.setRowIndex(context.readRowHolder().getRowIndex() + 1);
                if (readStationNo) {
                    incomingRecord.setStationNo(readStationNo(rowDataMap, headerIndexMap.get(STATION_NO_HEADER)));
                }
                incomingRecordList.add(incomingRecord);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成，文件：" + excelFilePath + "，有效数据数：" + incomingRecordList.size()
                        + "，重复电站进件序号数：" + duplicateIncomingIdList.size());
                printDuplicateIncomingIdList(excelFilePath, duplicateIncomingIdList);
            }
        }).sheet().doRead();

        return incomingRecordList;
    }

    /**
     * 根据表头名称查找列下标。
     *
     * @param headMap Excel表头映射
     * @param headerName 表头名称
     * @return 列下标，找不到时返回null
     */
    private static Integer findColumnIndex(Map<Integer, String> headMap, String headerName) {
        for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
            if (StringUtils.equals(StringUtils.trim(entry.getValue()), headerName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 读取电站编号字段。
     *
     * @param rowDataMap Excel行数据映射
     * @param stationNoIndex 电站编号列下标
     * @return 电站编号
     */
    private static String readStationNo(Map<Integer, String> rowDataMap, Integer stationNoIndex) {
        if (stationNoIndex == null) {
            return null;
        }
        return StringUtils.trimToNull(rowDataMap.get(stationNoIndex));
    }

    /**
     * 打印重复的电站进件序号。
     *
     * @param excelFilePath Excel文件路径
     * @param duplicateIncomingIdList 重复的电站进件序号列表
     */
    private static void printDuplicateIncomingIdList(String excelFilePath, List<String> duplicateIncomingIdList) {
        if (duplicateIncomingIdList.isEmpty()) {
            return;
        }

        System.out.println("========== 文件内重复电站进件序号，明细仍逐行参与比对：" + excelFilePath + " ==========");
        duplicateIncomingIdList.forEach(System.out::println);
        System.out.println("========== 重复电站进件序号打印结束 ==========");
    }

    /**
     * 打印历史数据中不在线上已有数据内的记录。
     *
     * @param missingRecordList 缺失记录列表
     */
    private static void printMissingRecordList(List<IncomingRecord> missingRecordList) {
        if (missingRecordList.isEmpty()) {
            System.out.println("历史数据中的电站进件序号均已在线上已有数据中存在");
            return;
        }

        System.out.println("========== 历史数据中不在线上已有数据内的记录 ==========");
        for (IncomingRecord record : missingRecordList) {
            //System.out.println("行号：" + record.getRowIndex() + "，电站进件序号：" + record.getIncomingId() + "，电站编号：" + StringUtils.defaultString(record.getStationNo()));
            System.out.println("电站进件序号：" + record.getIncomingId() + "，电站编号：" + StringUtils.defaultString(record.getStationNo()));
        }
        System.out.println("========== 缺失记录打印结束 ==========");
    }

    /**
     * 主方法，用于本地执行Excel比对和结果打印。
     *
     * @param args 命令行参数，未使用
     */
    public static void main(String[] args) {
        printHistoryNotInOnline(HISTORY_FILE_PATH, ONLINE_FILE_PATH);
    }

    /**
     * 电站进件序号比对记录。
     * 用于承载历史Excel中的行号、电站进件序号和电站编号。
     */
    @Data
    public static class IncomingRecord {

        /**
         * Excel中的实际行号。
         */
        private Integer rowIndex;

        /**
         * 电站进件序号，用于两个Excel文件之间的比对。
         */
        private String incomingId;

        /**
         * 电站编号，用于打印缺失记录时辅助核对。
         */
        private String stationNo;
    }
}
