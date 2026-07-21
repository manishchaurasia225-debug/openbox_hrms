package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.DepartmentResponse;
import com.ogm.hrms.entity.Department;
import org.springframework.stereotype.Component;

/** Maps {@link Department} entities to {@link DepartmentResponse} DTOs. */
@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.getDescription(),
                department.isActive(),
                department.getCreatedAt(),
                department.getUpdatedAt());
    }
}
