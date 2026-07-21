package com.ogm.hrms.dto.employee;

import java.time.LocalDate;

/** Family member view. */
public record FamilyMemberResponse(
        Long id,
        String name,
        String relationship,
        LocalDate dateOfBirth,
        String occupation,
        String phone,
        boolean dependent
) {
}
