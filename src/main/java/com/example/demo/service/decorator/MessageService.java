package com.example.demo.service.decorator;

/**
 * 消息处理组件接口，定义统一的发送消息能力。
 */
public interface MessageService {

    /**
     * 发送消息并返回处理结果描述。
     *
     * @param content 消息内容
     * @return 处理后的结果
     */
    String send(String content);
}
