package com.ogm.hrms.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for an employee prior-experience record. */
public record ExperienceRequest(
        @NotBlank @Size(max = 200) String companyName,
        @Size(max = 150) String designation,
        LocalDate fromDate,
        LocalDate toDate,
        @Size(max = 500) String description
) {
}
