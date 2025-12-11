package com.example.Rsv_TravYotei.model.dto;

import lombok.Data;

@Data
public class ReservationRequest {
    private String clientId;
    private String transportId;
    private Double totalAmount;
}