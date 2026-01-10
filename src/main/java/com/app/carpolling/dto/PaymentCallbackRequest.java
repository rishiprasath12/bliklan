package com.app.carpolling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {
    
    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;
    
    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;
    
    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;
    
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
}
