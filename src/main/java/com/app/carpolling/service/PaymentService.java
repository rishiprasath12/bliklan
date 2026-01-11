package com.app.carpolling.service;

import com.app.carpolling.dto.PaymentCallbackRequest;
import com.app.carpolling.dto.PaymentOrderResponse;
import com.app.carpolling.dto.PaymentRequest;
import com.app.carpolling.entity.Booking;
import com.app.carpolling.entity.BookingStatus;
import com.app.carpolling.entity.Payment;
import com.app.carpolling.entity.PaymentStatus;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private static final String KEY_ID = "rzp_test_S2Ff0VPxryvVep";
    private static final String KEY_SECRET = "weJQH3VKMciZ9WNn4zW1o0hm";

    /**
     * Creates a Razorpay order and saves the payment details with PENDING status
     * @param request PaymentRequest containing booking ID
     * @return Razorpay order response as JSON string
     */
    @Transactional
    public PaymentOrderResponse createOrder(PaymentRequest request) throws RazorpayException {
        // Get booking
        Booking booking = bookingService.getBookingById(request.getBookingId());
        
        // Validate booking is pending
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BaseException(ErrorCode.BOOKING_NOT_PENDING);
        }
        
        // Check if payment already exists for this booking
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BaseException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // Create Razorpay order
        RazorpayClient razorpay = new RazorpayClient(KEY_ID, KEY_SECRET);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (booking.getTotalAmount() * 100)); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", generateTransactionId());
        
        Order order = razorpay.orders.create(orderRequest);
        
        // Create and save payment record with PENDING status
        Payment payment = new Payment();
        payment.setTransactionId(order.get("receipt"));
        payment.setRazorpayOrderId(order.get("id"));
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentGatewayResponse(order.toString());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.get("id"));
        orderMap.put("entity", order.get("entity"));
        orderMap.put("amount", order.get("amount"));
        orderMap.put("amount_paid", order.get("amount_paid"));
        orderMap.put("amount_due", order.get("amount_due"));
        orderMap.put("currency", order.get("currency"));
        orderMap.put("receipt", order.get("receipt"));
        orderMap.put("status", order.get("status"));
        orderMap.put("created_at", order.get("created_at"));
        
        return PaymentOrderResponse.builder()
                .paymentId(savedPayment.getId())
                .order(orderMap)
                .publicKey(KEY_ID)
                .build();
    }

    /**
     * Verifies the Razorpay payment callback and updates the payment status
     * @param callbackRequest PaymentCallbackRequest containing Razorpay details
     * @return Updated Payment entity
     */
    @Transactional
    public Payment verifyPayment(PaymentCallbackRequest callbackRequest) throws RazorpayException {
        // Find payment by ID
        Payment payment = paymentRepository.findById(callbackRequest.getPaymentId())
                .orElseThrow(() -> new BaseException(ErrorCode.PAYMENT_NOT_FOUND));
        
        // Verify that the razorpay order ID matches
        if (!payment.getRazorpayOrderId().equals(callbackRequest.getRazorpayOrderId())) {
            throw new BaseException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "Order ID mismatch");
        }
        
        // Verify the Razorpay signature
        String signature = callbackRequest.getRazorpayOrderId() + "|" + callbackRequest.getRazorpayPaymentId();
        boolean isValid = Utils.verifySignature(signature, callbackRequest.getRazorpaySignature(), KEY_SECRET);
        
        if (isValid) {
            // Payment verified successfully
            payment.setRazorpayPaymentId(callbackRequest.getRazorpayPaymentId());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(LocalDateTime.now());
            payment.setPaymentGatewayResponse(
                    payment.getPaymentGatewayResponse() + 
                    "\n--- Verification Response ---\n" +
                    "Payment ID: " + callbackRequest.getRazorpayPaymentId() + 
                    ", Verified: true"
            );
            
            // Confirm the booking
            bookingService.confirmBooking(payment.getBooking().getId());
        } else {
            // Payment verification failed
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            
            // Cancel the booking
            bookingService.cancelBooking(payment.getBooking().getId());
        }
        
        return paymentRepository.save(payment);
    }
    
    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN" + timestamp + uuid;
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
    
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BaseException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}









