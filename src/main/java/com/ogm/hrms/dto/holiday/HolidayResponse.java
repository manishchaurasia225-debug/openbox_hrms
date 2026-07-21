package com.ogm.hrms.dto.holiday;

import com.ogm.hrms.enums.HolidayType;

import java.time.LocalDate;

/** Holiday view. */
public record HolidayResponse(
        Long id,
        LocalDate holidayDate,
        String name,
        HolidayType type,
        String region,
        String description,
        boolean recurring
) {
}
