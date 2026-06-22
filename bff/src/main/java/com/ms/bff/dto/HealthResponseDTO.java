package com.ms.bff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record HealthResponseDTO(
    @Schema(description = "Status of the application", example = "OK")
    String status
) {
    

}
