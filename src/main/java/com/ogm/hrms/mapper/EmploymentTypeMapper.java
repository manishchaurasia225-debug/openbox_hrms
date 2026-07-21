package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.EmploymentTypeResponse;
import com.ogm.hrms.entity.EmploymentType;
import org.springframework.stereotype.Component;

/** Maps {@link EmploymentType} entities to {@link EmploymentTypeResponse} DTOs. */
@Component
public class EmploymentTypeMapper {

    public EmploymentTypeResponse toResponse(EmploymentType employmentType) {
        return new EmploymentTypeResponse(
                employmentType.getId(),
                employmentType.getCode(),
                employmentType.getName(),
                employmentType.getDescription(),
                employmentType.isActive(),
                employmentType.getCreatedAt(),
                employmentType.getUpdatedAt());
    }
}
