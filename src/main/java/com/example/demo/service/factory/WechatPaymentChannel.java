package com.example.demo.service.factory;

import java.math.BigDecimal;

/**
 * WeChat payment channel implementation.
 */
public class WechatPaymentChannel implements PaymentChannel {

    /**
     * Completes payment through WeChat Pay and returns the operation result.
     *
     * @param orderNo business order number
     * @param amount payment amount
     * @return payment result description
     */
    @Override
    public String pay(String orderNo, BigDecimal amount) {
        return "WeChat Pay paid order " + orderNo + " with amount " + amount;
    }
}
