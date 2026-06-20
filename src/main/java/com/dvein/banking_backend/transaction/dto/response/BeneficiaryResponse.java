package com.dvein.banking_backend.transaction.dto.response;

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
    private String bankName;
    private String branchName;
    private Boolean isActive;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}