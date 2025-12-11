package com.example.Rsv_TravYotei.model.dto;

import lombok.Data;

@Data
public class ReservationItemRequest {
    private Integer seatNumber;
    private String passengerName;
}