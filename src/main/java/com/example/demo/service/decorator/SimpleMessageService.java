package com.example.demo.service.decorator;

/**
 * 基础消息服务，实现最朴素的消息发送逻辑。
 */
public class SimpleMessageService implements MessageService {

    @Override
    public String send(String content) {
        return "Sent: " + content;
    }
}
