package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.org.LocationRequest;
import com.ogm.hrms.dto.org.LocationResponse;
import com.ogm.hrms.entity.Location;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.LocationMapper;
import com.ogm.hrms.repository.LocationRepository;
import com.ogm.hrms.service.LocationService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/** Default {@link LocationService}: unique code/name enforcement, soft delete. */
@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository repository;
    private final LocationMapper mapper;

    public LocationServiceImpl(LocationRepository repository, LocationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public LocationResponse create(LocationRequest request) {
        String code = request.code().trim();
        String name = request.name().trim();
        if (repository.existsByCodeIgnoreCase(code)) {
            throw ApiException.conflict("A location with this code already exists");
        }
        if (repository.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("A location with this name already exists");
        }
        Location entity = new Location();
        apply(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LocationResponse> list(Pageable pageable) {
        return PageResponse.of(repository.findByDeletedFalse(pageable), mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse get(Long id) {
        return mapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public LocationResponse update(Long id, LocationRequest request) {
        Location entity = load(id);
        String code = request.code().trim();
        String name = request.name().trim();
        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw ApiException.conflict("A location with this code already exists");
        }
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("A location with this name already exists");
        }
        apply(entity, request);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Location entity = load(id);
        entity.setDeleted(true);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private Location load(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
    }

    private void apply(Location entity, LocationRequest request) {
        entity.setCode(request.code().trim());
        entity.setName(request.name().trim());
        entity.setAddressLine1(request.addressLine1());
        entity.setAddressLine2(request.addressLine2());
        entity.setCity(request.city());
        entity.setState(request.state());
        entity.setCountry(request.country());
        entity.setPostalCode(request.postalCode());
        entity.setActive(request.active() == null || request.active());
    }
}
