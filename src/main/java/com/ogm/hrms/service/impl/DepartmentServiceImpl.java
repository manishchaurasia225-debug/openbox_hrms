package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DepartmentRequest;
import com.ogm.hrms.dto.org.DepartmentResponse;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.DepartmentMapper;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.service.DepartmentService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Default {@link DepartmentService}: unique code/name enforcement, soft delete. */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        if (departmentRepository.existsByCodeIgnoreCase(code)) {
            throw ApiException.conflict("A department with this code already exists");
        }
        if (departmentRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("A department with this name already exists");
        }
        Department department = new Department();
        apply(department, request);
        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> list(Pageable pageable) {
        return PageResponse.of(departmentRepository.findByDeletedFalse(pageable), departmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id) {
        return departmentMapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = load(id);
        String code = request.code().trim();
        String name = request.name().trim();
        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw ApiException.conflict("A department with this code already exists");
        }
        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("A department with this name already exists");
        }
        apply(department, request);
        return departmentMapper.toResponse(department);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Department department = load(id);
        department.setDeleted(true);
        department.setDeletedAt(OffsetDateTime.now());
    }

    private Department load(Long id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private void apply(Department department, DepartmentRequest request) {
        department.setCode(request.code().trim());
        department.setName(request.name().trim());
        department.setDescription(request.description());
        department.setActive(request.active() == null || request.active());
    }
}
