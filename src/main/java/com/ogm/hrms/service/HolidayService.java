package com.ogm.hrms.service;

import com.ogm.hrms.dto.holiday.HolidayRequest;
import com.ogm.hrms.dto.holiday.HolidayResponse;
import com.ogm.hrms.enums.HolidayType;

import java.time.LocalDate;
import java.util.List;

/** Holiday calendar management (RBAC module {@code HOLIDAY}). */
public interface HolidayService {

    HolidayResponse create(HolidayRequest request);

    /** Calendar list; scope by year, or an explicit from/to range, optionally filtered by type. */
    List<HolidayResponse> calendar(Integer year, LocalDate from, LocalDate to, HolidayType type);

    HolidayResponse get(Long id);

    HolidayResponse update(Long id, HolidayRequest request);

    void delete(Long id);
}
