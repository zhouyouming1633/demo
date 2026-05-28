package com.example.demo.excel;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * TXT文件电站编号比较工具类
 * 读取两个TXT文件中的电站编号,输出两个文件中不同的电站编号汇总
 * @author Youming.Zhou
 * @date 2026/05/28
 */
public class TxtStationComparator {

    /**
     * 读取TXT文件中的电站编号
     * 每行一个电站编号
     *
     * @param filePath 文件路径
     * @return 电站编号集合
     */
    private static Set<String> readStationNoFromTxt(String filePath) {
        Set<String> stationNoSet = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
         String line;
            while ((line = reader.readLine()) != null) {
            // 去除空格并过滤空行
                String stationNo = line.trim();
           if (StringUtils.isNotBlank(stationNo)) {
                    stationNoSet.add(stationNo);
              }
        }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + filePath);
            e.printStackTrace();
        }

        return stationNoSet;
    }

    /**
     * 比较两个TXT文件中的电站编号差异
     * 输出两个文件中所有不同的电站编号汇总
     *
     * @param file1Path 第一个TXT文件路径
     * @param file2Path 第二个TXT文件路径
     */
    public static void compareStationNo(String file1Path, String file2Path) {
        System.out.println("开始读取文件...");

        // 读取两个文件的电站编号
        Set<String> stationSet1 = readStationNoFromTxt(file1Path);
        Set<String> stationSet2 = readStationNoFromTxt(file2Path);

        System.out.println("文件1共读取 " + stationSet1.size() + " 个电站编号");
        System.out.println("文件2共读取 " + stationSet2.size() + " 个电站编号");
    System.out.println();

        // 找出所有不同的电站编号(文件1独有 + 文件2独有)
        Set<String> differentStations = new TreeSet<>();

        // 添加文件1中有但文件2中没有的
        for (String station : stationSet1) {
            if (!stationSet2.contains(station)) {
              differentStations.add(station);
            }
      }

    // 添加文件2中有但文件1中没有的
      for (String station : stationSet2) {
            if (!stationSet1.contains(station)) {
                differentStations.add(station);
            }
        }

        // 输出结果
      System.out.println("======================");
        System.out.println("两个文件中不同的电站编号 (共 " + differentStations.size() + " 个):");
        System.out.println("==================");
        if (differentStations.isEmpty()) {
            System.out.println("(无差异,两个文件内容完全相同)");
        } else {
            differentStations.forEach(System.out::println);
        }
    }

    /**
     * 主方法,用于测试
     *
     * @param args 命令行参数,第一个参数为文件1路径,第二个参数为文件2路径
     */
    public static void main(String[] args) {
        //push_data_station_info
        compareStationNo("C:\\Users\\zhouy\\Desktop\\中信电量补推\\中信电站电量补推-比较用.txt", "C:\\Users\\zhouy\\Desktop\\中信电量补推\\push_data_station_info已存在的电站.txt");

        //t_operation_push_station_info
        //compareStationNo("C:\\Users\\zhouy\\Desktop\\中信电量补推\\中信电站电量补推-比较用.txt", "C:\\Users\\zhouy\\Desktop\\中信电量补推\\t_operation_push_station_info已存在的电站.txt");
    }
}
