package com.dvein.banking_backend.loan.mapper;

import com.dvein.banking_backend.loan.dto.response.LoanScheduleResponse;
import com.dvein.banking_backend.loan.model.LoanSchedule;
import org.springframework.stereotype.Component;

@Component
public class LoanScheduleMapper {

    public LoanScheduleResponse toResponse(LoanSchedule schedule) {
        return LoanScheduleResponse.builder()
                .emiNumber(schedule.getEmiNumber())
                .dueDate(schedule.getDueDate())
                .emiAmount(schedule.getEmiAmount())
                .principalComponent(schedule.getPrincipalComponent())
                .interestComponent(schedule.getInterestComponent())
                .outstandingPrincipal(schedule.getOutstandingPrincipal())
                .status(schedule.getStatus())
                .paidDate(schedule.getPaidDate())
                .build();
    }
}