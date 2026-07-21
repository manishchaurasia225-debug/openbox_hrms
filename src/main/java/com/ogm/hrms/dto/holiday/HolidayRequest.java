package com.ogm.hrms.dto.holiday;

import com.ogm.hrms.enums.HolidayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Create/update payload for a holiday. */
public record HolidayRequest(
        @NotNull LocalDate holidayDate,
        @NotBlank @Size(max = 150) String name,
        @NotNull HolidayType type,
        @Size(max = 100) String region,
        @Size(max = 300) String description,
        Boolean recurring
) {
}
