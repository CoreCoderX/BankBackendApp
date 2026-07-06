package com.dvein.banking_backend.card.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Block card request")
public class BlockCardRequest {

    @NotBlank(message = "Reason is required")
    @Schema(description = "Block reason", example = "Card lost")
    private String reason;
}