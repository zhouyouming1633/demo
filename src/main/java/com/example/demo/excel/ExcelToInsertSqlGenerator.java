package com.example.demo.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel读取并生成INSERT语句工具类
 * 读取Excel中的电站编号,生成push_data_station_info表的INSERT语句
 *
 * @author Youming.Zhou
 * @date 2026/05/28
 */
public class ExcelToInsertSqlGenerator {

    /**
     * 推送方编码
     */
    private static final String PARTY_CODE = "ZHONG_XIN";

    /**
     * 创建人ID
     */
    private static final Long CREATE_BY = 1L;

    /**
     * 读取Excel并生成INSERT语句
     *
     * @param excelFilePath Excel文件路径
     */
    public static void generateInsertSql(String excelFilePath) {
        List<String> stationNoList = new ArrayList<>();

        // 使用EasyExcel读取Excel文件
        EasyExcel.read(excelFilePath, StationExcelData.class, new ReadListener<StationExcelData>() {
            @Override
            public void invoke(StationExcelData data, AnalysisContext context) {
          // 过滤空数据
         if (data != null && StringUtils.isNotBlank(data.getStationNo())) {
                 stationNoList.add(data.getStationNo().trim());
              }
        }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                System.out.println("Excel读取完成,共读取 " + stationNoList.size() + " 条电站编号");
            }
        }).sheet().doRead();

        // 生成INSERT语句
        if (stationNoList.isEmpty()) {
            System.out.println("未读取到任何电站编号数据");
            return;
        }

        generateBatchInsertSql(stationNoList);
    }

    /**
     * 生成批量INSERT语句
     * 格式: INSERT INTO push_data_station_info (party_code, station_no, create_by) VALUES ('ZHONG_XIN', '站点1', 1), ('ZHONG_XIN', '站点2', 1);
     *
     * @param stationNoList 电站编号列表
     */
    private static void generateBatchInsertSql(List<String> stationNoList) {
     StringBuilder sql = new StringBuilder();
     sql.append("INSERT INTO push_data_station_info (party_code, station_no, create_by) VALUES ");

        for (int i = 0; i < stationNoList.size(); i++) {
            String stationNo = stationNoList.get(i);
            sql.append("('").append(PARTY_CODE).append("', '")
             .append(stationNo).append("', ")
                    .append(CREATE_BY).append(")");

       // 如果不是最后一条,添加逗号
          if (i < stationNoList.size() - 1) {
                sql.append(", ");
            }
        }

        sql.append(";");

        // 打印SQL语句
        System.out.println(sql.toString());
    }

    /**
     * 主方法,用于测试
     *
     * @param args 命令行参数,第一个参数为Excel文件路径
     */
    public static void main(String[] args) {
        //push_data_station_info生成
        generateInsertSql("C:\\Users\\zhouy\\Desktop\\中信电量补推\\push_data_station_info生成sql.xlsx");
    }
}
