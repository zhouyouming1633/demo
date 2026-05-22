package com.example.demo.service.factory;

import java.math.BigDecimal;

/**
 * Payment channel abstraction used by the factory pattern example.
 */
public interface PaymentChannel {

    /**
     * Pays the specified amount through the current payment channel.
     *
     * @param orderNo business order number
     * @param amount payment amount
     * @return payment result description
     */
    String pay(String orderNo, BigDecimal amount);
}
