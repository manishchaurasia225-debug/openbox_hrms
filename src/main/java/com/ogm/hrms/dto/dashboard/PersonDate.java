package com.ogm.hrms.dto.dashboard;

import java.time.LocalDate;

/** An employee associated with an upcoming date (birthday / work anniversary). */
public record PersonDate(Long employeeId, String name, LocalDate date) {
}
