package com.ecommerce.service;

import com.ecommerce.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends order emails when app.mail.enabled=true and spring.mail.* is configured. Never breaks checkout. */
@Service
@Slf4j
public class EmailService {
    private final ObjectProvider<JavaMailSender> mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${spring.mail.username:noreply@shopease.local}")
    private String from;

    public EmailService(ObjectProvider<JavaMailSender> mailSender) { this.mailSender = mailSender; }

    public void sendOrderConfirmation(Order order) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!enabled || sender == null) return;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(order.getUser().getEmail());
            msg.setSubject("ShopEase order #" + order.getId() + " confirmed");
            msg.setText("Hi " + order.getUser().getFirstName() + ",\n\nThanks for your order #" + order.getId()
                    + ". Total: Rs. " + order.getTotalAmount() + ".\nStatus: " + order.getStatus()
                    + "\nShipping to: " + order.getShippingAddress() + "\n\nShopEase");
            sender.send(msg);
        } catch (Exception e) {
            log.warn("Could not send order email: {}", e.getMessage());
        }
    }
}
