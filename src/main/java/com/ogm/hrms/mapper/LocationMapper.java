package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.LocationResponse;
import com.ogm.hrms.entity.Location;
import org.springframework.stereotype.Component;

/** Maps {@link Location} entities to {@link LocationResponse} DTOs. */
@Component
public class LocationMapper {

    public LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getCode(),
                location.getName(),
                location.getAddressLine1(),
                location.getAddressLine2(),
                location.getCity(),
                location.getState(),
                location.getCountry(),
                location.getPostalCode(),
                location.isActive(),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }
}
