package com.example.demo.tes;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * 第三方回调签名生成工具:越秀工单
 * 用于生成 appId、timestamp、sign 请求头参数，通过网关 thirdCallBackAuthCheck 校验
 *
 * @author Youming.Zhou
 * @date 2026-06-29
 */
public class ThirdCallbackSignBuilder {


    private String appId = "yxjnvExQ3DRaiVzXGr";

    private String appSecret = "PrDOBxpJidgDzouh7dM7VmGPAlXyZvAW";

    /**
     * 生成签名参数
     *
     * @return 包含 appId、timestamp、sign 的签名对象
     */
    public SignParams buildSignParams() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String content = appId + "&" + timestamp;

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        String sign = Base64.encodeBase64String(encrypted);

        return new SignParams(appId, timestamp, sign);
    }

    /**
     * 签名参数
     */
    public static class SignParams {
        /** 应用ID */
        private String appId;
        /** 毫秒时间戳，2分钟内有效 */
        private String timestamp;
        /** AES加密后的Base64签名 */
        private String sign;

        public SignParams(String appId, String timestamp, String sign) {
            this.appId = appId;
            this.timestamp = timestamp;
            this.sign = sign;
        }

        public String getAppId() {
            return appId;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getSign() {
            return sign;
        }
    }

    public static void main(String[] args) {
        try {
            SignParams signParams = new ThirdCallbackSignBuilder().buildSignParams();
            System.out.println(signParams.getAppId());
            System.out.println(signParams.getTimestamp());
            System.out.println(signParams.getSign());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}