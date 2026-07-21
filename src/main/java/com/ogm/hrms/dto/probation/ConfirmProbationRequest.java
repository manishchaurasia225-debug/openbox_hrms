package com.ogm.hrms.dto.probation;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Confirm an employee (end of probation). Defaults confirmation date to today when omitted. */
public record ConfirmProbationRequest(
        LocalDate confirmationDate,
        @Size(max = 500) String remarks
) {
}
