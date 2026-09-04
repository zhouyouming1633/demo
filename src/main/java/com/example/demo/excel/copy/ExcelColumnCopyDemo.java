package com.example.demo.excel.copy;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Excel列数据复制示例类。
 * 根据表头映射关系，从源Excel复制指定列数据，并生成一个新的目标Excel文件。
 */
public class ExcelColumnCopyDemo {

    /**
     * 默认表头所在行下标，0表示第一行。
     */
    private static final int HEADER_ROW_INDEX = 0;

    /**
     * 默认数据起始行下标，1表示第二行。
     */
    private static final int DATA_START_ROW_INDEX = 1;

    /**
     * 默认源Excel表头占用行数。
     */
    private static final int DEFAULT_HEADER_ROW_COUNT = 1;

    /**
     * 重复表头占位列下标，表示该表头名称无法唯一匹配到源Excel列。
     */
    private static final int DUPLICATE_SOURCE_HEADER_INDEX = -1;

    /**
     * 默认生成的目标Excel工作表名称。
     */
    private static final String TARGET_SHEET_NAME = "Sheet1";

    /**
     * 生成文件的名称前缀，添加在源文件名之前。
     */
    private static final String OUTPUT_FILE_NAME_PREFIX = "待验证流水_";

    /**
     * 空列映射前缀，Map的key以该前缀开头时表示只生成目标列，数据行保留空值。
     */
    private static final String EMPTY_COLUMN_PREFIX = "EMPTY:";

    /**
     * 默认值列映射key，该列会写入调用方传入的默认值。
     */
    private static final String DEFAULT_VALUE_COLUMN_KEY = "EMPTY:1";

    /**
     * 购电月份源表头名称，仅作为EMPTY:1列的取值配置，不单独生成目标列。
     */
    private static final String PURCHASE_ELECTRICITY_MONTH_HEADER_NAME = "购电月份";

    /**
     * 结算电价列映射key，该列会按应付电费金额除以结算电量计算。
     */
    private static final String SETTLEMENT_PRICE_COLUMN_KEY = "EMPTY:4";

    /**
     * 地区列映射key，该列会从源Excel路径中解析省份或直辖市名称。
     */
    private static final String REGION_COLUMN_KEY = "EMPTY:5";

    /**
     * 地区父目录名称前缀，用于从该目录的下一级目录解析省份或直辖市名称。
     */
    private static final String REGION_PARENT_DIRECTORY_PREFIX = "结算单录入-exce";

    /**
     * 应付电费金额目标表头名称，保存前会从生成Excel中删除该列。
     */
    private static final String PAYABLE_AMOUNT_TARGET_HEADER_NAME = "应付电费金额";

    /**
     * 发电户号目标表头名称，该列需要按文本写入，避免Excel显示为科学计数法。
     */
    private static final String CUSTOMER_NUMBER_TARGET_HEADER_NAME = "*发电户号";

    /**
     * 结算电量目标表头名称，用于计算结算电价。
     */
    private static final String SETTLEMENT_ELECTRICITY_TARGET_HEADER_NAME = "*结算电量(度)";

    /**
     * 结算电价保留的小数位数。
     */
    private static final int SETTLEMENT_PRICE_SCALE = 6;

    /**
     * 读取配置key，表示源Excel表头所在行号，行号从1开始。
     */
    private static final String READ_CONFIG_HEADER_ROW_KEY = "head";

    /**
     * 读取配置key，表示源Excel表头占用行数。
     */
    private static final String READ_CONFIG_HEADER_ROWS_KEY = "headerRows";

    /**
     * 读取配置key，表示源Excel数据起始行号，行号从1开始。
     */
    private static final String READ_CONFIG_DATA_START_ROW_KEY = "dataStart";

    /**
     * 读取配置key，表示源Excel数据截止行号，行号从1开始且包含该行。
     */
    private static final String READ_CONFIG_END_DATA_ROW_KEY = "endData";

    /**
     * 读取配置key，表示需要读取的源Excel工作表名称。
     */
    private static final String READ_CONFIG_SHEET_NAME_KEY = "sheetName";

    /**
     * 从源Excel复制指定列数据，并生成新的目标Excel文件。
     *
     * @param sourceExcelPath 源Excel文件路径，即excel-1路径
     * @param outputDirectoryPath 输出目录路径，生成文件名称会在源Excel文件名前添加待验证流水前缀
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     * @param defaultColumnValue EMPTY:1列默认值
     * @param readConfig 读取配置，sheetName表示工作表名称，head表示源表头行号，headerRows表示表头占用行数，dataStart表示源数据起始行号，endData表示源数据截止行号，行号从1开始
     * @return 输出Excel文件路径，文件名在源文件名前添加待验证流水前缀
     * @throws IOException 文件读取或写入失败时抛出
     */
    public static Path copyColumns(String sourceExcelPath,
                                   String outputDirectoryPath,
                                   Map<String, String> headerMapping,
                                   String defaultColumnValue,
                                   Map<String, ?> readConfig) throws IOException {
        validateHeaderMapping(headerMapping);
        validateReadConfig(readConfig);
        Path outputExcelPath = buildOutputExcelPath(sourceExcelPath, outputDirectoryPath);
        String regionColumnValue = resolveRegionColumnValue(sourceExcelPath, headerMapping);

        try (Workbook sourceWorkbook = openWorkbook(sourceExcelPath);
             Workbook targetWorkbook = new XSSFWorkbook();
             OutputStream outputStream = new FileOutputStream(outputExcelPath.toFile())) {
            Sheet sourceSheet = resolveSourceSheet(sourceWorkbook, readConfig);
            Sheet targetSheet = targetWorkbook.createSheet(TARGET_SHEET_NAME);
            FormulaEvaluator sourceFormulaEvaluator = sourceWorkbook.getCreationHelper().createFormulaEvaluator();

            int sourceHeaderRowIndex = resolveSourceHeaderRowIndex(readConfig);
            int sourceHeaderRowCount = resolveSourceHeaderRowCount(readConfig);
            int sourceDataStartRowIndex = resolveSourceDataStartRowIndex(readConfig, sourceHeaderRowIndex,
                    sourceHeaderRowCount);
            int sourceEndDataRowIndex = resolveSourceEndDataRowIndex(sourceSheet, readConfig);
            validateSourceDataRange(sourceDataStartRowIndex, sourceEndDataRowIndex);
            Map<String, Integer> sourceHeaderIndexMap = buildSourceHeaderIndexMap(sourceSheet, sourceHeaderRowIndex,
                    sourceHeaderRowCount);
            Map<String, Integer> targetHeaderIndexMap = createTargetHeader(targetSheet, headerMapping);
            CellStyle textCellStyle = createTextCellStyle(targetWorkbook);
            CellStyle settlementPriceCellStyle = createSettlementPriceCellStyle(targetWorkbook);

            validateSourceHeadersExist(sourceHeaderIndexMap, headerMapping);
            validateSettlementPriceHeadersExist(sourceHeaderIndexMap, headerMapping);
            copySourceRowsToTarget(sourceSheet, targetSheet, sourceDataStartRowIndex, sourceEndDataRowIndex,
                    headerMapping, sourceHeaderIndexMap,
                    targetHeaderIndexMap, sourceFormulaEvaluator, defaultColumnValue, regionColumnValue, textCellStyle,
                    settlementPriceCellStyle);
            int targetColumnCount = deleteTargetColumnByHeaderName(targetSheet, targetHeaderIndexMap,
                    PAYABLE_AMOUNT_TARGET_HEADER_NAME);
            autoSizeTargetColumns(targetSheet, targetColumnCount);

            targetWorkbook.write(outputStream);
        }

        return outputExcelPath;
    }

    /**
     * 程序入口，用于本地快速验证Excel列复制效果。
     *
     * @param args 命令行参数，暂未使用，可直接修改方法内的文件路径进行验证
     * @throws IOException 文件读取或写入失败时抛出
     */
    public static void main(String[] args) throws IOException {
        //EMPTY:x 表示可以为空 或者 能特殊处理的
        String defaultColumnValue = "202601";
        Map<String, String> headerMapping = new LinkedHashMap<>();
        headerMapping.put("EMPTY:5", "*地区");
        headerMapping.put("项目公司", "*项目公司");
        headerMapping.put("发电户号", "*发电户号");
        headerMapping.put("EMPTY:1", "*发电年月(yyyyMM)");
        headerMapping.put("供电单位", "供电所");
        headerMapping.put("上网电量", "*结算电量(度)");
        headerMapping.put("上网电费", "应付电费金额");
        headerMapping.put("EMPTY:2", "业主姓名");
        headerMapping.put("EMPTY:3", "装机容量(kw)");
        headerMapping.put("EMPTY:4", "结算电价");
        //有的账单是几个月合并起来的，所以要分开记录
        //headerMapping.put("购电月份", "多月份");
        Map<String, Object> readConfig = new HashMap<>();
        // sheetName 未配置时默认读取第一个工作表，需要指定工作表时取消下一行注释并填写名称
        readConfig.put("sheetName", "Sheet1");
        readConfig.put("head", 1);//表头从第几行开始读取
        readConfig.put("endData", 2);//数据行结束行
        //数据行从第几行开始读取，如果 dataStart 不配置，则默认从 head + headerRows 后一行开始读数据
        //readConfig.put("dataStart", 5);
        // headerRows 未配置时默认表头占用1行，需要多行表头时再显式配置

        String sourceExcelPath =
        "\"C:\\Users\\zhouy\\Desktop\\结算单录入-excel\\湖南省\\宜章晶创\\1月\\宜章1月.xlsx\"";
        // 移除从文件资源管理器复制路径时携带的首尾双引号，保留路径中间的字符不变
        String sourceExcelPathWithoutQuotes = sourceExcelPath.replaceAll("^\"|\"$", "");
        // 输出目录自动使用源Excel文件所在目录，无需单独复制和维护目录地址
        String waitExcelPath = resolveExcelParentDirectoryPath(sourceExcelPathWithoutQuotes);
        Path outputExcelPath = copyColumns(
                sourceExcelPathWithoutQuotes,
                waitExcelPath,
                headerMapping,
                defaultColumnValue,
                readConfig
        );

        System.out.println("Excel列数据复制完成，结果文件：" + outputExcelPath);
    }

    /**
     * 获取Excel文件所在的父目录路径。
     *
     * @param excelPath Excel文件路径
     * @return Excel文件所在目录的绝对路径
     */
    private static String resolveExcelParentDirectoryPath(String excelPath) {
        Path excelFilePath = Paths.get(excelPath).toAbsolutePath().normalize();
        Path parentDirectoryPath = excelFilePath.getParent();
        if (parentDirectoryPath == null) {
            throw new IllegalArgumentException("Excel文件必须存在上一级目录：" + excelPath);
        }
        return parentDirectoryPath.toString();
    }

    /**
     * 构建输出Excel文件路径。
     *
     * @param sourceExcelPath 源Excel文件路径
     * @param outputDirectoryPath 输出目录路径
     * @return 输出Excel文件路径，文件名在源文件名前添加待验证流水前缀
     * @throws IOException 创建输出目录失败时抛出
     */
    private static Path buildOutputExcelPath(String sourceExcelPath, String outputDirectoryPath) throws IOException {
        Path sourcePath = Paths.get(sourceExcelPath);
        Path outputDirectory = Paths.get(outputDirectoryPath);
        Path sourceFileName = sourcePath.getFileName();
        if (sourceFileName == null) {
            throw new IllegalArgumentException("源Excel文件名不能为空");
        }

        Files.createDirectories(outputDirectory);
        String outputFileName = prependOutputFileNamePrefix(sourceFileName.toString());
        Path outputExcelPath = outputDirectory.resolve(outputFileName);
        if (sourcePath.toAbsolutePath().normalize().equals(outputExcelPath.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("输出目录不能与源Excel所在目录相同，否则会覆盖源文件");
        }
        return outputExcelPath;
    }

    /**
     * 在源文件名前添加生成文件前缀。
     *
     * @param sourceFileName 源Excel文件名
     * @return 添加待验证流水前缀后的文件名
     */
    private static String prependOutputFileNamePrefix(String sourceFileName) {
        return OUTPUT_FILE_NAME_PREFIX + sourceFileName;
    }

    /**
     * 打开Excel工作簿。
     *
     * @param excelPath Excel文件路径
     * @return Excel工作簿对象
     * @throws IOException 文件读取失败时抛出
     */
    private static Workbook openWorkbook(String excelPath) throws IOException {
        try (InputStream inputStream = new FileInputStream(excelPath)) {
            return WorkbookFactory.create(inputStream);
        }
    }

    /**
     * 解析地区列默认值。
     *
     * @param sourceExcelPath 源Excel文件路径
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     * @return 地区列值，未配置地区列时返回null
     */
    private static String resolveRegionColumnValue(String sourceExcelPath, Map<String, String> headerMapping) {
        if (!headerMapping.containsKey(REGION_COLUMN_KEY)) {
            return null;
        }

        Path sourcePath = Paths.get(sourceExcelPath);
        for (int pathIndex = 0; pathIndex < sourcePath.getNameCount() - 1; pathIndex++) {
            String directoryName = sourcePath.getName(pathIndex).toString();
            if (!directoryName.startsWith(REGION_PARENT_DIRECTORY_PREFIX)) {
                continue;
            }

            String regionName = sourcePath.getName(pathIndex + 1).toString();
            if (isBlank(regionName)) {
                throw new IllegalArgumentException("源Excel路径中地区目录不能为空：" + sourceExcelPath);
            }
            return regionName;
        }

        throw new IllegalArgumentException("源Excel路径未找到地区父目录：" + REGION_PARENT_DIRECTORY_PREFIX
                + "，sourceExcelPath=" + sourceExcelPath);
    }

    /**
     * 校验表头映射关系。
     *
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     */
    private static void validateHeaderMapping(Map<String, String> headerMapping) {
        if (headerMapping == null || headerMapping.isEmpty()) {
            throw new IllegalArgumentException("表头映射关系不能为空");
        }

        Set<String> targetHeaderSet = new HashSet<>();
        for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
            String sourceHeaderName = entry.getKey();
            String targetHeaderName = entry.getValue();
            if (isBlank(sourceHeaderName) || isBlank(targetHeaderName)) {
                throw new IllegalArgumentException("表头映射关系中的源表头和目标表头都不能为空");
            }
            // 购电月份仅用于覆盖EMPTY:1的默认值，不参与目标表头重复校验。
            if (isPurchaseElectricityMonthHeader(sourceHeaderName)) {
                continue;
            }
            if (!targetHeaderSet.add(targetHeaderName.trim())) {
                throw new IllegalArgumentException("生成Excel表头重复：" + targetHeaderName);
            }
        }
    }

    /**
     * 校验读取配置。
     *
     * @param readConfig 读取配置，sheetName表示工作表名称，head表示源表头行号，headerRows表示表头占用行数，dataStart表示源数据起始行号，endData表示源数据截止行号，行号从1开始
     */
    private static void validateReadConfig(Map<String, ?> readConfig) {
        if (readConfig == null || readConfig.isEmpty()) {
            return;
        }

        Object sheetName = readConfig.get(READ_CONFIG_SHEET_NAME_KEY);
        if (readConfig.containsKey(READ_CONFIG_SHEET_NAME_KEY)
                && (!(sheetName instanceof String) || isBlank((String) sheetName))) {
            throw new IllegalArgumentException("读取配置sheetName必须是非空字符串");
        }

        Integer headerRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_HEADER_ROW_KEY);
        Integer headerRowCount = resolveIntegerReadConfig(readConfig, READ_CONFIG_HEADER_ROWS_KEY);
        Integer dataStartRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_DATA_START_ROW_KEY);
        Integer endDataRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_END_DATA_ROW_KEY);
        if (headerRowNumber != null && headerRowNumber <= 0) {
            throw new IllegalArgumentException("读取配置head必须大于0");
        }
        if (headerRowCount != null && headerRowCount <= 0) {
            throw new IllegalArgumentException("读取配置headerRows必须大于0");
        }
        if (dataStartRowNumber != null && dataStartRowNumber <= 0) {
            throw new IllegalArgumentException("读取配置dataStart必须大于0");
        }
        if (endDataRowNumber != null && endDataRowNumber <= 0) {
            throw new IllegalArgumentException("读取配置endData必须大于0");
        }
    }

    /**
     * 根据读取配置选择源Excel工作表，未配置sheetName时默认读取第一个工作表。
     *
     * @param sourceWorkbook 源Excel工作簿
     * @param readConfig 读取配置，sheetName表示工作表名称
     * @return 需要读取的源Excel工作表
     */
    private static Sheet resolveSourceSheet(Workbook sourceWorkbook, Map<String, ?> readConfig) {
        if (readConfig == null || !readConfig.containsKey(READ_CONFIG_SHEET_NAME_KEY)) {
            return sourceWorkbook.getSheetAt(0);
        }

        String sheetName = (String) readConfig.get(READ_CONFIG_SHEET_NAME_KEY);
        Sheet sourceSheet = sourceWorkbook.getSheet(sheetName);
        if (sourceSheet == null) {
            throw new IllegalArgumentException("源Excel中不存在指定工作表：" + sheetName);
        }
        return sourceSheet;
    }

    /**
     * 读取整数类型的Excel配置值。
     *
     * @param readConfig Excel读取配置
     * @param configKey 配置key
     * @return 配置的整数值，未配置时返回null
     */
    private static Integer resolveIntegerReadConfig(Map<String, ?> readConfig, String configKey) {
        if (readConfig == null || !readConfig.containsKey(configKey)) {
            return null;
        }

        Object configValue = readConfig.get(configKey);
        if (!(configValue instanceof Integer)) {
            throw new IllegalArgumentException("读取配置" + configKey + "必须是整数");
        }
        return (Integer) configValue;
    }

    /**
     * 解析源Excel表头行下标。
     *
     * @param readConfig 读取配置，head表示源表头行号，行号从1开始
     * @return 源Excel表头行下标
     */
    private static int resolveSourceHeaderRowIndex(Map<String, ?> readConfig) {
        Integer headerRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_HEADER_ROW_KEY);
        if (headerRowNumber == null) {
            return HEADER_ROW_INDEX;
        }
        return headerRowNumber - 1;
    }

    /**
     * 解析源Excel表头占用行数。
     *
     * @param readConfig 读取配置，headerRows表示表头占用行数
     * @return 源Excel表头占用行数
     */
    private static int resolveSourceHeaderRowCount(Map<String, ?> readConfig) {
        Integer headerRowCount = resolveIntegerReadConfig(readConfig, READ_CONFIG_HEADER_ROWS_KEY);
        if (headerRowCount == null) {
            return DEFAULT_HEADER_ROW_COUNT;
        }
        return headerRowCount;
    }

    /**
     * 解析源Excel数据起始行下标。
     *
     * @param readConfig 读取配置，dataStart表示源数据起始行号，行号从1开始
     * @param sourceHeaderRowIndex 源Excel表头起始行下标
     * @param sourceHeaderRowCount 源Excel表头占用行数
     * @return 源Excel数据起始行下标
     */
    private static int resolveSourceDataStartRowIndex(Map<String, ?> readConfig,
                                                      int sourceHeaderRowIndex,
                                                      int sourceHeaderRowCount) {
        Integer dataStartRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_DATA_START_ROW_KEY);
        if (dataStartRowNumber != null) {
            return dataStartRowNumber - 1;
        }
        return sourceHeaderRowIndex + sourceHeaderRowCount;
    }

    /**
     * 解析源Excel数据截止行下标。
     *
     * @param sourceSheet 源Excel工作表
     * @param readConfig 读取配置，endData表示源数据截止行号，行号从1开始且包含该行
     * @return 源Excel数据截止行下标
     */
    private static int resolveSourceEndDataRowIndex(Sheet sourceSheet, Map<String, ?> readConfig) {
        Integer endDataRowNumber = resolveIntegerReadConfig(readConfig, READ_CONFIG_END_DATA_ROW_KEY);
        if (endDataRowNumber == null) {
            return sourceSheet.getLastRowNum();
        }
        return Math.min(endDataRowNumber - 1, sourceSheet.getLastRowNum());
    }

    /**
     * 校验源Excel数据读取范围。
     *
     * @param sourceDataStartRowIndex 源Excel数据起始行下标
     * @param sourceEndDataRowIndex 源Excel数据截止行下标
     */
    private static void validateSourceDataRange(int sourceDataStartRowIndex, int sourceEndDataRowIndex) {
        if (sourceEndDataRowIndex < sourceDataStartRowIndex) {
            throw new IllegalArgumentException("源Excel数据截止行不能早于数据起始行");
        }
    }

    /**
     * 建立源Excel表头到列下标的映射。
     *
     * @param sourceSheet 源Excel工作表
     * @param sourceHeaderRowIndex 源Excel表头起始行下标
     * @param sourceHeaderRowCount 源Excel表头占用行数
     * @return 源Excel表头名称与列下标的映射关系
     */
    private static Map<String, Integer> buildSourceHeaderIndexMap(Sheet sourceSheet,
                                                                  int sourceHeaderRowIndex,
                                                                  int sourceHeaderRowCount) {
        int sourceHeaderEndRowIndex = sourceHeaderRowIndex + sourceHeaderRowCount - 1;
        int headerColumnCount = resolveHeaderColumnCount(sourceSheet, sourceHeaderRowIndex, sourceHeaderEndRowIndex);
        if (headerColumnCount <= 0) {
            throw new IllegalArgumentException("源Excel第" + (sourceHeaderRowIndex + 1)
                    + "行表头不能为空，sheetName=" + sourceSheet.getSheetName());
        }

        DataFormatter dataFormatter = new DataFormatter();
        Map<String, Integer> headerIndexMap = new HashMap<>();

        // 按列读取多行表头，兼容横向或纵向合并单元格。
        for (int columnIndex = 0; columnIndex < headerColumnCount; columnIndex++) {
            String headerName = buildMergedHeaderName(sourceSheet, sourceHeaderRowIndex, sourceHeaderEndRowIndex,
                    columnIndex, dataFormatter);
            if (isBlank(headerName)) {
                continue;
            }
            registerSourceHeaderIndex(headerIndexMap, headerName, columnIndex);
            registerSourceHeaderIndex(headerIndexMap, resolveLeafHeaderName(headerName), columnIndex);
        }

        return headerIndexMap;
    }

    /**
     * 解析表头区域最大列数。
     *
     * @param sourceSheet 源Excel工作表
     * @param sourceHeaderRowIndex 源Excel表头起始行下标
     * @param sourceHeaderEndRowIndex 源Excel表头结束行下标
     * @return 表头区域最大列数
     */
    private static int resolveHeaderColumnCount(Sheet sourceSheet,
                                                int sourceHeaderRowIndex,
                                                int sourceHeaderEndRowIndex) {
        int headerColumnCount = 0;
        for (int rowIndex = sourceHeaderRowIndex; rowIndex <= sourceHeaderEndRowIndex; rowIndex++) {
            Row headerRow = sourceSheet.getRow(rowIndex);
            if (headerRow == null || headerRow.getLastCellNum() < 0) {
                continue;
            }
            headerColumnCount = Math.max(headerColumnCount, headerRow.getLastCellNum());
        }
        return headerColumnCount;
    }

    /**
     * 构建合并单元格感知的多行表头名称。
     *
     * @param sourceSheet 源Excel工作表
     * @param sourceHeaderRowIndex 源Excel表头起始行下标
     * @param sourceHeaderEndRowIndex 源Excel表头结束行下标
     * @param columnIndex 源Excel列下标
     * @param dataFormatter 单元格格式化器
     * @return 多行表头组合名称，父子表头使用/分隔
     */
    private static String buildMergedHeaderName(Sheet sourceSheet,
                                                int sourceHeaderRowIndex,
                                                int sourceHeaderEndRowIndex,
                                                int columnIndex,
                                                DataFormatter dataFormatter) {
        StringBuilder headerNameBuilder = new StringBuilder();
        String lastHeaderPart = null;
        for (int rowIndex = sourceHeaderRowIndex; rowIndex <= sourceHeaderEndRowIndex; rowIndex++) {
            String headerPart = readMergedHeaderCellValue(sourceSheet, rowIndex, columnIndex, dataFormatter);
            if (isBlank(headerPart) || headerPart.equals(lastHeaderPart)) {
                continue;
            }
            if (headerNameBuilder.length() > 0) {
                headerNameBuilder.append("/");
            }
            headerNameBuilder.append(headerPart);
            lastHeaderPart = headerPart;
        }
        return headerNameBuilder.toString();
    }

    /**
     * 读取表头单元格文本，普通单元格为空时从所在合并区域左上角取值。
     *
     * @param sourceSheet 源Excel工作表
     * @param rowIndex 源Excel行下标
     * @param columnIndex 源Excel列下标
     * @param dataFormatter 单元格格式化器
     * @return 表头单元格文本
     */
    private static String readMergedHeaderCellValue(Sheet sourceSheet,
                                                    int rowIndex,
                                                    int columnIndex,
                                                    DataFormatter dataFormatter) {
        String cellValue = readCellText(sourceSheet, rowIndex, columnIndex, dataFormatter);
        if (!isBlank(cellValue)) {
            return cellValue.trim();
        }

        CellRangeAddress mergedRegion = findMergedRegion(sourceSheet, rowIndex, columnIndex);
        if (mergedRegion == null) {
            return "";
        }
        return readCellText(sourceSheet, mergedRegion.getFirstRow(), mergedRegion.getFirstColumn(), dataFormatter)
                .trim();
    }

    /**
     * 读取单元格格式化文本。
     *
     * @param sourceSheet 源Excel工作表
     * @param rowIndex 源Excel行下标
     * @param columnIndex 源Excel列下标
     * @param dataFormatter 单元格格式化器
     * @return 单元格格式化文本
     */
    private static String readCellText(Sheet sourceSheet,
                                       int rowIndex,
                                       int columnIndex,
                                       DataFormatter dataFormatter) {
        Row row = sourceSheet.getRow(rowIndex);
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return dataFormatter.formatCellValue(cell);
    }

    /**
     * 查找指定单元格所在的合并区域。
     *
     * @param sourceSheet 源Excel工作表
     * @param rowIndex 源Excel行下标
     * @param columnIndex 源Excel列下标
     * @return 合并区域，未找到时返回null
     */
    private static CellRangeAddress findMergedRegion(Sheet sourceSheet, int rowIndex, int columnIndex) {
        for (int mergedRegionIndex = 0; mergedRegionIndex < sourceSheet.getNumMergedRegions(); mergedRegionIndex++) {
            CellRangeAddress mergedRegion = sourceSheet.getMergedRegion(mergedRegionIndex);
            if (mergedRegion.isInRange(rowIndex, columnIndex)) {
                return mergedRegion;
            }
        }
        return null;
    }

    /**
     * 登记源Excel表头索引，重复表头使用特殊下标标记，便于提示改用完整路径匹配。
     *
     * @param headerIndexMap 源Excel表头名称与列下标的映射关系
     * @param headerName 源Excel表头名称
     * @param columnIndex 源Excel列下标
     */
    private static void registerSourceHeaderIndex(Map<String, Integer> headerIndexMap,
                                                  String headerName,
                                                  int columnIndex) {
        if (isBlank(headerName)) {
            return;
        }

        String trimmedHeaderName = headerName.trim();
        Integer existingColumnIndex = headerIndexMap.get(trimmedHeaderName);
        if (existingColumnIndex == null) {
            headerIndexMap.put(trimmedHeaderName, columnIndex);
            return;
        }
        if (existingColumnIndex != columnIndex) {
            headerIndexMap.put(trimmedHeaderName, DUPLICATE_SOURCE_HEADER_INDEX);
        }
    }

    /**
     * 解析多行表头的末级表头名称。
     *
     * @param headerName 多行表头组合名称
     * @return 末级表头名称
     */
    private static String resolveLeafHeaderName(String headerName) {
        if (isBlank(headerName)) {
            return "";
        }

        String trimmedHeaderName = headerName.trim();
        int separatorIndex = trimmedHeaderName.lastIndexOf("/");
        if (separatorIndex < 0 || separatorIndex == trimmedHeaderName.length() - 1) {
            return trimmedHeaderName;
        }
        return trimmedHeaderName.substring(separatorIndex + 1).trim();
    }

    /**
     * 创建目标Excel表头。
     *
     * @param targetSheet 目标Excel工作表
     * @param headerMapping 表头映射关系，key为源Excel表头，value为生成Excel表头
     * @return 生成Excel表头与列下标的映射关系
     */
    private static Map<String, Integer> createTargetHeader(Sheet targetSheet, Map<String, String> headerMapping) {
        Row targetHeaderRow = targetSheet.createRow(HEADER_ROW_INDEX);
        Map<String, Integer> targetHeaderIndexMap = new LinkedHashMap<>();
        int targetColumnIndex = 0;

        // 按Map迭代顺序生成目标Excel表头，建议传入LinkedHashMap保证列顺序稳定。
        for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
            // 购电月份是EMPTY:1的可选数据来源，不在目标Excel中单独创建列。
            if (isPurchaseElectricityMonthHeader(entry.getKey())) {
                continue;
            }
            String trimmedTargetHeaderName = entry.getValue().trim();
            Cell targetHeaderCell = targetHeaderRow.createCell(targetColumnIndex);
            targetHeaderCell.setCellValue(trimmedTargetHeaderName);
            targetHeaderIndexMap.put(trimmedTargetHeaderName, targetColumnIndex);
            targetColumnIndex++;
        }

        return targetHeaderIndexMap;
    }

    /**
     * 创建文本单元格样式。
     *
     * @param targetWorkbook 目标Excel工作簿
     * @return 文本格式单元格样式
     */
    private static CellStyle createTextCellStyle(Workbook targetWorkbook) {
        CellStyle cellStyle = targetWorkbook.createCellStyle();
        cellStyle.setDataFormat(targetWorkbook.createDataFormat().getFormat("@"));
        return cellStyle;
    }

    /**
     * 创建结算电价单元格样式。
     *
     * @param targetWorkbook 目标Excel工作簿
     * @return 保留6位小数的单元格样式
     */
    private static CellStyle createSettlementPriceCellStyle(Workbook targetWorkbook) {
        CellStyle cellStyle = targetWorkbook.createCellStyle();
        cellStyle.setDataFormat(targetWorkbook.createDataFormat().getFormat("0.000000"));
        return cellStyle;
    }

    /**
     * 校验源Excel是否存在映射关系要求的表头。
     *
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param headerMapping 表头映射关系，key为源Excel表头，value为生成Excel表头
     */
    private static void validateSourceHeadersExist(Map<String, Integer> sourceHeaderIndexMap,
                                                   Map<String, String> headerMapping) {
        for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
            String sourceHeaderName = entry.getKey();
            if (isEmptyColumnKey(sourceHeaderName)) {
                continue;
            }
            validateSourceHeaderUsable(sourceHeaderIndexMap, sourceHeaderName);
        }
    }

    /**
     * 校验结算电价计算所需的映射关系是否存在。
     *
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     */
    private static void validateSettlementPriceHeadersExist(Map<String, Integer> sourceHeaderIndexMap,
                                                            Map<String, String> headerMapping) {
        if (!headerMapping.containsKey(SETTLEMENT_PRICE_COLUMN_KEY)) {
            return;
        }

        String payableAmountSourceHeader = findSourceHeaderNameByTargetHeader(headerMapping,
                PAYABLE_AMOUNT_TARGET_HEADER_NAME);
        String settlementElectricitySourceHeader = findSourceHeaderNameByTargetHeader(headerMapping,
                SETTLEMENT_ELECTRICITY_TARGET_HEADER_NAME);

        if (isBlank(payableAmountSourceHeader)) {
            throw new IllegalArgumentException("使用EMPTY:4计算结算电价时，Map中必须包含目标表头："
                    + PAYABLE_AMOUNT_TARGET_HEADER_NAME);
        }
        if (isBlank(settlementElectricitySourceHeader)) {
            throw new IllegalArgumentException("使用EMPTY:4计算结算电价时，Map中必须包含目标表头："
                    + SETTLEMENT_ELECTRICITY_TARGET_HEADER_NAME);
        }
        validateSourceHeaderUsable(sourceHeaderIndexMap, payableAmountSourceHeader);
        validateSourceHeaderUsable(sourceHeaderIndexMap, settlementElectricitySourceHeader);
    }

    /**
     * 校验源Excel表头是否存在且能唯一定位列。
     *
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param sourceHeaderName 源Excel表头名称
     */
    private static void validateSourceHeaderUsable(Map<String, Integer> sourceHeaderIndexMap,
                                                   String sourceHeaderName) {
        String trimmedSourceHeaderName = sourceHeaderName.trim();
        Integer sourceColumnIndex = sourceHeaderIndexMap.get(trimmedSourceHeaderName);
        if (sourceColumnIndex == null) {
            throw new IllegalArgumentException("源Excel未找到表头：" + sourceHeaderName);
        }
        if (DUPLICATE_SOURCE_HEADER_INDEX == sourceColumnIndex) {
            throw new IllegalArgumentException("源Excel表头匹配不唯一，请在Map中使用完整路径：" + sourceHeaderName);
        }
    }

    /**
     * 将源Excel数据行复制到目标Excel。
     *
     * @param sourceSheet 源Excel工作表
     * @param targetSheet 目标Excel工作表
     * @param sourceDataStartRowIndex 源Excel数据起始行下标
     * @param sourceEndDataRowIndex 源Excel数据截止行下标
     * @param headerMapping 表头映射关系，key为源Excel表头，value为生成Excel表头
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param targetHeaderIndexMap 生成Excel表头与列下标的映射关系
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @param defaultColumnValue EMPTY:1列默认值
     * @param regionColumnValue EMPTY:5列地区值
     * @param textCellStyle 文本单元格样式
     * @param settlementPriceCellStyle 结算电价单元格样式
     */
    private static void copySourceRowsToTarget(Sheet sourceSheet,
                                               Sheet targetSheet,
                                               int sourceDataStartRowIndex,
                                               int sourceEndDataRowIndex,
                                               Map<String, String> headerMapping,
                                               Map<String, Integer> sourceHeaderIndexMap,
                                               Map<String, Integer> targetHeaderIndexMap,
                                               FormulaEvaluator sourceFormulaEvaluator,
                                               String defaultColumnValue,
                                               String regionColumnValue,
                                               CellStyle textCellStyle,
                                               CellStyle settlementPriceCellStyle) {
        int targetRowIndex = DATA_START_ROW_INDEX;
        for (int sourceRowIndex = sourceDataStartRowIndex; sourceRowIndex <= sourceEndDataRowIndex; sourceRowIndex++) {
            Row sourceRow = sourceSheet.getRow(sourceRowIndex);
            Row targetRow = targetSheet.createRow(targetRowIndex);
            copyRowByHeaderMapping(sourceRow, targetRow, headerMapping, sourceHeaderIndexMap,
                    targetHeaderIndexMap, sourceFormulaEvaluator, defaultColumnValue, regionColumnValue, textCellStyle,
                    settlementPriceCellStyle);
            targetRowIndex++;
        }
    }

    /**
     * 按表头映射关系复制单行数据。
     *
     * @param sourceRow 源Excel数据行
     * @param targetRow 目标Excel数据行
     * @param headerMapping 表头映射关系，key为源Excel表头，value为生成Excel表头
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param targetHeaderIndexMap 生成Excel表头与列下标的映射关系
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @param defaultColumnValue EMPTY:1列默认值
     * @param regionColumnValue EMPTY:5列地区值
     * @param textCellStyle 文本单元格样式
     * @param settlementPriceCellStyle 结算电价单元格样式
     */
    private static void copyRowByHeaderMapping(Row sourceRow,
                                               Row targetRow,
                                               Map<String, String> headerMapping,
                                               Map<String, Integer> sourceHeaderIndexMap,
                                               Map<String, Integer> targetHeaderIndexMap,
                                               FormulaEvaluator sourceFormulaEvaluator,
                                               String defaultColumnValue,
                                               String regionColumnValue,
                                               CellStyle textCellStyle,
                                               CellStyle settlementPriceCellStyle) {
        for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
            String sourceHeaderName = entry.getKey().trim();
            String targetHeaderName = entry.getValue().trim();
            // 购电月份只为EMPTY:1提供数据，不在目标Excel中复制为独立列。
            if (isPurchaseElectricityMonthHeader(sourceHeaderName)) {
                continue;
            }
            Integer targetColumnIndex = targetHeaderIndexMap.get(targetHeaderName);
            Cell targetCell = targetRow.createCell(targetColumnIndex);

            if (DEFAULT_VALUE_COLUMN_KEY.equals(sourceHeaderName)) {
                copyDefaultValueOrPurchaseElectricityMonth(sourceRow, targetCell, headerMapping,
                        sourceHeaderIndexMap, sourceFormulaEvaluator, defaultColumnValue);
                continue;
            }
            if (REGION_COLUMN_KEY.equals(sourceHeaderName)) {
                copyRegionValue(regionColumnValue, targetCell);
                continue;
            }
            if (SETTLEMENT_PRICE_COLUMN_KEY.equals(sourceHeaderName)) {
                copySettlementPrice(sourceRow, targetCell, headerMapping, sourceHeaderIndexMap,
                        sourceFormulaEvaluator, settlementPriceCellStyle);
                continue;
            }
            if (isEmptyColumnKey(sourceHeaderName)) {
                targetCell.setBlank();
                continue;
            }

            Integer sourceColumnIndex = sourceHeaderIndexMap.get(sourceHeaderName);
            Cell sourceCell = sourceRow == null ? null : sourceRow.getCell(sourceColumnIndex);
            if (isCustomerNumberTargetColumn(targetHeaderName)) {
                copyCellValueAsText(sourceCell, targetCell, sourceFormulaEvaluator, textCellStyle);
                continue;
            }
            copyCellValue(sourceCell, targetCell, sourceFormulaEvaluator);
        }
    }

    /**
     * 写入EMPTY:1列；配置购电月份源表头时复制源列值，否则写入默认值。
     *
     * @param sourceRow 源Excel数据行
     * @param targetCell 目标Excel单元格
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @param defaultColumnValue EMPTY:1列默认值
     */
    private static void copyDefaultValueOrPurchaseElectricityMonth(
            Row sourceRow,
            Cell targetCell,
            Map<String, String> headerMapping,
            Map<String, Integer> sourceHeaderIndexMap,
            FormulaEvaluator sourceFormulaEvaluator,
            String defaultColumnValue) {
        if (!containsPurchaseElectricityMonthHeader(headerMapping)) {
            copyDefaultValue(defaultColumnValue, targetCell);
            return;
        }

        Integer sourceColumnIndex = sourceHeaderIndexMap.get(PURCHASE_ELECTRICITY_MONTH_HEADER_NAME);
        Cell sourceCell = sourceRow == null ? null : sourceRow.getCell(sourceColumnIndex);
        copyCellValue(sourceCell, targetCell, sourceFormulaEvaluator);
    }

    /**
     * 写入默认值列。
     *
     * @param defaultColumnValue 默认值
     * @param targetCell 目标Excel单元格
     */
    private static void copyDefaultValue(String defaultColumnValue, Cell targetCell) {
        if (isBlank(defaultColumnValue)) {
            targetCell.setBlank();
            return;
        }
        targetCell.setCellValue(defaultColumnValue);
    }

    /**
     * 写入地区列值。
     *
     * @param regionColumnValue 地区列值
     * @param targetCell 目标Excel单元格
     */
    private static void copyRegionValue(String regionColumnValue, Cell targetCell) {
        if (isBlank(regionColumnValue)) {
            targetCell.setBlank();
            return;
        }
        targetCell.setCellValue(regionColumnValue);
    }

    /**
     * 计算并写入结算电价。
     *
     * @param sourceRow 源Excel数据行
     * @param targetCell 目标Excel单元格
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     * @param sourceHeaderIndexMap 源Excel表头与列下标的映射关系
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @param settlementPriceCellStyle 结算电价单元格样式
     */
    private static void copySettlementPrice(Row sourceRow,
                                            Cell targetCell,
                                            Map<String, String> headerMapping,
                                            Map<String, Integer> sourceHeaderIndexMap,
                                            FormulaEvaluator sourceFormulaEvaluator,
                                            CellStyle settlementPriceCellStyle) {
        String payableAmountSourceHeader = findSourceHeaderNameByTargetHeader(headerMapping,
                PAYABLE_AMOUNT_TARGET_HEADER_NAME);
        String settlementElectricitySourceHeader = findSourceHeaderNameByTargetHeader(headerMapping,
                SETTLEMENT_ELECTRICITY_TARGET_HEADER_NAME);
        BigDecimal payableAmount = readNumericValue(sourceRow, sourceHeaderIndexMap.get(payableAmountSourceHeader),
                sourceFormulaEvaluator);
        BigDecimal settlementElectricity = readNumericValue(sourceRow,
                sourceHeaderIndexMap.get(settlementElectricitySourceHeader), sourceFormulaEvaluator);

        if (payableAmount == null || settlementElectricity == null) {
            targetCell.setBlank();
            return;
        }

        targetCell.setCellStyle(settlementPriceCellStyle);
        // 应付电费金额或结算电量任意一个为0时，结算电价统一写入0
        if (BigDecimal.ZERO.compareTo(payableAmount) == 0
                || BigDecimal.ZERO.compareTo(settlementElectricity) == 0) {
            targetCell.setCellValue(0D);
            return;
        }

        BigDecimal settlementPrice = payableAmount.divide(settlementElectricity, SETTLEMENT_PRICE_SCALE,
                RoundingMode.HALF_UP);
        targetCell.setCellValue(settlementPrice.doubleValue());
    }

    /**
     * 按生成Excel表头反查源Excel表头。
     *
     * @param headerMapping 表头映射关系，key为源Excel表头或EMPTY:前缀占位，value为生成Excel表头
     * @param targetHeaderName 生成Excel表头名称
     * @return 源Excel表头名称，未找到时返回null
     */
    private static String findSourceHeaderNameByTargetHeader(Map<String, String> headerMapping, String targetHeaderName) {
        for (Map.Entry<String, String> entry : headerMapping.entrySet()) {
            if (targetHeaderName.equals(entry.getValue().trim())
                    && !isEmptyColumnKey(entry.getKey())
                    && !isPurchaseElectricityMonthHeader(entry.getKey())) {
                return entry.getKey().trim();
            }
        }
        return null;
    }

    /**
     * 读取单元格数字值。
     *
     * @param sourceRow 源Excel数据行
     * @param sourceColumnIndex 源Excel列下标
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @return 数字值，空单元格或无法解析时返回null
     */
    private static BigDecimal readNumericValue(Row sourceRow,
                                               Integer sourceColumnIndex,
                                               FormulaEvaluator sourceFormulaEvaluator) {
        if (sourceRow == null || sourceColumnIndex == null) {
            return null;
        }

        Cell sourceCell = sourceRow.getCell(sourceColumnIndex);
        if (sourceCell == null) {
            return null;
        }

        if (sourceCell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(sourceCell.getNumericCellValue());
        }
        if (sourceCell.getCellType() == CellType.STRING) {
            return parseNumericValue(sourceCell.getStringCellValue());
        }
        if (sourceCell.getCellType() == CellType.FORMULA) {
            CellValue cellValue = sourceFormulaEvaluator.evaluate(sourceCell);
            if (cellValue == null) {
                return null;
            }
            if (cellValue.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cellValue.getNumberValue());
            }
            if (cellValue.getCellType() == CellType.STRING) {
                return parseNumericValue(cellValue.getStringValue());
            }
        }

        return null;
    }

    /**
     * 解析字符串数字。
     *
     * @param value 字符串数字
     * @return 数字值，无法解析时返回null
     */
    private static BigDecimal parseNumericValue(String value) {
        if (isBlank(value)) {
            return null;
        }

        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * 复制单元格值。
     *
     * @param sourceCell 源Excel单元格
     * @param targetCell 目标Excel单元格
     * @param sourceFormulaEvaluator 源Excel公式计算器
     */
    private static void copyCellValue(Cell sourceCell, Cell targetCell, FormulaEvaluator sourceFormulaEvaluator) {
        if (sourceCell == null) {
            targetCell.setBlank();
            return;
        }

        if (sourceCell.getCellType() == CellType.FORMULA) {
            copyFormulaResult(sourceCell, targetCell, sourceFormulaEvaluator);
            return;
        }

        copyNormalCellValue(sourceCell.getCellType(), sourceCell, targetCell);
    }

    /**
     * 按文本复制单元格值，避免长数字在生成Excel中显示为科学计数法。
     *
     * @param sourceCell 源Excel单元格
     * @param targetCell 目标Excel单元格
     * @param sourceFormulaEvaluator 源Excel公式计算器
     * @param textCellStyle 文本单元格样式
     */
    private static void copyCellValueAsText(Cell sourceCell,
                                            Cell targetCell,
                                            FormulaEvaluator sourceFormulaEvaluator,
                                            CellStyle textCellStyle) {
        targetCell.setCellStyle(textCellStyle);
        if (sourceCell == null) {
            targetCell.setBlank();
            return;
        }

        if (sourceCell.getCellType() == CellType.FORMULA) {
            copyFormulaResultAsText(sourceCell, targetCell, sourceFormulaEvaluator);
            return;
        }

        copyNormalCellValueAsText(sourceCell.getCellType(), sourceCell, targetCell);
    }

    /**
     * 按文本复制公式单元格的计算结果。
     *
     * @param sourceCell 源Excel公式单元格
     * @param targetCell 目标Excel单元格
     * @param sourceFormulaEvaluator 源Excel公式计算器
     */
    private static void copyFormulaResultAsText(Cell sourceCell,
                                                Cell targetCell,
                                                FormulaEvaluator sourceFormulaEvaluator) {
        try {
            CellValue cellValue = sourceFormulaEvaluator.evaluate(sourceCell);
            if (cellValue == null) {
                targetCell.setBlank();
                return;
            }
            copyFormulaCellValueAsText(cellValue, targetCell);
        } catch (RuntimeException ex) {
            // 公式计算失败时，使用Excel缓存的公式结果，避免整次复制中断。
            copyNormalCellValueAsText(sourceCell.getCachedFormulaResultType(), sourceCell, targetCell);
        }
    }

    /**
     * 按文本复制普通单元格值。
     *
     * @param cellType 单元格类型
     * @param sourceCell 源Excel单元格
     * @param targetCell 目标Excel单元格
     */
    private static void copyNormalCellValueAsText(CellType cellType, Cell sourceCell, Cell targetCell) {
        switch (cellType) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(formatPlainNumber(sourceCell.getNumericCellValue()));
                break;
            case BOOLEAN:
                targetCell.setCellValue(String.valueOf(sourceCell.getBooleanCellValue()));
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            case BLANK:
            case _NONE:
            default:
                targetCell.setBlank();
                break;
        }
    }

    /**
     * 按文本复制公式计算结果。
     *
     * @param cellValue 公式计算结果
     * @param targetCell 目标Excel单元格
     */
    private static void copyFormulaCellValueAsText(CellValue cellValue, Cell targetCell) {
        switch (cellValue.getCellType()) {
            case STRING:
                targetCell.setCellValue(cellValue.getStringValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(formatPlainNumber(cellValue.getNumberValue()));
                break;
            case BOOLEAN:
                targetCell.setCellValue(String.valueOf(cellValue.getBooleanValue()));
                break;
            case ERROR:
                targetCell.setCellErrorValue(cellValue.getErrorValue());
                break;
            case BLANK:
            case _NONE:
            default:
                targetCell.setBlank();
                break;
        }
    }

    /**
     * 复制公式单元格的计算结果。
     *
     * @param sourceCell 源Excel公式单元格
     * @param targetCell 目标Excel单元格
     * @param sourceFormulaEvaluator 源Excel公式计算器
     */
    private static void copyFormulaResult(Cell sourceCell, Cell targetCell, FormulaEvaluator sourceFormulaEvaluator) {
        try {
            CellValue cellValue = sourceFormulaEvaluator.evaluate(sourceCell);
            if (cellValue == null) {
                targetCell.setBlank();
                return;
            }
            copyFormulaCellValue(cellValue, sourceCell, targetCell);
        } catch (RuntimeException ex) {
            // 公式计算失败时，使用Excel缓存的公式结果，避免整次复制中断。
            copyNormalCellValue(sourceCell.getCachedFormulaResultType(), sourceCell, targetCell);
        }
    }

    /**
     * 按普通单元格类型复制单元格值。
     *
     * @param cellType 单元格类型
     * @param sourceCell 源Excel单元格
     * @param targetCell 目标Excel单元格
     */
    private static void copyNormalCellValue(CellType cellType, Cell sourceCell, Cell targetCell) {
        switch (cellType) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                copyNumericCellValue(sourceCell.getNumericCellValue(), sourceCell, targetCell);
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            case BLANK:
            case _NONE:
            default:
                targetCell.setBlank();
                break;
        }
    }

    /**
     * 按公式计算结果复制单元格值。
     *
     * @param cellValue 公式计算结果
     * @param sourceCell 源Excel公式单元格
     * @param targetCell 目标Excel单元格
     */
    private static void copyFormulaCellValue(CellValue cellValue, Cell sourceCell, Cell targetCell) {
        switch (cellValue.getCellType()) {
            case STRING:
                targetCell.setCellValue(cellValue.getStringValue());
                break;
            case NUMERIC:
                copyNumericCellValue(cellValue.getNumberValue(), sourceCell, targetCell);
                break;
            case BOOLEAN:
                targetCell.setCellValue(cellValue.getBooleanValue());
                break;
            case ERROR:
                targetCell.setCellErrorValue(cellValue.getErrorValue());
                break;
            case BLANK:
            case _NONE:
            default:
                targetCell.setBlank();
                break;
        }
    }

    /**
     * 复制数字或日期类型的单元格值。
     *
     * @param numericValue 数字值
     * @param sourceCell 源Excel单元格
     * @param targetCell 目标Excel单元格
     */
    private static void copyNumericCellValue(double numericValue, Cell sourceCell, Cell targetCell) {
        if (DateUtil.isCellDateFormatted(sourceCell)) {
            targetCell.setCellValue(DateUtil.getJavaDate(numericValue));
            return;
        }
        targetCell.setCellValue(numericValue);
    }

    /**
     * 将数字格式化为普通十进制文本。
     *
     * @param numericValue 数字值
     * @return 普通十进制文本
     */
    private static String formatPlainNumber(double numericValue) {
        return BigDecimal.valueOf(numericValue).stripTrailingZeros().toPlainString();
    }

    /**
     * 按表头名称删除目标Excel列。
     *
     * @param targetSheet 目标Excel工作表
     * @param targetHeaderIndexMap 生成Excel表头与列下标的映射关系
     * @param targetHeaderName 待删除的目标表头名称
     * @return 删除后的目标Excel列数
     */
    private static int deleteTargetColumnByHeaderName(Sheet targetSheet,
                                                      Map<String, Integer> targetHeaderIndexMap,
                                                      String targetHeaderName) {
        Integer deleteColumnIndex = targetHeaderIndexMap.get(targetHeaderName);
        if (deleteColumnIndex == null) {
            return targetHeaderIndexMap.size();
        }

        deleteColumn(targetSheet, deleteColumnIndex);
        return targetHeaderIndexMap.size() - 1;
    }

    /**
     * 删除指定列，并将右侧列整体左移。
     *
     * @param sheet Excel工作表
     * @param deleteColumnIndex 待删除列下标
     */
    private static void deleteColumn(Sheet sheet, int deleteColumnIndex) {
        for (Row row : sheet) {
            int lastCellNumber = row.getLastCellNum();
            if (lastCellNumber < 0 || deleteColumnIndex >= lastCellNumber) {
                continue;
            }

            for (int columnIndex = deleteColumnIndex; columnIndex < lastCellNumber - 1; columnIndex++) {
                Cell sourceCell = row.getCell(columnIndex + 1);
                Cell targetCell = row.getCell(columnIndex);
                if (targetCell == null) {
                    targetCell = row.createCell(columnIndex);
                }
                copyGeneratedCellValue(sourceCell, targetCell);
            }

            Cell lastCell = row.getCell(lastCellNumber - 1);
            if (lastCell != null) {
                row.removeCell(lastCell);
            }
        }
    }

    /**
     * 复制生成Excel内部单元格值，用于删除列时左移数据。
     *
     * @param sourceCell 源单元格
     * @param targetCell 目标单元格
     */
    private static void copyGeneratedCellValue(Cell sourceCell, Cell targetCell) {
        if (sourceCell == null) {
            targetCell.setBlank();
            return;
        }

        targetCell.setCellStyle(sourceCell.getCellStyle());
        switch (sourceCell.getCellType()) {
            case STRING:
                targetCell.setCellValue(sourceCell.getStringCellValue());
                break;
            case NUMERIC:
                targetCell.setCellValue(sourceCell.getNumericCellValue());
                break;
            case BOOLEAN:
                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                break;
            case ERROR:
                targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
                break;
            case BLANK:
            case _NONE:
            default:
                targetCell.setBlank();
                break;
        }
    }

    /**
     * 自动调整目标Excel列宽。
     *
     * @param targetSheet 目标Excel工作表
     * @param columnCount 需要调整列宽的列数
     */
    private static void autoSizeTargetColumns(Sheet targetSheet, int columnCount) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
            targetSheet.autoSizeColumn(columnIndex);
        }
    }

    /**
     * 判断映射key是否表示新增空列。
     *
     * @param sourceHeaderName 映射关系中的key
     * @return true表示新增空列，false表示从源Excel读取列数据
     */
    private static boolean isEmptyColumnKey(String sourceHeaderName) {
        return sourceHeaderName != null && sourceHeaderName.trim().startsWith(EMPTY_COLUMN_PREFIX);
    }

    /**
     * 判断表头映射中是否配置了购电月份源表头。
     *
     * @param headerMapping 表头映射关系
     * @return true表示已配置购电月份源表头，false表示未配置
     */
    private static boolean containsPurchaseElectricityMonthHeader(Map<String, String> headerMapping) {
        for (String sourceHeaderName : headerMapping.keySet()) {
            if (isPurchaseElectricityMonthHeader(sourceHeaderName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断源表头名称是否为购电月份。
     *
     * @param sourceHeaderName 源表头名称
     * @return true表示购电月份源表头，false表示其他表头
     */
    private static boolean isPurchaseElectricityMonthHeader(String sourceHeaderName) {
        return sourceHeaderName != null
                && PURCHASE_ELECTRICITY_MONTH_HEADER_NAME.equals(sourceHeaderName.trim());
    }

    /**
     * 判断目标列是否为发电户号列。
     *
     * @param targetHeaderName 生成Excel表头名称
     * @return true表示发电户号列，false表示其他列
     */
    private static boolean isCustomerNumberTargetColumn(String targetHeaderName) {
        return targetHeaderName != null && CUSTOMER_NUMBER_TARGET_HEADER_NAME.equals(targetHeaderName.trim());
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 待判断字符串
     * @return true表示字符串为空白，false表示字符串非空白
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
