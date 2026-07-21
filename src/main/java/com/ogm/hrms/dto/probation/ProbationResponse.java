package com.ogm.hrms.dto.probation;

import com.ogm.hrms.enums.ProbationStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Probation record view. */
public record ProbationResponse(
        Long id,
        Long employeeId,
        String employeeName,
        LocalDate startDate,
        LocalDate endDate,
        ProbationStatus status,
        LocalDate confirmationDate,
        String decidedBy,
        OffsetDateTime decidedAt,
        String remarks
) {
}
