package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.org.SystemSettingRequest;
import com.ogm.hrms.dto.org.SystemSettingResponse;
import com.ogm.hrms.entity.SystemSetting;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.SystemSettingMapper;
import com.ogm.hrms.repository.SystemSettingRepository;
import com.ogm.hrms.service.SystemSettingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Default {@link SystemSettingService}. Enforces unique keys, and protects settings flagged
 * non-editable (seeded, structural) from modification or deletion.
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository repository;
    private final SystemSettingMapper mapper;

    public SystemSettingServiceImpl(SystemSettingRepository repository, SystemSettingMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemSettingResponse> list(String category) {
        List<SystemSetting> settings = (category == null || category.isBlank())
                ? repository.findByDeletedFalseOrderBySettingKeyAsc()
                : repository.findByCategoryIgnoreCaseAndDeletedFalseOrderBySettingKeyAsc(category.trim());
        return settings.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettingResponse get(String key) {
        return mapper.toResponse(load(key));
    }

    @Override
    @Transactional
    public SystemSettingResponse create(SystemSettingRequest request) {
        String key = request.key().trim();
        if (repository.existsBySettingKey(key)) {
            throw ApiException.conflict("A setting with this key already exists");
        }
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(request.value());
        setting.setCategory(request.category());
        setting.setDescription(request.description());
        setting.setEditable(true);
        return mapper.toResponse(repository.save(setting));
    }

    @Override
    @Transactional
    public SystemSettingResponse updateValue(String key, String value) {
        SystemSetting setting = load(key);
        if (!setting.isEditable()) {
            throw ApiException.forbidden("This setting is not editable");
        }
        setting.setSettingValue(value);
        return mapper.toResponse(setting);
    }

    @Override
    @Transactional
    public void delete(String key) {
        SystemSetting setting = load(key);
        if (!setting.isEditable()) {
            throw ApiException.forbidden("This setting is not editable");
        }
        setting.setDeleted(true);
        setting.setDeletedAt(OffsetDateTime.now());
    }

    private SystemSetting load(String key) {
        return repository.findBySettingKey(key)
                .filter(setting -> !setting.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("SystemSetting", "key", key));
    }
}
