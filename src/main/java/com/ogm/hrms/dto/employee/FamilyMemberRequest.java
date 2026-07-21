package com.ogm.hrms.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for an employee family member. */
public record FamilyMemberRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 60) String relationship,
        LocalDate dateOfBirth,
        @Size(max = 120) String occupation,
        @Size(max = 30) String phone,
        Boolean dependent
) {
}
