package com.ogm.hrms.dto.probation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Extend a probation period to a later end date. */
public record ExtendProbationRequest(
        @NotNull LocalDate newEndDate,
        @Size(max = 500) String remarks
) {
}
