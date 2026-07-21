package com.ogm.hrms.dto.employee;

import java.time.LocalDate;

/** Prior-experience record view. */
public record ExperienceResponse(
        Long id,
        String companyName,
        String designation,
        LocalDate fromDate,
        LocalDate toDate,
        String description
) {
}
