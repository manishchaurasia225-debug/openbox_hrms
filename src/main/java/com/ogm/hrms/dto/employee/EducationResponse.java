package com.ogm.hrms.dto.employee;

/** Education record view. */
public record EducationResponse(
        Long id,
        String institution,
        String degree,
        String fieldOfStudy,
        Integer startYear,
        Integer endYear,
        String grade
) {
}
