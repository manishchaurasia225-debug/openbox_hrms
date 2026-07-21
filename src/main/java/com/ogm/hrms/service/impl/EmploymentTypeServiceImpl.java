package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.EmploymentTypeRequest;
import com.ogm.hrms.dto.org.EmploymentTypeResponse;
import com.ogm.hrms.entity.EmploymentType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.EmploymentTypeMapper;
import com.ogm.hrms.repository.EmploymentTypeRepository;
import com.ogm.hrms.service.EmploymentTypeService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Default {@link EmploymentTypeService}: unique code/name enforcement, soft delete. */
@Service
public class EmploymentTypeServiceImpl implements EmploymentTypeService {

    private final EmploymentTypeRepository repository;
    private final EmploymentTypeMapper mapper;

    public EmploymentTypeServiceImpl(EmploymentTypeRepository repository, EmploymentTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public EmploymentTypeResponse create(EmploymentTypeRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        if (repository.existsByCodeIgnoreCase(code)) {
            throw ApiException.conflict("An employment type with this code already exists");
        }
        if (repository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("An employment type with this name already exists");
        }
        EmploymentType entity = new EmploymentType();
        apply(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmploymentTypeResponse> list(Pageable pageable) {
        return PageResponse.of(repository.findByDeletedFalse(pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmploymentTypeResponse get(Long id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public EmploymentTypeResponse update(Long id, EmploymentTypeRequest request) {
        EmploymentType entity = load(id);
        String code = request.code().trim();
        String name = request.name().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw ApiException.conflict("An employment type with this code already exists");
        }
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("An employment type with this name already exists");
        }
        apply(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmploymentType entity = load(id);
        entity.setDeleted(true);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private EmploymentType load(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("EmploymentType", "id", id));
    }

    private void apply(EmploymentType entity, EmploymentTypeRequest request) {
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setActive(request.active() == null || request.active());
    }
}
