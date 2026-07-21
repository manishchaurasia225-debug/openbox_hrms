package com.ogm.hrms.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update payload for an employee emergency contact. */
public record EmergencyContactRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 60) String relationship,
        @Size(max = 30) String phone,
        @Size(max = 30) String alternatePhone,
        @Size(max = 190) String email,
        @Size(max = 300) String address
) {
}
