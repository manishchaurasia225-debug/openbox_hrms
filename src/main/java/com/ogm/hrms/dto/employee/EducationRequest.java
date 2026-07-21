package com.ogm.hrms.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update payload for an employee education record. */
public record EducationRequest(
        @NotBlank @Size(max = 200) String institution,
        @Size(max = 150) String degree,
        @Size(max = 150) String fieldOfStudy,
        Integer startYear,
        Integer endYear,
        @Size(max = 60) String grade
) {
}
