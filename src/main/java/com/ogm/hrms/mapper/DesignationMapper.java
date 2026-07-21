package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.DesignationResponse;
import com.ogm.hrms.entity.Designation;
import org.springframework.stereotype.Component;

/** Maps {@link Designation} entities to {@link DesignationResponse} DTOs. */
@Component
public class DesignationMapper {

    public DesignationResponse toResponse(Designation designation) {
        return new DesignationResponse(
                designation.getId(),
                designation.getCode(),
                designation.getName(),
                designation.getDescription(),
                designation.isActive(),
                designation.getCreatedAt(),
                designation.getUpdatedAt());
    }
}
