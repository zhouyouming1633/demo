package com.example.demo.service.decorator;

import java.util.Base64;

/**
 * 加密装饰器，在发送前对消息内容进行 Base64 编码。
 */
public class EncryptionDecorator extends MessageDecorator {

    public EncryptionDecorator(MessageService wrapped) {
        super(wrapped);
    }

    /**
     * 先对内容进行 Base64 加密，再委托给被装饰的服务发送。
     *
     * @param content 明文消息内容
     * @return 加密后的发送结果
     */
    @Override
    public String send(String content) {
        String encrypted = Base64.getEncoder().encodeToString(content.getBytes());
        return super.send(encrypted);
    }
}
