package com.example.Rsv_TravYotei.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmationEvent {
    
    @JsonProperty("reservationId")
    private String reservationId;
    
    @JsonProperty("status")
    private String status;  // "CONFIRMED"
    
    @JsonProperty("amount")
    private Double amount;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Constructeur pour Jackson
    @JsonCreator
    public static PaymentConfirmationEvent create(
            @JsonProperty("reservationId") String reservationId,
            @JsonProperty("status") String status,
            @JsonProperty("amount") Double amount,
            @JsonProperty("paymentMethod") String paymentMethod,
            @JsonProperty("transactionId") String transactionId,
            @JsonProperty("timestamp") String timestamp) {
        return PaymentConfirmationEvent.builder()
                .reservationId(reservationId)
                .status(status)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .transactionId(transactionId)
                .timestamp(timestamp)
                .build();
    }
}