package com.app.carpolling.controller;

import com.app.carpolling.dto.ApiResponse;
import com.app.carpolling.dto.PaymentCallbackRequest;
import com.app.carpolling.dto.PaymentOrderResponse;
import com.app.carpolling.dto.PaymentRequest;
import com.app.carpolling.entity.Payment;
import com.app.carpolling.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * Creates a Razorpay order for the given booking
     * @param request PaymentRequest containing booking ID
     * @return Razorpay order response as JSON string
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
        @Valid @RequestBody PaymentRequest request
    ) {
        try {
            PaymentOrderResponse orderResponse = paymentService.createOrder(request);
            return ResponseEntity.ok(
                ApiResponse.success("Razorpay order created successfully", orderResponse)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Payment callback endpoint to verify Razorpay payment
     * @param callbackRequest PaymentCallbackRequest containing Razorpay payment details
     * @return Updated Payment entity
     */
    @PostMapping("/payment-callback")
    public ResponseEntity<ApiResponse<Payment>> paymentCallback(
        @Valid @RequestBody PaymentCallbackRequest callbackRequest
    ) {
        try {
            Payment payment = paymentService.verifyPayment(callbackRequest);
            if (payment.getStatus().toString().equals("SUCCESS")) {
                return ResponseEntity.ok(
                    ApiResponse.success("Payment verified successfully", payment)
                );
            } else {
                return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(ApiResponse.error("Payment verification failed: " + payment.getFailureReason()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByBookingId(
        @PathVariable Long bookingId
    ) {
        try {
            Payment payment = paymentService.getPaymentByBookingId(bookingId);
            return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", payment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentByTransactionId(
        @PathVariable String transactionId
    ) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", payment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentById(
        @PathVariable Long paymentId
    ) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(
                ApiResponse.success("Payment retrieved successfully", payment)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}









