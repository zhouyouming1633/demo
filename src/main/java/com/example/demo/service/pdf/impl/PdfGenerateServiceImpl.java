package com.example.demo.service.pdf.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.model.entity.Book;
import com.example.demo.service.pdf.PdfGenerateService;
import org.springframework.stereotype.Service;
import wiki.xsx.core.pdf.component.XEasyPdfComponent;
import wiki.xsx.core.pdf.component.table.XEasyPdfCell;
import wiki.xsx.core.pdf.component.table.XEasyPdfRow;
import wiki.xsx.core.pdf.component.table.XEasyPdfTable;
import wiki.xsx.core.pdf.component.text.XEasyPdfText;
import wiki.xsx.core.pdf.doc.XEasyPdfDefaultFontStyle;
import wiki.xsx.core.pdf.doc.XEasyPdfDocument;
import wiki.xsx.core.pdf.doc.XEasyPdfPage;
import wiki.xsx.core.pdf.doc.XEasyPdfPositionStyle;
import wiki.xsx.core.pdf.handler.XEasyPdfHandler;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description 基于x-easypdf的PDF文件生成服务实现
 * @author ZhouYouMing
 * @date 2026/6/12 14:10
 */
@Service
public class PdfGenerateServiceImpl implements PdfGenerateService {

    /**
     * 标题字体大小
     */
    private static final float TITLE_FONT_SIZE = 18F;

    /**
     * 普通文本字体大小
     */
    private static final float NORMAL_FONT_SIZE = 10F;

    /**
     * 表格字体大小
     */
    private static final float TABLE_FONT_SIZE = 8F;

    /**
     * 表格起始X坐标
     */
    private static final float TABLE_BEGIN_X = 35F;

    /**
     * 表格起始Y坐标
     */
    private static final float TABLE_BEGIN_Y = 735F;

    /**
     * 表格行最小高度
     */
    private static final float TABLE_MIN_ROW_HEIGHT = 24F;

    /**
     * @description 根据图书列表生成图书清单PDF字节数组
     * @param books 图书数据列表
     * @return PDF文件字节数组
     */
    @Override
    public byte[] generateBookListPdf(List<Book> books) {
        XEasyPdfDocument document = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            document = XEasyPdfHandler.Document.build(buildBookListPage(books))
                    .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL)
                    .enableReplaceTotalPagePlaceholder();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("生成图书清单PDF失败", e);
        } finally {
            closeDocument(document);
        }
    }

    /**
     * @description 生成运维商账单确认书PDF字节数组
     * @param
     * @return PDF文件字节数组
     */
    @Override
    public byte[] generateOperationBillConfirmationPdf() {
        XEasyPdfDocument document = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            document = XEasyPdfHandler.Document.build(buildOperationBillPages())
                    .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL)
                    .enableReplaceTotalPagePlaceholder();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("生成运维商账单确认书PDF失败", e);
        } finally {
            closeDocument(document);
        }
    }

    /**
     * @description 构建运维商账单确认书PDF页面列表
     * @param
     * @return PDF页面列表
     */
    private List<XEasyPdfPage> buildOperationBillPages() {
        List<XEasyPdfPage> pages = new ArrayList<XEasyPdfPage>();
        JSONObject billConfig = JSON.parseObject(buildMockOperationBillConfirmationJson());
        Map<String, String> placeholderData = queryOperationBillPlaceholderData();
        Map<String, List<Map<String, String>>> tableDataMap = queryOperationBillTableData();
        JSONArray pageConfigs = billConfig.getJSONArray("pages");
        for (int i = 0; i < pageConfigs.size(); i++) {
            pages.add(buildOperationConfiguredPage(pageConfigs.getJSONObject(i), placeholderData, tableDataMap));
        }
        return pages;
    }

    /**
     * @description 根据JSON页面配置构建运维商账单确认书页面
     * @param pageConfig 页面JSON配置
     * @param placeholderData 占位符业务数据
     * @param tableDataMap 表格业务数据集合
     * @return PDF页面对象
     */
    private XEasyPdfPage buildOperationConfiguredPage(JSONObject pageConfig, Map<String, String> placeholderData, Map<String, List<Map<String, String>>> tableDataMap) {
        List<XEasyPdfComponent> components = new ArrayList<XEasyPdfComponent>();
        JSONArray componentConfigs = pageConfig.getJSONArray("components");
        for (int i = 0; i < componentConfigs.size(); i++) {
            components.add(buildOperationConfiguredComponent(componentConfigs.getJSONObject(i), placeholderData, tableDataMap));
        }
        return XEasyPdfHandler.Page.build(XEasyPdfHandler.Page.Rectangle.A4, components);
    }

    /**
     * @description 构建模拟的运维商账单确认书完整JSON配置
     * @param
     * @return 运维商账单确认书完整JSON配置字符串
     */
    private String buildMockOperationBillConfirmationJson() {
        return """
                {
                  "pages": [
                    {
                      "pageCode": "main",
                      "components": [
                        {"type": "title", "text": "确认书", "beginY": 795},
                        {"type": "paragraph", "text": "致：${customerCompanyName}", "beginY": 760, "height": 16},
                        {"type": "paragraph", "text": "根据贵我双方于${contractYear}年签署的协议编号为${agreementNo}的《户用光伏电站运维服务合同》服务内容项下截止至【${settleYear}】年【${settleMonth}】月【${settleDay}】日的${stationCount}户电站运维费用结算事宜，我司特作如下确认：", "beginY": 730, "height": 42},
                        {"type": "subTitle", "text": "一、结算范围说明", "beginY": 675},
                        {"type": "paragraph", "text": "根据合同约定，本次为【${settleYear}】年【${settleMonth}】月月度预结算对账，预结算对账内容为月度基础运维费及月度工单扣罚金额、回购扣罚金额。预结算对账不包含年度工单准时闭环奖励及年度发电绩效奖惩，相关奖惩费用将在年度结算时统一处理。", "beginY": 650, "height": 48},
                        {"type": "subTitle", "text": "二、费用结算明细", "beginY": 590},
                        {"type": "paragraph", "text": "单位：元", "beginY": 568, "height": 14},
                        {"type": "table", "tableCode": "settlementTable", "dataKey": "settlementTableRows", "beginX": 25, "beginY": 548, "fontSize": 7, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "预结算单号", "field": "preSettlementNo", "width": 92},
                          {"title": "运维商", "field": "operationCompanyName", "width": 128},
                          {"title": "${settleMonth}月基础运维费", "field": "baseMaintenanceFee", "width": 78},
                          {"title": "${settleMonth}月工单扣罚金额", "field": "workOrderDeductionAmount", "width": 84},
                          {"title": "电站回购扣罚金额", "field": "buybackDeductionAmount", "width": 84},
                          {"title": "${settleMonth}月可结运维费", "field": "settlementAmount", "width": 79}
                        ]},
                        {"type": "paragraph", "text": "即，${settleYear}年【${settleMonth}】月运维费用预结算对账金额为【${settlementAmount}】元，明细以本确认书附件所列示内容为准。", "beginY": 420, "height": 32},
                        {"type": "subTitle", "text": "三、特别确认事项", "beginY": 375},
                        {"type": "paragraph", "text": "我司确认并同意，若我司出现以下情形，贵司有权依据运维合同第十款第3条（3.3.1）及${policyYear}年发布的《户用运维商政策》第8款第8.5条约定采取相应措施，包括但不限于要求整改、缩减运维区域或解除合同：", "beginY": 350, "height": 46},
                        {"type": "paragraph", "text": "月度工单准时闭环率以及累计闭环率为：", "beginY": 295, "height": 16},
                        {"type": "table", "tableCode": "closeRateTable", "dataKey": "closeRateTableRows", "beginX": 26, "beginY": 275, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 22, "headerRowHeight": 22, "bodyRowHeight": 22, "columns": [
                          {"title": "${settleYear}年1月", "field": "month01", "width": 34},
                          {"title": "${settleYear}年2月", "field": "month02", "width": 34},
                          {"title": "${settleYear}年3月", "field": "month03", "width": 34},
                          {"title": "${settleYear}年Q1", "field": "quarter01", "width": 34},
                          {"title": "${settleYear}年4月", "field": "month04", "width": 34},
                          {"title": "${settleYear}年5月", "field": "month05", "width": 34},
                          {"title": "${settleYear}年6月", "field": "month06", "width": 34},
                          {"title": "${settleYear}年Q2", "field": "quarter02", "width": 34},
                          {"title": "${settleYear}年7月", "field": "month07", "width": 34},
                          {"title": "${settleYear}年8月", "field": "month08", "width": 34},
                          {"title": "${settleYear}年9月", "field": "month09", "width": 34},
                          {"title": "${settleYear}年Q3", "field": "quarter03", "width": 34},
                          {"title": "${settleYear}年10月", "field": "month10", "width": 34},
                          {"title": "${settleYear}年11月", "field": "month11", "width": 34},
                          {"title": "${settleYear}年12月", "field": "month12", "width": 34},
                          {"title": "${settleYear}年Q4", "field": "quarter04", "width": 34}
                        ]},
                        {"type": "paragraph", "text": "1、月度工单准时闭环率累计低于80%的次数达到3次以上；\\n2、月度工单准时闭环率连续2个月低于80%；\\n3、运维商评价结果处于C、D级时，接受工作指导及发函整改；若连续两个月处于D级，贵司有权缩减运维区域或解除合同。", "beginY": 205, "height": 58},
                        {"type": "paragraph", "text": "我司对上述表格及附件内容无任何异议，不会再向贵司提出除此以外的任何款项支付主张或索赔请求。\\n特此确认！", "beginY": 135, "height": 42},
                        {"type": "paragraph", "text": "【${operationCompanyShortName}】公司\\n【${signYear}】年【${signMonth}】月【${signDay}】日", "beginY": 75, "height": 38, "horizontalStyle": "RIGHT"}
                      ]
                    },
                    {
                      "pageCode": "appendixBase",
                      "components": [
                        {"type": "title", "text": "附件：基础运维费", "beginY": 795},
                        {"type": "paragraph", "text": "附件所列的明细，均来自于系统账单数据。", "beginY": 765, "height": 14},
                        {"type": "paragraph", "text": "数据源：取自“运维商预结算”—账单明细——基础运维费", "beginY": 742, "height": 14},
                        {"type": "paragraph", "text": "字段表头设计如下：", "beginY": 718, "height": 14},
                        {"type": "table", "tableCode": "baseMaintenanceBillTable", "dataKey": "baseMaintenanceBillRows", "beginX": 25, "beginY": 705, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "序号", "field": "index", "width": 28},
                          {"title": "账单编号", "field": "billNo", "width": 105},
                          {"title": "电站编号", "field": "stationNo", "width": 75},
                          {"title": "业主姓名", "field": "ownerName", "width": 50},
                          {"title": "定价方式", "field": "priceType", "width": 48},
                          {"title": "运维单价（元/户/年）", "field": "maintenanceUnitPrice", "width": 82},
                          {"title": "金额", "field": "amount", "width": 42},
                          {"title": "账单年月", "field": "billMonth", "width": 55},
                          {"title": "生成时间", "field": "createTime", "width": 60}
                        ]}
                      ]
                    },
                    {
                      "pageCode": "appendixWorkOrder",
                      "components": [
                        {"type": "title", "text": "附件：工单扣罚明细", "beginY": 795},
                        {"type": "subTitle", "text": "工单超时扣款", "beginY": 765},
                        {"type": "paragraph", "text": "数据源：取自“运维商预结算”—账单明细——工单超时费", "beginY": 742, "height": 14},
                        {"type": "table", "tableCode": "workOrderTimeoutTable", "dataKey": "workOrderTimeoutRows", "beginX": 25, "beginY": 715, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "序号", "field": "index", "width": 22},
                          {"title": "账单编号", "field": "billNo", "width": 70},
                          {"title": "电站编号", "field": "stationNo", "width": 55},
                          {"title": "运维单号", "field": "workOrderNo", "width": 62},
                          {"title": "运维大类", "field": "workOrderMajorType", "width": 35},
                          {"title": "运维小类", "field": "workOrderMinorType", "width": 42},
                          {"title": "业主姓名", "field": "ownerName", "width": 35},
                          {"title": "运维商", "field": "operationCompanyName", "width": 50},
                          {"title": "派商时间", "field": "dispatchTime", "width": 55},
                          {"title": "完结时间", "field": "finishTime", "width": 55},
                          {"title": "工单超时(天)", "field": "timeoutDays", "width": 42},
                          {"title": "罚款金额", "field": "penaltyAmount", "width": 42},
                          {"title": "账单年月", "field": "billMonth", "width": 35},
                          {"title": "生成时间", "field": "createTime", "width": 45}
                        ]},
                        {"type": "subTitle", "text": "工单超期扣款", "beginY": 570},
                        {"type": "paragraph", "text": "此部分是由于超过规定时效还未完单产生的扣罚明细。数据源：取自“运维商预结算”—账单明细——超期工单", "beginY": 548, "height": 20},
                        {"type": "table", "tableCode": "overdueWorkOrderTable", "dataKey": "overdueWorkOrderRows", "beginX": 25, "beginY": 520, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "序号", "field": "index", "width": 28},
                          {"title": "账单编号", "field": "billNo", "width": 80},
                          {"title": "电站编号", "field": "stationNo", "width": 65},
                          {"title": "运维单号", "field": "workOrderNo", "width": 75},
                          {"title": "运维大类", "field": "workOrderMajorType", "width": 45},
                          {"title": "运维小类", "field": "workOrderMinorType", "width": 48},
                          {"title": "业主姓名", "field": "ownerName", "width": 45},
                          {"title": "运维商", "field": "operationCompanyName", "width": 65},
                          {"title": "派商时间", "field": "dispatchTime", "width": 70},
                          {"title": "罚款金额", "field": "penaltyAmount", "width": 48},
                          {"title": "账单年月", "field": "billMonth", "width": 45}
                        ]}
                      ]
                    },
                    {
                      "pageCode": "appendixRewardAndBuyback",
                      "components": [
                        {"type": "title", "text": "附件：奖惩与电站回购", "beginY": 795},
                        {"type": "subTitle", "text": "工单及时闭环奖惩", "beginY": 765},
                        {"type": "paragraph", "text": "数据源：取自“运维商预结算”—账单明细——工单及时闭环奖惩", "beginY": 742, "height": 14},
                        {"type": "table", "tableCode": "closeRewardTable", "dataKey": "closeRewardRows", "beginX": 25, "beginY": 715, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "序号", "field": "index", "width": 28},
                          {"title": "账单编号", "field": "billNo", "width": 75},
                          {"title": "运维商", "field": "operationCompanyName", "width": 100},
                          {"title": "月度准时闭环工单数", "field": "monthlyOnTimeCloseCount", "width": 62},
                          {"title": "月度实际应闭环工单数", "field": "monthlyShouldCloseCount", "width": 62},
                          {"title": "工单准时闭环率%", "field": "closeRate", "width": 58},
                          {"title": "奖惩标准（年/户/元）", "field": "rewardStandard", "width": 65},
                          {"title": "奖惩计提（元）", "field": "rewardAmount", "width": 55},
                          {"title": "账单年月", "field": "billMonth", "width": 40}
                        ]},
                        {"type": "subTitle", "text": "电站回购", "beginY": 570},
                        {"type": "paragraph", "text": "数据源：取自“运维商预结算”—账单明细——电站回购", "beginY": 548, "height": 14},
                        {"type": "table", "tableCode": "buybackTable", "dataKey": "buybackRows", "beginX": 25, "beginY": 520, "fontSize": 5, "borderWidth": 0.5, "minRowHeight": 24, "headerRowHeight": 24, "bodyRowHeight": 26, "columns": [
                          {"title": "序号", "field": "index", "width": 35},
                          {"title": "账单编号", "field": "billNo", "width": 110},
                          {"title": "电站编号", "field": "stationNo", "width": 90},
                          {"title": "业主姓名", "field": "ownerName", "width": 70},
                          {"title": "账单年月", "field": "billMonth", "width": 65},
                          {"title": "回购/扣罚金额", "field": "buybackAmount", "width": 80},
                          {"title": "生成时间", "field": "createTime", "width": 95}
                        ]}
                      ]
                    }
                  ]
                }
                """;
    }

    /**
     * @description 根据JSON组件配置构建运维商账单确认书组件
     * @param componentConfig 组件JSON配置
     * @param placeholderData 占位符业务数据
     * @param tableDataMap 表格业务数据集合
     * @return PDF组件
     */
    private XEasyPdfComponent buildOperationConfiguredComponent(JSONObject componentConfig, Map<String, String> placeholderData, Map<String, List<Map<String, String>>> tableDataMap) {
        String type = componentConfig.getString("type");
        if ("title".equals(type)) {
            return buildOperationTitle(replaceOperationPlaceholder(componentConfig.getString("text"), placeholderData), getJsonFloat(componentConfig, "beginY", 795F));
        }
        if ("subTitle".equals(type)) {
            return buildOperationSubTitle(replaceOperationPlaceholder(componentConfig.getString("text"), placeholderData), getJsonFloat(componentConfig, "beginY", 0F));
        }
        if ("paragraph".equals(type)) {
            XEasyPdfText paragraph = buildOperationParagraph(
                    replaceOperationPlaceholder(componentConfig.getString("text"), placeholderData),
                    getJsonFloat(componentConfig, "beginY", 0F),
                    getJsonFloat(componentConfig, "height", 14F)
            );
            if ("RIGHT".equals(componentConfig.getString("horizontalStyle"))) {
                paragraph.setHorizontalStyle(XEasyPdfPositionStyle.RIGHT);
            }
            return paragraph;
        }
        if ("table".equals(type)) {
            return buildOperationConfiguredTable(componentConfig, placeholderData, tableDataMap);
        }
        throw new IllegalArgumentException("不支持的运维商账单确认书组件类型：" + type);
    }

    /**
     * @description 根据JSON表格配置和业务数据构建PDF表格
     * @param tableConfig 表格JSON配置
     * @param placeholderData 占位符业务数据
     * @param tableDataMap 表格业务数据集合
     * @return PDF表格组件
     */
    private XEasyPdfTable buildOperationConfiguredTable(JSONObject tableConfig, Map<String, String> placeholderData, Map<String, List<Map<String, String>>> tableDataMap) {
        JSONArray columns = tableConfig.getJSONArray("columns");
        float fontSize = getJsonFloat(tableConfig, "fontSize", 7F);
        float headerRowHeight = getJsonFloat(tableConfig, "headerRowHeight", 24F);
        float bodyRowHeight = getJsonFloat(tableConfig, "bodyRowHeight", 24F);
        float[] widths = buildOperationConfiguredTableWidths(columns);
        List<XEasyPdfRow> rows = new ArrayList<XEasyPdfRow>();
        rows.add(buildOperationTableRow(widths, buildOperationConfiguredTableHeaders(columns, placeholderData), true, headerRowHeight, fontSize));

        List<Map<String, String>> dataRows = tableDataMap.get(tableConfig.getString("dataKey"));
        if (dataRows != null) {
            for (Map<String, String> dataRow : dataRows) {
                rows.add(buildOperationTableRow(widths, buildOperationConfiguredTableValues(columns, dataRow, placeholderData), false, bodyRowHeight, fontSize));
            }
        }

        return XEasyPdfHandler.Table.build(rows)
                .setPosition(getJsonFloat(tableConfig, "beginX", 25F), getJsonFloat(tableConfig, "beginY", 548F))
                .setFontSize(fontSize)
                .setBorderWidth(getJsonFloat(tableConfig, "borderWidth", 0.5F))
                .setMinRowHeight(getJsonFloat(tableConfig, "minRowHeight", 24F))
                .enableCenterStyle();
    }

    /**
     * @description 根据JSON列配置构建表格列宽数组
     * @param columns 表格列配置
     * @return 列宽数组
     */
    private float[] buildOperationConfiguredTableWidths(JSONArray columns) {
        float[] widths = new float[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            widths[i] = getJsonFloat(columns.getJSONObject(i), "width", 50F);
        }
        return widths;
    }

    /**
     * @description 根据JSON列配置构建表格表头内容
     * @param columns 表格列配置
     * @param placeholderData 占位符业务数据
     * @return 表头内容数组
     */
    private String[] buildOperationConfiguredTableHeaders(JSONArray columns, Map<String, String> placeholderData) {
        String[] headers = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            headers[i] = replaceOperationPlaceholder(columns.getJSONObject(i).getString("title"), placeholderData);
        }
        return headers;
    }

    /**
     * @description 根据JSON列配置和数据行构建表格行内容
     * @param columns 表格列配置
     * @param dataRow 表格业务数据行
     * @param placeholderData 占位符业务数据
     * @return 表格行内容数组
     */
    private String[] buildOperationConfiguredTableValues(JSONArray columns, Map<String, String> dataRow, Map<String, String> placeholderData) {
        String[] values = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            String field = columns.getJSONObject(i).getString("field");
            values[i] = replaceOperationPlaceholder(dataRow.get(field), placeholderData);
        }
        return values;
    }

    /**
     * @description 替换运维商账单确认书JSON模板中的占位符
     * @param text 模板文本
     * @param placeholderData 占位符业务数据
     * @return 替换后的文本
     */
    private String replaceOperationPlaceholder(String text, Map<String, String> placeholderData) {
        if (text == null) {
            return "";
        }
        String result = text;
        for (Map.Entry<String, String> entry : placeholderData.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue() == null ? "" : entry.getValue());
        }
        return result;
    }

    /**
     * @description 获取JSON浮点数字段，字段缺失时返回默认值
     * @param jsonObject JSON对象
     * @param fieldName 字段名称
     * @param defaultValue 默认值
     * @return 浮点数字段值
     */
    private float getJsonFloat(JSONObject jsonObject, String fieldName, float defaultValue) {
        return jsonObject.containsKey(fieldName) ? jsonObject.getFloatValue(fieldName) : defaultValue;
    }

    /**
     * @description 查询运维商账单确认书占位符业务数据
     * @param
     * @return 占位符业务数据
     */
    private Map<String, String> queryOperationBillPlaceholderData() {
        // TODO(Codex): 接入真实运维商账单数据库查询后，删除当前模拟占位符数据。
        Map<String, String> dataMap = new HashMap<String, String>();
        dataMap.put("customerCompanyName", "上海晶坪电力有限公司");
        dataMap.put("agreementNo", "YWDLS-202505090191");
        dataMap.put("contractYear", "2026");
        dataMap.put("settleYear", "2026");
        dataMap.put("settleMonth", "5");
        dataMap.put("settleDay", "31");
        dataMap.put("stationCount", "368");
        dataMap.put("settlementAmount", "3,713.49");
        dataMap.put("policyYear", "2025");
        dataMap.put("operationCompanyShortName", "");
        dataMap.put("signYear", "");
        dataMap.put("signMonth", "");
        dataMap.put("signDay", "");
        return dataMap;
    }

    /**
     * @description 查询运维商账单确认书表格业务数据
     * @param
     * @return 表格业务数据集合
     */
    private Map<String, List<Map<String, String>>> queryOperationBillTableData() {
        // TODO(Codex): 接入真实运维商账单数据库查询后，删除当前模拟表格数据。
        Map<String, List<Map<String, String>>> tableDataMap = new HashMap<String, List<Map<String, String>>>();
        List<Map<String, String>> settlementRows = new ArrayList<Map<String, String>>();
        settlementRows.add(buildOperationDataRow(
                "preSettlementNo", "YYJSN20260602000026",
                "operationCompanyName", "商丘昊能新能源科技有限公司",
                "baseMaintenanceFee", "23,510.4",
                "workOrderDeductionAmount", "-19,796.91",
                "buybackDeductionAmount", "0.00",
                "settlementAmount", "3,713.49"
        ));
        settlementRows.add(buildOperationDataRow(
                "preSettlementNo", "总计",
                "operationCompanyName", "${stationCount}",
                "baseMaintenanceFee", "23,510.4",
                "workOrderDeductionAmount", "-19,796.91",
                "buybackDeductionAmount", "0.00",
                "settlementAmount", "3,713.49"
        ));
        tableDataMap.put("settlementTableRows", settlementRows);

        List<Map<String, String>> closeRateRows = new ArrayList<Map<String, String>>();
        closeRateRows.add(buildOperationDataRow(
                "month01", "",
                "month02", "",
                "month03", "",
                "quarter01", "",
                "month04", "",
                "month05", "51.59%",
                "month06", "",
                "quarter02", "",
                "month07", "",
                "month08", "",
                "month09", "",
                "quarter03", "",
                "month10", "",
                "month11", "",
                "month12", "",
                "quarter04", ""
        ));
        tableDataMap.put("closeRateTableRows", closeRateRows);

        List<Map<String, String>> baseMaintenanceBillRows = new ArrayList<Map<String, String>>();
        baseMaintenanceBillRows.add(buildOperationDataRow(
                "index", "1",
                "billNo", "MTBILL2026060100017640",
                "stationNo", "10202209003043",
                "ownerName", "童钦礼",
                "priceType", "按户",
                "maintenanceUnitPrice", "292",
                "amount", "24.8",
                "billMonth", "202605",
                "createTime", "2026-06-01 15:05:52"
        ));
        baseMaintenanceBillRows.add(buildOperationDataRow(
                "index", "2",
                "billNo", "MTBILL2026060100017639",
                "stationNo", "10202209003042",
                "ownerName", "王玉锋",
                "priceType", "按户",
                "maintenanceUnitPrice", "292",
                "amount", "24.8",
                "billMonth", "202605",
                "createTime", "2026-06-01 15:05:52"
        ));
        baseMaintenanceBillRows.add(buildOperationDataRow(
                "index", "。。。。。。以此类推"
        ));
        tableDataMap.put("baseMaintenanceBillRows", baseMaintenanceBillRows);

        List<Map<String, String>> workOrderTimeoutRows = new ArrayList<Map<String, String>>();
        workOrderTimeoutRows.add(buildOperationDataRow(
                "index", "1",
                "billNo", "MTOTBILL202605319759",
                "stationNo", "10202305019978",
                "workOrderNo", "MT2026052847191",
                "workOrderMajorType", "故障单",
                "workOrderMinorType", "一般故障",
                "ownerName", "代素真",
                "operationCompanyName", "商丘昊能新能",
                "dispatchTime", "2026-05-28 09:25:37",
                "finishTime", "2026-05-31 16:03:54",
                "timeoutDays", "0.34028",
                "penaltyAmount", "-6.81",
                "billMonth", "202605",
                "createTime", "2026-06-01 00:00:00"
        ));
        workOrderTimeoutRows.add(buildOperationDataRow(
                "billNo", "以此类推。。。。。。"
        ));
        tableDataMap.put("workOrderTimeoutRows", workOrderTimeoutRows);

        List<Map<String, String>> overdueWorkOrderRows = new ArrayList<Map<String, String>>();
        overdueWorkOrderRows.add(buildOperationDataRow(
                "index", "1",
                "billNo", "MTOTBILL202605037838",
                "stationNo", "10202311059780",
                "workOrderNo", "MT2026041334865",
                "workOrderMajorType", "故障单",
                "workOrderMinorType", "一般故障",
                "ownerName", "程新景",
                "operationCompanyName", "商丘昊能新能",
                "dispatchTime", "2026-04-13 08:51:34",
                "penaltyAmount", "308.92",
                "billMonth", "202605"
        ));
        overdueWorkOrderRows.add(buildOperationDataRow(
                "billNo", "以此类推。。。。。。"
        ));
        tableDataMap.put("overdueWorkOrderRows", overdueWorkOrderRows);

        List<Map<String, String>> closeRewardRows = new ArrayList<Map<String, String>>();
        closeRewardRows.add(buildOperationDataRow(
                "index", "1",
                "billNo", "BHZD0000000955",
                "operationCompanyName", "商丘昊能新能源科技有限公司",
                "monthlyOnTimeCloseCount", "697",
                "monthlyShouldCloseCount", "1351",
                "closeRate", "51.59",
                "rewardStandard", "0",
                "rewardAmount", "0",
                "billMonth", "202605"
        ));
        closeRewardRows.add(buildOperationDataRow(
                "billNo", "以此类推。。。。。。"
        ));
        tableDataMap.put("closeRewardRows", closeRewardRows);

        List<Map<String, String>> buybackRows = new ArrayList<Map<String, String>>();
        buybackRows.add(buildOperationDataRow(
                "index", "1",
                "billNo", "DZHG20260601000025",
                "stationNo", "10202311051101",
                "ownerName", "赵艳周",
                "billMonth", "202605",
                "buybackAmount", "101,153.81",
                "createTime", "22026-06-01 00:00:00"
        ));
        buybackRows.add(buildOperationDataRow(
                "index", "以此类推。。。。。。"
        ));
        tableDataMap.put("buybackRows", buybackRows);
        return tableDataMap;
    }

    /**
     * @description 构建运维商账单确认书表格模拟数据行
     * @param keyValues 表格字段名和值，按照key、value顺序传入
     * @return 表格数据行
     */
    private Map<String, String> buildOperationDataRow(String... keyValues) {
        Map<String, String> dataRow = new HashMap<String, String>();
        for (int i = 0; i + 1 < keyValues.length; i = i + 2) {
            dataRow.put(keyValues[i], keyValues[i + 1]);
        }
        return dataRow;
    }

    /**
     * @description 构建运维商账单确认书标题文本
     * @param text 标题内容
     * @param beginY Y轴起始坐标
     * @return PDF文本组件
     */
    private XEasyPdfText buildOperationTitle(String text, float beginY) {
        return XEasyPdfHandler.Text.build(18F, text)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD)
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER)
                .setPosition(0F, beginY)
                .setWidth(595F)
                .setHeight(24F);
    }

    /**
     * @description 构建运维商账单确认书小标题文本
     * @param text 小标题内容
     * @param beginY Y轴起始坐标
     * @return PDF文本组件
     */
    private XEasyPdfText buildOperationSubTitle(String text, float beginY) {
        return XEasyPdfHandler.Text.build(11F, text)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD)
                .setPosition(35F, beginY)
                .setWidth(525F)
                .setHeight(18F);
    }

    /**
     * @description 构建运维商账单确认书段落文本
     * @param text 段落内容
     * @param beginY Y轴起始坐标
     * @param height 文本区域高度
     * @return PDF文本组件
     */
    private XEasyPdfText buildOperationParagraph(String text, float beginY, float height) {
        return XEasyPdfHandler.Text.build(10F, text)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL)
                .setLeading(14F)
                .setPosition(35F, beginY)
                .setWidth(525F)
                .setHeight(height);
    }

    /**
     * @description 构建运维商账单确认书表格行
     * @param widths 每列宽度
     * @param values 每列内容
     * @param header 是否为表头
     * @param height 行高
     * @param fontSize 字体大小
     * @return PDF表格行组件
     */
    private XEasyPdfRow buildOperationTableRow(float[] widths, String[] values, boolean header, float height, float fontSize) {
        List<XEasyPdfCell> cells = new ArrayList<XEasyPdfCell>();
        for (int i = 0; i < widths.length; i++) {
            String value = i < values.length ? values[i] : "";
            cells.add(buildOperationTableCell(value, widths[i], header, fontSize));
        }
        XEasyPdfRow row = XEasyPdfHandler.Table.Row.build(cells)
                .setHeight(height)
                .setFontSize(fontSize)
                .setVerticalStyle(XEasyPdfPositionStyle.CENTER)
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER);
        if (header) {
            row.setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD).setBackgroundColor(new Color(235, 239, 245));
        }
        return row;
    }

    /**
     * @description 构建运维商账单确认书表格单元格
     * @param content 单元格内容
     * @param width 单元格宽度
     * @param header 是否为表头
     * @param fontSize 字体大小
     * @return PDF单元格组件
     */
    private XEasyPdfCell buildOperationTableCell(String content, float width, boolean header, float fontSize) {
        String cellContent = content == null ? "" : content;
        XEasyPdfText text = XEasyPdfHandler.Text.build(fontSize, cellContent)
                .setNewLine(false)
                .setDefaultFontStyle(header ? XEasyPdfDefaultFontStyle.BOLD : XEasyPdfDefaultFontStyle.NORMAL)
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER)
                .setVerticalStyle(XEasyPdfPositionStyle.CENTER);
        XEasyPdfCell cell = XEasyPdfHandler.Table.Row.Cell.build(width)
                .addContent(text)
                .setFontSize(fontSize)
                .setBorderWidth(0.5F)
                .setBorderColor(new Color(150, 150, 150))
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER)
                .setVerticalStyle(XEasyPdfPositionStyle.CENTER)
                .enableBorder()
                .enableAutoScaleFontSize();
        if (header) {
            cell.setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD).setBackgroundColor(new Color(235, 239, 245));
        }
        return cell;
    }

    /**
     * @description 构建图书清单PDF页面
     * @param books 图书数据列表
     * @return PDF页面对象
     */
    private XEasyPdfPage buildBookListPage(List<Book> books) {
        List<XEasyPdfComponent> components = new ArrayList<XEasyPdfComponent>();
        components.add(buildTitleText());
        components.add(buildExportInfoText(books));
        components.add(buildBookTable(books));
        return XEasyPdfHandler.Page.build(XEasyPdfHandler.Page.Rectangle.A4, components);
    }

    /**
     * @description 构建PDF标题文本
     * @param
     * @return PDF文本组件
     */
    private XEasyPdfText buildTitleText() {
        return XEasyPdfHandler.Text.build(TITLE_FONT_SIZE, "图书清单")
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD)
                .setFontColor(new Color(30, 30, 30))
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER)
                .setPosition(0F, 795F)
                .setWidth(595F)
                .setHeight(30F);
    }

    /**
     * @description 构建PDF导出信息文本
     * @param books 图书数据列表
     * @return PDF文本组件
     */
    private XEasyPdfText buildExportInfoText(List<Book> books) {
        int count = books == null ? 0 : books.size();
        String exportTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String content = "导出时间：" + exportTime + "    图书数量：" + count;
        return XEasyPdfHandler.Text.build(NORMAL_FONT_SIZE, content)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL)
                .setFontColor(new Color(80, 80, 80))
                .setPosition(35F, 765F)
                .setWidth(525F)
                .setHeight(20F);
    }

    /**
     * @description 构建图书列表表格
     * @param books 图书数据列表
     * @return PDF表格组件
     */
    private XEasyPdfTable buildBookTable(List<Book> books) {
        List<XEasyPdfRow> rows = new ArrayList<XEasyPdfRow>();
        rows.add(buildHeaderRow());
        if (books == null || books.isEmpty()) {
            rows.add(buildEmptyRow());
        } else {
            for (Book book : books) {
                rows.add(buildBookRow(book));
            }
        }
        return XEasyPdfHandler.Table.build(rows)
                .setPosition(TABLE_BEGIN_X, TABLE_BEGIN_Y)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL)
                .setFontSize(TABLE_FONT_SIZE)
                .setBorderWidth(0.5F)
                .setBorderColor(new Color(150, 150, 150))
                .setMinRowHeight(TABLE_MIN_ROW_HEIGHT)
                .enableCenterStyle();
    }

    /**
     * @description 构建表格表头行
     * @param
     * @return PDF表格行组件
     */
    private XEasyPdfRow buildHeaderRow() {
        return XEasyPdfHandler.Table.Row.build(
                buildHeaderCell("ISBN", 85F),
                buildHeaderCell("书名", 105F),
                buildHeaderCell("作者", 70F),
                buildHeaderCell("出版社", 95F),
                buildHeaderCell("状态", 55F),
                buildHeaderCell("备注", 115F)
        ).setHeight(TABLE_MIN_ROW_HEIGHT)
                .setFontSize(TABLE_FONT_SIZE)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD)
                .setBackgroundColor(new Color(235, 239, 245))
                .enableCenterStyle();
    }

    /**
     * @description 构建空数据提示行
     * @param
     * @return PDF表格行组件
     */
    private XEasyPdfRow buildEmptyRow() {
        return XEasyPdfHandler.Table.Row.build(
                buildBodyCell("暂无图书数据", 525F)
        ).setHeight(TABLE_MIN_ROW_HEIGHT)
                .setFontSize(TABLE_FONT_SIZE)
                .enableCenterStyle();
    }

    /**
     * @description 构建图书数据行
     * @param book 图书数据
     * @return PDF表格行组件
     */
    private XEasyPdfRow buildBookRow(Book book) {
        return XEasyPdfHandler.Table.Row.build(
                buildBodyCell(emptyToDefault(book.getIsbn()), 85F),
                buildBodyCell(emptyToDefault(book.getName()), 105F),
                buildBodyCell(emptyToDefault(book.getAuthor()), 70F),
                buildBodyCell(emptyToDefault(book.getPublisher()), 95F),
                buildBodyCell(formatStatus(book.getStatus()), 55F),
                buildBodyCell(emptyToDefault(book.getRemark()), 115F)
        ).setMinHeight(TABLE_MIN_ROW_HEIGHT)
                .setFontSize(TABLE_FONT_SIZE)
                .setVerticalStyle(XEasyPdfPositionStyle.CENTER);
    }

    /**
     * @description 构建表头单元格
     * @param content 单元格内容
     * @param width 单元格宽度
     * @return PDF单元格组件
     */
    private XEasyPdfCell buildHeaderCell(String content, float width) {
        return buildCell(content, width)
                .setDefaultFontStyle(XEasyPdfDefaultFontStyle.BOLD)
                .setBackgroundColor(new Color(235, 239, 245))
                .enableCenterStyle();
    }

    /**
     * @description 构建表体单元格
     * @param content 单元格内容
     * @param width 单元格宽度
     * @return PDF单元格组件
     */
    private XEasyPdfCell buildBodyCell(String content, float width) {
        return buildCell(content, width)
                .setHorizontalStyle(XEasyPdfPositionStyle.CENTER)
                .setVerticalStyle(XEasyPdfPositionStyle.CENTER);
    }

    /**
     * @description 构建通用单元格
     * @param content 单元格内容
     * @param width 单元格宽度
     * @return PDF单元格组件
     */
    private XEasyPdfCell buildCell(String content, float width) {
        return XEasyPdfHandler.Table.Row.Cell.build(width)
                .addContent(XEasyPdfHandler.Text.build(TABLE_FONT_SIZE, content)
                        .setNewLine(false)
                        .setDefaultFontStyle(XEasyPdfDefaultFontStyle.NORMAL))
                .setFontSize(TABLE_FONT_SIZE)
                .setBorderWidth(0.5F)
                .setBorderColor(new Color(150, 150, 150))
                .enableBorder()
                .enableAutoScaleFontSize();
    }

    /**
     * @description 将图书状态转换为PDF展示文本
     * @param status 图书状态值
     * @return 状态展示文本
     */
    private String formatStatus(Integer status) {
        if (status == null) {
            return "-";
        }
        if (Integer.valueOf(0).equals(status)) {
            return "下架";
        }
        if (Integer.valueOf(1).equals(status)) {
            return "在库";
        }
        if (Integer.valueOf(2).equals(status)) {
            return "借出";
        }
        if (Integer.valueOf(3).equals(status)) {
            return "删除";
        }
        return "未知";
    }

    /**
     * @description 将空文本转换为默认展示值
     * @param value 原始文本
     * @return 处理后的展示文本
     */
    private String emptyToDefault(String value) {
        if (value == null || value.trim().length() == 0) {
            return "-";
        }
        return value;
    }

    /**
     * @description 关闭PDF文档对象
     * @param document PDF文档对象
     * @return void
     */
    private void closeDocument(XEasyPdfDocument document) {
        if (document == null) {
            return;
        }
        try {
            document.close();
        } catch (Exception ignored) {
            // 关闭失败不影响主流程结果，生成失败会在主流程中抛出异常。
        }
    }
}
