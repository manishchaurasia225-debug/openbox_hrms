package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.DesignationRequest;
import com.ogm.hrms.dto.org.DesignationResponse;
import com.ogm.hrms.entity.Designation;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.DesignationMapper;
import com.ogm.hrms.repository.DesignationRepository;
import com.ogm.hrms.service.DesignationService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Default {@link DesignationService}: unique code/name enforcement, soft delete. */
@Service
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;

    public DesignationServiceImpl(DesignationRepository designationRepository, DesignationMapper designationMapper) {
        this.designationRepository = designationRepository;
        this.designationMapper = designationMapper;
    }

    @Override
    @Transactional
    public DesignationResponse create(DesignationRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        if (designationRepository.existsByCodeIgnoreCase(code)) {
            throw ApiException.conflict("A designation with this code already exists");
        }
        if (designationRepository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("A designation with this name already exists");
        }
        Designation designation = new Designation();
        apply(designation, request);
        return designationMapper.toResponse(designationRepository.save(designation));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DesignationResponse> list(Pageable pageable) {
        return PageResponse.of(designationRepository.findByDeletedFalse(pageable), designationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DesignationResponse get(Long id) {
        return designationMapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public DesignationResponse update(Long id, DesignationRequest request) {
        Designation designation = load(id);
        String code = request.code().trim();
        String name = request.name().trim();
        if (designationRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw ApiException.conflict("A designation with this code already exists");
        }
        if (designationRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("A designation with this name already exists");
        }
        apply(designation, request);
        return designationMapper.toResponse(designation);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Designation designation = load(id);
        designation.setDeleted(true);
        designation.setDeletedAt(OffsetDateTime.now());
    }

    private Designation load(Long id) {
        return designationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Designation", "id", id));
    }

    private void apply(Designation designation, DesignationRequest request) {
        designation.setCode(request.code().trim());
        designation.setName(request.name().trim());
        designation.setDescription(request.description());
        designation.setActive(request.active() == null || request.active());
    }
}
