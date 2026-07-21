package com.ogm.hrms.repository;

import com.ogm.hrms.entity.Holiday;
import com.ogm.hrms.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    boolean existsByHolidayDateAndNameIgnoreCase(LocalDate holidayDate, String name);

    boolean existsByHolidayDateAndNameIgnoreCaseAndIdNot(LocalDate holidayDate, String name, Long id);

    List<Holiday> findByDeletedFalseAndHolidayDateBetweenOrderByHolidayDateAsc(LocalDate from, LocalDate to);

    List<Holiday> findByDeletedFalseAndTypeAndHolidayDateBetweenOrderByHolidayDateAsc(HolidayType type,
                                                                                     LocalDate from, LocalDate to);

    Optional<Holiday> findByIdAndDeletedFalse(Long id);
}
