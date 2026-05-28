package com.example.demo.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel数据实体类
 * 用于读取Excel中的电站编号
 *
 * @author Youming.Zhou
 * @date 2026/05/28
 */
@Data
public class StationExcelData {

    /**
     * 电站编号
     */
    @ExcelProperty("电站编号")
    private String stationNo;
}
