package com.sayedhesham.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderResponseDTO {
    
    private List<ReorderItemDTO> availableItems;
    
    private List<UnavailableItemDTO> unavailableItems;
    
    private List<String> warnings;
    
    private String originalOrderId;
    
    private String fetchedAt;
}