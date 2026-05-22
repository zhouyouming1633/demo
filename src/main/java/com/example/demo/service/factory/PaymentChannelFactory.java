package com.example.demo.service.factory;

/**
 * Simple factory for creating payment channel instances by payment type.
 */
public class PaymentChannelFactory {

    /**
     * Creates the matching payment channel according to the specified payment type.
     *
     * @param paymentType payment type selected by business code
     * @return matching payment channel implementation
     */
    public PaymentChannel create(PaymentType paymentType) {
        if (paymentType == null) {
            throw new IllegalArgumentException("Payment type must not be null.");
        }

        switch (paymentType) {
            case ALIPAY:
                return new AlipayPaymentChannel();
            case WECHAT:
                return new WechatPaymentChannel();
            default:
                throw new IllegalArgumentException("Unsupported payment type: " + paymentType);
        }
    }
}
