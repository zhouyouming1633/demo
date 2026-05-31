package com.example.demo.service.decorator;

/**
 * 客户端示例，展示如何通过装饰器链组合多种行为。
 */
public class MessageClient {

    /**
     * 演示装饰器模式：将基础消息服务逐层装饰为 "日志 + 签名 + 加密" 的组合。
     */
    public void demo() {
        // 最基础的消息发送能力
        MessageService service = new SimpleMessageService();

        // 动态叠加装饰器——顺序决定行为
        MessageService decorated = new LoggingDecorator(
                new SignatureDecorator(
                        new EncryptionDecorator(service),
                        "Alice"
                )
        );

        String result = decorated.send("Hello Decorator Pattern");
        System.out.println("Final: " + result);
    }

    public static void main(String[] args) {
        new MessageClient().demo();
    }
}
