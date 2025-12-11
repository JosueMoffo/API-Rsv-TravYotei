package com.example.Rsv_TravYotei.model;

import java.util.List;
import java.util.UUID;

public class ReservationRequest {
    private String clientId;
    private UUID transportId;
    private Double totalAmount;
    private List<ReservationItemRequest> items;

    // getters et setters
}