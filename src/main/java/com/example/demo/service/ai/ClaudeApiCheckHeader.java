package com.example.demo.service.ai;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ClaudeApiCheckHeader {
    // 中转API地址（请替换成实际的地址）
    private static final String API_URL = "https://api.n1n.ai/v1/chat/completions";

    // 你的API密钥
    private static final String API_KEY = "sk-iynFx0q9gWfclk5IKs8IzYvuRkxU2RkTGyWkIyu5F6lP1u9z";

    public static void main(String[] args) {
        callClaudeAPI();
    }

    public static void callClaudeAPI() {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法
            connection.setRequestMethod("POST");

            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Accept", "application/json");

            // 启用输入输出流
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // 构建请求体
            String requestBody = buildRequestBody();

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // ========== 关键部分：打印所有响应头 ==========
            System.out.println("========== 响应头信息 ==========");
            Map<String, List<String>> headers = connection.getHeaderFields();

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String headerName = entry.getKey();
                List<String> headerValues = entry.getValue();

                // headerName 可能为 null（对应HTTP状态行）
                if (headerName == null) {
                    System.out.println("Status Line: " + String.join(", ", headerValues));
                } else {
                    System.out.println(headerName + ": " + String.join(", ", headerValues));
                }
            }
            System.out.println("================================\n");

            // 获取响应状态码
            int statusCode = connection.getResponseCode();
            System.out.println("响应状态码: " + statusCode);

            // 读取响应内容
            String responseBody = readResponse(connection, statusCode);
            System.out.println("\n========== 响应内容 ==========");
            System.out.println(responseBody);



            System.out.println("========== 完整响应头 **  DOUBAO *** ==========");
            // 遍历打印所有响应头
            connection.getHeaderFields().forEach((key, value) -> {
                System.out.println(key + " : " + value);
            });

            // 重点看这几个关键字段
            System.out.println("========== 关键核验字段 ==========");
            System.out.println("x-model：" + connection.getHeaderField("x-model"));
            System.out.println("x-anthropic-version：" + connection.getHeaderField("x-anthropic-version"));
            System.out.println("anthropic-ratelimit-input-tokens：" + connection.getHeaderField("anthropic-ratelimit-input-tokens"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 构建请求体（标准OpenAI兼容格式）
     */
    private static String buildRequestBody() {
        return "{"
                + "\"model\": \"claude-opus-4-7\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"Hello, say 'hi' only\""
                + "    }"
                + "],"
                + "\"max_tokens\": 50,"
                + "\"temperature\": 0.7"
                + "}";
    }

    /**
     * 读取响应内容
     */
    private static String readResponse(HttpURLConnection connection, int statusCode) throws IOException {
        InputStream inputStream;

        // 根据状态码选择正确的输入流
        if (statusCode >= 200 && statusCode < 300) {
            inputStream = connection.getInputStream();
        } else {
            inputStream = connection.getErrorStream();
        }

        if (inputStream == null) {
            return "No response body";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
}