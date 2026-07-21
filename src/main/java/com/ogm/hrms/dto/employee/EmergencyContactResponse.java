package com.ogm.hrms.dto.employee;

/** Emergency contact view. */
public record EmergencyContactResponse(
        Long id,
        String name,
        String relationship,
        String phone,
        String alternatePhone,
        String email,
        String address
) {
}
