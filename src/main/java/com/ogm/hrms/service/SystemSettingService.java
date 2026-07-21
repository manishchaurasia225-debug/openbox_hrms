package com.ogm.hrms.service;

import com.ogm.hrms.dto.org.SystemSettingRequest;
import com.ogm.hrms.dto.org.SystemSettingResponse;

import java.util.List;

/** Manage the key/value system-settings store (RBAC module {@code SETTINGS}). */
public interface SystemSettingService {

    List<SystemSettingResponse> list(String category);

    SystemSettingResponse get(String key);

    SystemSettingResponse create(SystemSettingRequest request);

    SystemSettingResponse updateValue(String key, String value);

    void delete(String key);
}
