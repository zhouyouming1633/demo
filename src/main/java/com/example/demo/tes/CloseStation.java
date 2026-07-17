package com.example.demo.tes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 关站服务，按工单ID逐条调用接口请求参考中的关站接口。
 *
 * @author ZhouYouMing
 * @date 2026/7/1 14:43
 */
public class CloseStation {

    /** 接口请求参考中的关站接口地址 */
    private static final String CLOSE_STATION_URL = "https://hy.jinkopower.com/workOrderReplenishingManage/justNotifyZhongXinCloseStation";

    /** 接口请求参考中的Cookie请求头 */
    private static final String COOKIE = "PLAY_SESSION=\"58083e86e75d781d9426625ded9ad329b940a133-___ID=4b49ac58-722b-4df2-a24b-fd2a2ccc5d60\"; manageutoken=0EF14F72376E3E5032D7C281FC66678A-4AA360; SERVERID=059cbd2f91c0c825bdaf23155053df1f|1783483284|1783483172";

    /**
     * 程序入口，用于本地直接执行关站接口调用。
     *
     * @param args 命令行参数，当前未使用
     */
    public static void main(String[] args) {
        String wrid = "38004,38023,37156,37161,37162,37169,37197,26375,37254,34280,28281,37842,37310,21358,18123,21367,37371,37817,37821,26066,31639";
        List<String> workOrderIds = Arrays.asList(wrid.split(","));
        new CloseStation().closeStation(workOrderIds);
    }

    /**
     * 根据工单ID列表逐条调用关站接口。
     *
     * @param workOrderIds 工单ID列表，列表为空时不发起接口请求
     */
    public void closeStation(List<String> workOrderIds) {
        if (workOrderIds == null || workOrderIds.isEmpty()) {
            System.out.println("关站工单ID列表为空，无需调用接口");
            return;
        }

        RestTemplate restTemplate = new RestTemplate();
        for (String workOrderId : workOrderIds) {
            if (workOrderId == null || workOrderId.trim().isEmpty()) {
                System.out.println("关站工单ID为空，已跳过");
                continue;
            }

            // 每次请求都等待接口返回后再进入下一条，避免并发调用影响关站顺序。
            callCloseStation(restTemplate, workOrderId.trim());
        }
    }

    /**
     * 调用单个工单ID的关站接口。
     *
     * @param restTemplate HTTP请求客户端
     * @param workOrderId  工单ID
     */
    private void callCloseStation(RestTemplate restTemplate, String workOrderId) {
        try {
            String requestUrl = buildRequestUrl(workOrderId);
            HttpEntity<String> requestEntity = buildRequestEntity();
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    requestUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                handleSuccessHttpResponse(workOrderId, responseEntity.getBody());
                return;
            }

            System.out.println("关站接口调用失败，workorderid=" + workOrderId
                    + "，HTTP状态码=" + responseEntity.getStatusCode()
                    + "，响应内容=" + responseEntity.getBody());
        } catch (HttpStatusCodeException e) {
            System.out.println("关站接口调用异常，workorderid=" + workOrderId
                    + "，HTTP状态码=" + e.getStatusCode()
                    + "，错误内容=" + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("关站接口调用异常，workorderid=" + workOrderId + "，错误信息=" + e.getMessage());
        }
    }

    /**
     * 构建关站接口请求地址。
     *
     * @param workOrderId 工单ID
     * @return 包含workorderid查询参数的请求地址
     */
    private String buildRequestUrl(String workOrderId) {
        String encodedWorkOrderId = URLEncoder.encode(workOrderId, StandardCharsets.UTF_8);
        return CLOSE_STATION_URL + "?workorderid=" + encodedWorkOrderId;
    }

    /**
     * 构建关站接口请求实体。
     *
     * @return 包含接口请求参考中请求头的HTTP请求实体
     */
    private HttpEntity<String> buildRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("priority", "u=1, i");
        headers.set("x-requested-with", "XMLHttpRequest");
        headers.set("Cookie", COOKIE);
        headers.set("User-Agent", "Apifox/1.0.0 (https://apifox.com)");
        headers.setContentType(new MediaType(MediaType.APPLICATION_FORM_URLENCODED, StandardCharsets.UTF_8));
        headers.set("Accept", "*/*");
        headers.set("Host", "hy.jinkopower.com");
        headers.set("Connection", "keep-alive");

        return new HttpEntity<>(headers);
    }

    /**
     * 处理HTTP成功返回后的业务结果。
     *
     * @param workOrderId  工单ID
     * @param responseBody 接口响应内容
     */
    private void handleSuccessHttpResponse(String workOrderId, String responseBody) {
        try {
            CloseStationResponse response = JSON.parseObject(responseBody, CloseStationResponse.class);
            if (response != null && Integer.valueOf(1).equals(response.getState())) {
                System.out.println("关站接口调用成功，workorderid=" + workOrderId
                        + "，state=" + response.getState()
                        + "，msg=" + response.getMsg()
                        + "，uid=" + response.getUid());
                return;
            }

            System.out.println("关站接口调用失败，workorderid=" + workOrderId
                    + "，state=" + (response == null ? null : response.getState())
                    + "，msg=" + (response == null ? null : response.getMsg())
                    + "，uid=" + (response == null ? null : response.getUid())
                    + "，响应内容=" + responseBody);
        } catch (JSONException e) {
            System.out.println("关站接口响应解析失败，workorderid=" + workOrderId
                    + "，错误信息=" + e.getMessage()
                    + "，响应内容=" + responseBody);
        }
    }

    /**
     * 关站接口响应结果。
     */
    private static class CloseStationResponse {

        /** 业务状态，1表示成功，其他值表示失败 */
        private Integer state = 0;

        /** 接口返回消息 */
        private Object msg;

        /** 接口返回唯一标识 */
        private String uid;

        /**
         * 获取业务状态。
         *
         * @return 业务状态
         */
        public Integer getState() {
            return state;
        }

        /**
         * 设置业务状态。
         *
         * @param state 业务状态
         */
        public void setState(Integer state) {
            this.state = state;
        }

        /**
         * 获取接口返回消息。
         *
         * @return 接口返回消息
         */
        public Object getMsg() {
            return msg;
        }

        /**
         * 设置接口返回消息。
         *
         * @param msg 接口返回消息
         */
        public void setMsg(Object msg) {
            this.msg = msg;
        }

        /**
         * 获取接口返回唯一标识。
         *
         * @return 接口返回唯一标识
         */
        public String getUid() {
            return uid;
        }

        /**
         * 设置接口返回唯一标识。
         *
         * @param uid 接口返回唯一标识
         */
        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}
