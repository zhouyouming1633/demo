package com.example.demo.service.decorator;

/**
 * 签名装饰器，在消息尾部追加数字签名标记。
 */
public class SignatureDecorator extends MessageDecorator {

    private final String signer;

    public SignatureDecorator(MessageService wrapped, String signer) {
        super(wrapped);
        this.signer = signer;
    }

    /**
     * 在消息内容后附加签名信息，再委托被装饰的服务发送。
     *
     * @param content 原始消息内容
     * @return 带签名的发送结果
     */
    @Override
    public String send(String content) {
        return super.send(content + " --signed by " + signer);
    }
}
