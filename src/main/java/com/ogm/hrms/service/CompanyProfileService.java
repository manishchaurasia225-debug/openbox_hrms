package com.ogm.hrms.service;

import com.ogm.hrms.dto.org.CompanyProfileRequest;
import com.ogm.hrms.dto.org.CompanyProfileResponse;

/** Read/update the single company profile (RBAC module {@code COMPANY}). */
public interface CompanyProfileService {

    CompanyProfileResponse get();

    CompanyProfileResponse update(CompanyProfileRequest request);
}
