package com.example.demo.service.decorator;

/**
 * 日志装饰器，在消息发送前后输出日志记录。
 */
public class LoggingDecorator extends MessageDecorator {

    public LoggingDecorator(MessageService wrapped) {
        super(wrapped);
    }

    /**
     * 在发送前后追加日志，然后委托被装饰的服务完成实际发送。
     *
     * @param content 消息内容
     * @return 记录日志后的发送结果
     */
    @Override
    public String send(String content) {
        System.out.println("[LOG] Before sending: " + content);
        String result = super.send(content);
        System.out.println("[LOG] After sending: " + result);
        return result;
    }
}
