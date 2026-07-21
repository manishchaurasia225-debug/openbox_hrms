package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.holiday.HolidayRequest;
import com.ogm.hrms.dto.holiday.HolidayResponse;
import com.ogm.hrms.entity.Holiday;
import com.ogm.hrms.enums.HolidayType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.HolidayRepository;
import com.ogm.hrms.service.HolidayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Default {@link HolidayService}: prevents duplicate date+name, supports year/range calendar views. */
@Service
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;

    public HolidayServiceImpl(HolidayRepository holidayRepository) {
        this.holidayRepository = holidayRepository;
    }

    @Override
    @Transactional
    public HolidayResponse create(HolidayRequest request) {
        if (holidayRepository.existsByHolidayDateAndNameIgnoreCase(request.holidayDate(), request.name().trim())) {
            throw ApiException.conflict("A holiday with this name already exists on that date");
        }
        Holiday holiday = new Holiday();
        apply(holiday, request);
        return toResponse(holidayRepository.save(holiday));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> calendar(Integer year, LocalDate from, LocalDate to, HolidayType type) {
        LocalDate start;
        LocalDate end;
        if (year != null) {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        } else if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw ApiException.badRequest("'from' must be on or before 'to'");
            }
            start = from;
            end = to;
        } else {
            int currentYear = LocalDate.now().getYear();
            start = LocalDate.of(currentYear, 1, 1);
            end = LocalDate.of(currentYear, 12, 31);
        }
        List<Holiday> holidays = (type != null)
                ? holidayRepository.findByDeletedFalseAndTypeAndHolidayDateBetweenOrderByHolidayDateAsc(type, start, end)
                : holidayRepository.findByDeletedFalseAndHolidayDateBetweenOrderByHolidayDateAsc(start, end);
        return holidays.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HolidayResponse get(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional
    public HolidayResponse update(Long id, HolidayRequest request) {
        Holiday holiday = load(id);
        if (holidayRepository.existsByHolidayDateAndNameIgnoreCaseAndIdNot(request.holidayDate(), request.name().trim(), id)) {
            throw ApiException.conflict("A holiday with this name already exists on that date");
        }
        apply(holiday, request);
        return toResponse(holiday);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Holiday holiday = load(id);
        holiday.setDeleted(true);
        holiday.setDeletedAt(OffsetDateTime.now());
    }

    private Holiday load(Long id) {
        return holidayRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
    }

    private void apply(Holiday holiday, HolidayRequest request) {
        holiday.setHolidayDate(request.holidayDate());
        holiday.setName(request.name().trim());
        holiday.setType(request.type());
        holiday.setRegion(request.region());
        holiday.setDescription(request.description());
        holiday.setRecurring(request.recurring() != null && request.recurring());
    }

    private HolidayResponse toResponse(Holiday h) {
        return new HolidayResponse(h.getId(), h.getHolidayDate(), h.getName(), h.getType(), h.getRegion(),
                h.getDescription(), h.isRecurring());
    }
}
