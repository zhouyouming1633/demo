package com.example.demo.service.decorator;

/**
 * 抽象装饰器，持有一个被包裹的 MessageService 并将调用委托给它。
 * 子类可以在此之上叠加额外行为，形成装饰链。
 */
public abstract class MessageDecorator implements MessageService {

    /**
     * 被装饰的消息服务实例，由子类通过构造方法注入。
     */
    protected final MessageService wrapped;

    /**
     * 构造装饰器并注入被包裹的服务。
     *
     * @param wrapped 被装饰的消息服务
     */
    public MessageDecorator(MessageService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String send(String content) {
        return wrapped.send(content);
    }
}
