package com.dvein.banking_backend.admin.dto.request;

import com.dvein.banking_backend.common.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer search request")
public class CustomerSearchRequest {

    @Schema(description = "Search term", example = "John")
    private String searchTerm;

    @Schema(description = "Customer status filter")
    private CustomerStatus status;

    @Schema(description = "Email filter", example = "john@example.com")
    private String email;

    @Schema(description = "Phone filter", example = "9876543210")
    private String phone;

    @Schema(description = "PAN filter", example = "ABCDE1234F")
    private String pan;

    @Schema(description = "Aadhaar filter", example = "123456789012")
    private String aadhaar;

    @Builder.Default
    @Schema(description = "Page number", example = "0")
    private int page = 0;

    @Builder.Default
    @Schema(description = "Page size", example = "20")
    private int size = 20;

    @Builder.Default
    @Schema(description = "Sort by", example = "createdAt")
    private String sortBy = "createdAt";

    @Builder.Default
    @Schema(description = "Sort direction", example = "DESC")
    private String sortDirection = "DESC";
}