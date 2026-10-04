package com.ecommerce.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Payment simulation (stands in for Razorpay / Stripe).
 * To go live, replace charge() with a call to the gateway SDK and verify the signature on the callback.
 */
@Service
public class PaymentService {

    /** @return true when the payment is captured now; false for cash on delivery (paid later). */
    public boolean charge(String method, BigDecimal amount) {
        return !"COD".equalsIgnoreCase(method);
    }
}
