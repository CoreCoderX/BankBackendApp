package com.dvein.banking_backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeMpinRequest {

    @NotBlank
    private String oldMpin;

    @NotBlank
    private String newMpin;

    @NotBlank
    private String confirmMpin;
}
