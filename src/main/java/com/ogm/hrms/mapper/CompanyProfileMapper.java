package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.org.CompanyProfileResponse;
import com.ogm.hrms.entity.CompanyProfile;
import org.springframework.stereotype.Component;

/** Maps {@link CompanyProfile} entities to {@link CompanyProfileResponse} DTOs. */
@Component
public class CompanyProfileMapper {

    public CompanyProfileResponse toResponse(CompanyProfile company) {
        return new CompanyProfileResponse(
                company.getId(),
                company.getLegalName(),
                company.getDisplayName(),
                company.getRegistrationNumber(),
                company.getEmail(),
                company.getPhone(),
                company.getWebsite(),
                company.getAddressLine1(),
                company.getAddressLine2(),
                company.getCity(),
                company.getState(),
                company.getCountry(),
                company.getPostalCode(),
                company.getTimezone(),
                company.getCurrency(),
                company.getLogoUrl(),
                company.getUpdatedAt());
    }
}
