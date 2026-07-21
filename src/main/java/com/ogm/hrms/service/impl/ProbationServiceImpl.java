package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.probation.ConfirmProbationRequest;
import com.ogm.hrms.dto.probation.ExtendProbationRequest;
import com.ogm.hrms.dto.probation.ProbationResponse;
import com.ogm.hrms.dto.probation.StartProbationRequest;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.ProbationRecord;
import com.ogm.hrms.enums.EmploymentStatus;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.ProbationStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.ProbationRecordRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.service.NotificationService;
import com.ogm.hrms.service.ProbationService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Default {@link ProbationService}. One active probation per employee; confirmation/extension are
 * recorded decisions that notify the employee. Termination also moves the employee's employment
 * status to TERMINATED.
 */
@Service
public class ProbationServiceImpl implements ProbationService {

    private static final Set<ProbationStatus> ACTIVE = EnumSet.of(ProbationStatus.IN_PROBATION, ProbationStatus.EXTENDED);

    private final ProbationRecordRepository probationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    public ProbationServiceImpl(ProbationRecordRepository probationRepository, EmployeeRepository employeeRepository,
                                NotificationService notificationService) {
        this.probationRepository = probationRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ProbationResponse start(StartProbationRequest r) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(r.employeeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + r.employeeId()));
        if (r.startDate().isAfter(r.endDate())) {
            throw ApiException.badRequest("startDate must be on or before endDate");
        }
        if (probationRepository.existsByEmployee_IdAndStatusIn(employee.getId(), ACTIVE)) {
            throw ApiException.conflict("This employee already has an active probation record");
        }
        ProbationRecord record = new ProbationRecord();
        record.setEmployee(employee);
        record.setStartDate(r.startDate());
        record.setEndDate(r.endDate());
        record.setStatus(ProbationStatus.IN_PROBATION);
        record.setRemarks(r.remarks());
        return toResponse(probationRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProbationResponse> list(Long employeeId, ProbationStatus status, Pageable pageable) {
        if (employeeId != null) {
            return PageResponse.of(probationRepository.findByEmployee_Id(employeeId, pageable), this::toResponse);
        }
        if (status != null) {
            return PageResponse.of(probationRepository.findByStatus(status, pageable), this::toResponse);
        }
        return PageResponse.of(probationRepository.findAll(pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProbationResponse get(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProbationResponse> upcoming(int withinDays) {
        LocalDate today = LocalDate.now();
        return probationRepository
                .findByStatusInAndEndDateBetweenOrderByEndDateAsc(ACTIVE, today, today.plusDays(withinDays))
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ProbationResponse confirm(Long id, AuthenticatedUser principal, ConfirmProbationRequest r) {
        ProbationRecord record = requireActive(id);
        record.setStatus(ProbationStatus.CONFIRMED);
        record.setConfirmationDate(r.confirmationDate() != null ? r.confirmationDate() : LocalDate.now());
        stampDecision(record, principal, r.remarks());
        notify(record, "Confirmation", "Congratulations — your employment has been confirmed.");
        return toResponse(record);
    }

    @Override
    @Transactional
    public ProbationResponse extend(Long id, AuthenticatedUser principal, ExtendProbationRequest r) {
        ProbationRecord record = requireActive(id);
        if (!r.newEndDate().isAfter(record.getEndDate())) {
            throw ApiException.badRequest("newEndDate must be after the current end date");
        }
        record.setStatus(ProbationStatus.EXTENDED);
        record.setEndDate(r.newEndDate());
        stampDecision(record, principal, r.remarks());
        notify(record, "Probation extended", "Your probation has been extended to " + r.newEndDate() + ".");
        return toResponse(record);
    }

    @Override
    @Transactional
    public ProbationResponse terminate(Long id, AuthenticatedUser principal, String remarks) {
        ProbationRecord record = requireActive(id);
        record.setStatus(ProbationStatus.TERMINATED);
        stampDecision(record, principal, remarks);
        record.getEmployee().setEmploymentStatus(EmploymentStatus.TERMINATED);
        return toResponse(record);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private ProbationRecord load(Long id) {
        return probationRepository.findWithEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProbationRecord", "id", id));
    }

    private ProbationRecord requireActive(Long id) {
        ProbationRecord record = load(id);
        if (!ACTIVE.contains(record.getStatus())) {
            throw ApiException.badRequest("This probation record is not active");
        }
        return record;
    }

    private void stampDecision(ProbationRecord record, AuthenticatedUser principal, String remarks) {
        record.setDecidedBy(principal.email());
        record.setDecidedAt(OffsetDateTime.now());
        record.setRemarks(remarks);
    }

    private void notify(ProbationRecord record, String title, String message) {
        notificationService.notify(record.getEmployee().getUser(), NotificationChannel.IN_APP, title, message,
                "PROBATION", record.getId());
    }

    private ProbationResponse toResponse(ProbationRecord p) {
        Employee e = p.getEmployee();
        return new ProbationResponse(p.getId(), e.getId(), e.getFullName(), p.getStartDate(), p.getEndDate(),
                p.getStatus(), p.getConfirmationDate(), p.getDecidedBy(), p.getDecidedAt(), p.getRemarks());
    }
}
