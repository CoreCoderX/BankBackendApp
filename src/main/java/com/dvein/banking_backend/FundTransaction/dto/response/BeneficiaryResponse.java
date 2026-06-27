package com.dvein.banking_backend.FundTransaction.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryResponse {

    private Long id;
    private String nickname;
    private String accountNumber;
    private String ifscCode;
    private String bank_name;
    private String branchName;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}