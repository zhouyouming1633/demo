package com.example.demo.service.factory;

import java.math.BigDecimal;

/**
 * Alipay payment channel implementation.
 */
public class AlipayPaymentChannel implements PaymentChannel {

    /**
     * Completes payment through Alipay and returns the operation result.
     *
     * @param orderNo business order number
     * @param amount payment amount
     * @return payment result description
     */
    @Override
    public String pay(String orderNo, BigDecimal amount) {
        return "Alipay paid order " + orderNo + " with amount " + amount;
    }
}
