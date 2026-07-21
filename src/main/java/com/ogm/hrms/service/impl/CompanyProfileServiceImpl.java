package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.org.CompanyProfileRequest;
import com.ogm.hrms.dto.org.CompanyProfileResponse;
import com.ogm.hrms.entity.CompanyProfile;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.CompanyProfileMapper;
import com.ogm.hrms.repository.CompanyProfileRepository;
import com.ogm.hrms.service.CompanyProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default {@link CompanyProfileService}. Operates on the single seeded company row; the API edits it
 * in place and never creates additional rows (single-company platform).
 */
@Service
public class CompanyProfileServiceImpl implements CompanyProfileService {

    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyProfileMapper companyProfileMapper;

    public CompanyProfileServiceImpl(CompanyProfileRepository companyProfileRepository,
                                     CompanyProfileMapper companyProfileMapper) {
        this.companyProfileRepository = companyProfileRepository;
        this.companyProfileMapper = companyProfileMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyProfileResponse get() {
        return companyProfileMapper.toResponse(load());
    }

    @Override
    @Transactional
    public CompanyProfileResponse update(CompanyProfileRequest request) {
        CompanyProfile company = load();
        company.setLegalName(request.legalName().trim());
        company.setDisplayName(request.displayName().trim());
        company.setRegistrationNumber(request.registrationNumber());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setWebsite(request.website());
        company.setAddressLine1(request.addressLine1());
        company.setAddressLine2(request.addressLine2());
        company.setCity(request.city());
        company.setState(request.state());
        company.setCountry(request.country());
        company.setPostalCode(request.postalCode());
        company.setTimezone(request.timezone());
        company.setCurrency(request.currency());
        company.setLogoUrl(request.logoUrl());
        return companyProfileMapper.toResponse(company);
    }

    private CompanyProfile load() {
        return companyProfileRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ResourceNotFoundException("Company profile is not initialized"));
    }
}
