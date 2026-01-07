package com.app.carpolling.service;

import com.app.carpolling.dto.PaymentRequest;
import com.app.carpolling.entity.Booking;
import com.app.carpolling.entity.BookingStatus;
import com.app.carpolling.entity.Payment;
import com.app.carpolling.entity.PaymentStatus;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private static final String KEY_ID = "rzp_test_thbnstH0Bq80hy"; // Replace with your Key ID
    private static final String KEY_SECRET = "oc86adrgm685ECs3Wzdk0nOb"; // Replace with your Key Secret


    @Transactional
    public Payment processPayment(PaymentRequest request) throws RazorpayException {
        // Get booking
        Booking booking = bookingService.getBookingById(request.getBookingId());
        
        // Validate booking is pending
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BaseException(ErrorCode.BOOKING_NOT_PENDING);
        }
        
        // Check if payment already exists
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BaseException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        RazorpayClient razorpay = new RazorpayClient(KEY_ID, KEY_SECRET);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", booking.getTotalAmount() * 100);
        orderRequest.put("currency", "INR");
        razorpay.orders.create(orderRequest);
        
        // Create payment
        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(PaymentStatus.INITIATED);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Simulate payment processing
        // In real application, integrate with payment gateway
        boolean paymentSuccess = processPaymentGateway(request);
        
        if (paymentSuccess) {
            savedPayment.setStatus(PaymentStatus.SUCCESS);
            savedPayment.setPaidAt(LocalDateTime.now());
            savedPayment.setPaymentGatewayResponse("Payment successful");
            
            // Confirm booking
            bookingService.confirmBooking(booking.getId());
        } else {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setFailureReason("Payment gateway error");
            
            // Release seats and cancel booking on payment failure
            bookingService.cancelBooking(booking.getId());
        }
        
        return paymentRepository.save(savedPayment);
    }
    
    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN" + timestamp + uuid;
    }
    
    private boolean processPaymentGateway(PaymentRequest request) {
        // Simulate payment gateway processing
        // In real application, call actual payment gateway API
        try {
            Thread.sleep(1000); // Simulate network delay
            return true; // Simulate successful payment
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    @Transactional(readOnly = true)
    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new BaseException(ErrorCode.PAYMENT_NOT_FOUND, "Payment not found for this booking"));
    }
    
    @Transactional(readOnly = true)
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new BaseException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}









