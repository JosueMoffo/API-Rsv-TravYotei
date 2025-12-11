package com.example.Rsv_TravYotei.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class CreateReservationRequest {
    
    @NotBlank(message = "L'ID client est requis")
    @Size(max = 128, message = "L'ID client ne peut pas dépasser 128 caractères")
    private String clientId;
    
    @NotBlank(message = "L'ID du trajet est requis")
    private String transportId;
    
    @NotEmpty(message = "Au moins un passager est requis")
    @Size(min = 1, max = 50, message = "Entre 1 et 50 passagers maximum")
    private List<PassengerInfo> passengers;
    
    @Data
    public static class PassengerInfo {
        
        @NotBlank(message = "Le nom du passager est requis")
        @Size(max = 255, message = "Le nom du passager ne peut pas dépasser 255 caractères")
        private String name;
        
        @Min(value = 1, message = "Le numéro de siège doit être supérieur à 0")
        @Max(value = 999, message = "Le numéro de siège ne peut pas dépasser 999")
        private Integer seatNumber;
    }
}