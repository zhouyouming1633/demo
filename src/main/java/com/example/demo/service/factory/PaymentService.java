package com.example.demo.service.factory;

import java.math.BigDecimal;

/**
 * Client service that demonstrates how business code uses a factory.
 */
public class PaymentService {

    /**
     * Payment channel factory used to hide concrete implementation creation.
     */
    private final PaymentChannelFactory paymentChannelFactory;

    /**
     * Creates a payment service with the specified factory.
     *
     * @param paymentChannelFactory factory used to create payment channels
     */
    public PaymentService(PaymentChannelFactory paymentChannelFactory) {
        this.paymentChannelFactory = paymentChannelFactory;
    }

    /**
     * Pays an order by selecting a payment channel through the factory.
     *
     * @param orderNo business order number
     * @param amount payment amount
     * @param paymentType payment type selected by the user
     * @return payment result description
     */
    public String payOrder(String orderNo, BigDecimal amount, PaymentType paymentType) {
        PaymentChannel paymentChannel = paymentChannelFactory.create(paymentType);
        return paymentChannel.pay(orderNo, amount);
    }
}
