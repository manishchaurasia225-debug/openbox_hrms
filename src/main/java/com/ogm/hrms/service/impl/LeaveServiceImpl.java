package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.leave.AllocateLeaveRequest;
import com.ogm.hrms.dto.leave.ApplyLeaveRequest;
import com.ogm.hrms.dto.leave.LeaveBalanceResponse;
import com.ogm.hrms.dto.leave.LeaveRequestResponse;
import com.ogm.hrms.dto.leave.LeaveTypeRequest;
import com.ogm.hrms.dto.leave.LeaveTypeResponse;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.LeaveBalance;
import com.ogm.hrms.entity.LeaveRequest;
import com.ogm.hrms.entity.LeaveType;
import com.ogm.hrms.enums.LeaveStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LeaveBalanceRepository;
import com.ogm.hrms.repository.LeaveRequestRepository;
import com.ogm.hrms.repository.LeaveTypeRepository;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.security.CurrentAccess;
import com.ogm.hrms.service.LeaveService;
import com.ogm.hrms.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Default {@link LeaveService}. Enforces the leave business rules: balances never go negative and an
 * employee cannot approve their own request. Balance is deducted only on final HR approval and
 * restored when a previously-approved leave is cancelled. LOP/WFH-style (unpaid or zero-quota) types
 * are not balance-tracked.
 */
@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationService notificationService;

    private final CurrentAccess currentAccess;

    public LeaveServiceImpl(LeaveTypeRepository leaveTypeRepository, LeaveBalanceRepository leaveBalanceRepository,
                            LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository,
                            NotificationService notificationService, CurrentAccess currentAccess) {
        this.leaveTypeRepository = leaveTypeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.currentAccess = currentAccess;
    }

    // --- leave types -----------------------------------------------------------------------------

    @Override
    @Transactional
    public LeaveTypeResponse createType(LeaveTypeRequest r) {
        if (leaveTypeRepository.existsByCodeIgnoreCase(r.code().trim())) {
            throw ApiException.conflict("A leave type with this code already exists");
        }
        if (leaveTypeRepository.existsByNameIgnoreCase(r.name().trim())) {
            throw ApiException.conflict("A leave type with this name already exists");
        }
        LeaveType type = new LeaveType();
        applyType(type, r);
        return toType(leaveTypeRepository.save(type));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeResponse> listTypes() {
        return leaveTypeRepository.findByDeletedFalseOrderByName().stream().map(this::toType).toList();
    }

    @Override
    @Transactional
    public LeaveTypeResponse updateType(Long id, LeaveTypeRequest r) {
        LeaveType type = leaveTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        if (leaveTypeRepository.existsByCodeIgnoreCaseAndIdNot(r.code().trim(), id)) {
            throw ApiException.conflict("A leave type with this code already exists");
        }
        if (leaveTypeRepository.existsByNameIgnoreCaseAndIdNot(r.name().trim(), id)) {
            throw ApiException.conflict("A leave type with this name already exists");
        }
        applyType(type, r);
        return toType(type);
    }

    @Override
    @Transactional
    public void deleteType(Long id) {
        LeaveType type = leaveTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", id));
        type.setDeleted(true);
        type.setDeletedAt(OffsetDateTime.now());
    }

    // --- balances --------------------------------------------------------------------------------

    @Override
    @Transactional
    public LeaveBalanceResponse allocate(AllocateLeaveRequest r) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(r.employeeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + r.employeeId()));
        LeaveType type = leaveTypeRepository.findByIdAndDeletedFalse(r.leaveTypeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown leave type: " + r.leaveTypeId()));
        LeaveBalance balance = getOrCreateBalance(employee, type, r.year());
        balance.setAllocated(r.allocated());
        if (balance.getUsed().compareTo(r.allocated()) > 0) {
            throw ApiException.badRequest("Allocated cannot be less than already-used days ("
                    + balance.getUsed() + ")");
        }
        return toBalance(leaveBalanceRepository.save(balance));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceResponse> balances(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployee_IdAndYear(employeeId, year).stream().map(this::toBalance).toList();
    }

    // --- requests --------------------------------------------------------------------------------

    @Override
    @Transactional
    public LeaveRequestResponse apply(AuthenticatedUser principal, ApplyLeaveRequest r) {
        Employee employee = currentEmployee(principal);
        LeaveType type = leaveTypeRepository.findByIdAndDeletedFalse(r.leaveTypeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown leave type: " + r.leaveTypeId()));
        if (!type.isActive()) {
            throw ApiException.badRequest("This leave type is not active");
        }
        if (r.fromDate().isAfter(r.toDate())) {
            throw ApiException.badRequest("fromDate must be on or before toDate");
        }
        boolean halfDay = Boolean.TRUE.equals(r.halfDay()) && r.fromDate().equals(r.toDate());
        BigDecimal days = computeDays(r.fromDate(), r.toDate(), halfDay);

        if (isTracked(type)) {
            LeaveBalance balance = getOrCreateBalance(employee, type, r.fromDate().getYear());
            if (balance.remaining().compareTo(days) < 0) {
                throw ApiException.badRequest("Insufficient leave balance: remaining " + balance.remaining()
                        + ", requested " + days);
            }
        }

        LeaveRequest request = new LeaveRequest();
        request.setEmployee(employee);
        request.setLeaveType(type);
        request.setFromDate(r.fromDate());
        request.setToDate(r.toDate());
        request.setDays(days);
        request.setHalfDay(halfDay);
        request.setReason(r.reason());
        request.setStatus(LeaveStatus.PENDING);
        return toRequest(leaveRequestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> myRequests(AuthenticatedUser principal, Pageable pageable) {
        Employee employee = currentEmployee(principal);
        return PageResponse.of(leaveRequestRepository.findByEmployee_Id(employee.getId(), pageable), this::toRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LeaveRequestResponse> list(Long employeeId, LeaveStatus status, Pageable pageable) {
        // Self scope: a standard employee may only ever see their own requests, regardless of filter.
        if (currentAccess.isEmployeeScopeOnly()) {
            Long own = currentAccess.employeeId();
            if (own == null) {
                return PageResponse.of(Page.empty(pageable), this::toRequest);
            }
            employeeId = own;
        }
        if (employeeId != null && status != null) {
            return PageResponse.of(leaveRequestRepository.findByEmployee_IdAndStatus(employeeId, status, pageable), this::toRequest);
        }
        if (status != null) {
            return PageResponse.of(leaveRequestRepository.findByStatus(status, pageable), this::toRequest);
        }
        if (employeeId != null) {
            return PageResponse.of(leaveRequestRepository.findByEmployee_Id(employeeId, pageable), this::toRequest);
        }
        return PageResponse.of(leaveRequestRepository.findAll(pageable), this::toRequest);
    }

    @Override
    @Transactional
    public LeaveRequestResponse approve(Long id, AuthenticatedUser principal, String remarks) {
        LeaveRequest request = loadRequest(id);
        assertNotSelfApproval(request, principal);
        OffsetDateTime now = OffsetDateTime.now();
        switch (request.getStatus()) {
            case PENDING -> {
                request.setStatus(LeaveStatus.MANAGER_APPROVED);
                request.setManagerApprovedBy(principal.email());
                request.setManagerApprovedAt(now);
                request.setDecisionRemarks(remarks);
            }
            case MANAGER_APPROVED -> {
                deductBalanceOnApproval(request);
                request.setStatus(LeaveStatus.APPROVED);
                request.setHrApprovedBy(principal.email());
                request.setHrApprovedAt(now);
                request.setDecisionRemarks(remarks);
                notificationService.notify(request.getEmployee().getUser(), NotificationChannel.IN_APP,
                        "Leave approved",
                        "Your leave from " + request.getFromDate() + " to " + request.getToDate() + " has been approved.",
                        "LEAVE_REQUEST", request.getId());
            }
            default -> throw ApiException.badRequest("Leave request is not awaiting approval");
        }
        return toRequest(request);
    }

    @Override
    @Transactional
    public LeaveRequestResponse reject(Long id, AuthenticatedUser principal, String remarks) {
        LeaveRequest request = loadRequest(id);
        assertNotSelfApproval(request, principal);
        if (request.getStatus() != LeaveStatus.PENDING && request.getStatus() != LeaveStatus.MANAGER_APPROVED) {
            throw ApiException.badRequest("Leave request cannot be rejected in its current state");
        }
        request.setStatus(LeaveStatus.REJECTED);
        request.setDecisionRemarks(remarks);
        notificationService.notify(request.getEmployee().getUser(), NotificationChannel.IN_APP,
                "Leave rejected",
                "Your leave from " + request.getFromDate() + " to " + request.getToDate() + " was rejected.",
                "LEAVE_REQUEST", request.getId());
        return toRequest(request);
    }

    @Override
    @Transactional
    public LeaveRequestResponse cancel(Long id, AuthenticatedUser principal) {
        LeaveRequest request = loadRequest(id);
        if (!isOwner(request, principal)) {
            throw ApiException.forbidden("You can only cancel your own leave request");
        }
        switch (request.getStatus()) {
            case PENDING, MANAGER_APPROVED -> request.setStatus(LeaveStatus.CANCELLED);
            case APPROVED -> {
                restoreBalanceOnCancel(request);
                request.setStatus(LeaveStatus.CANCELLED);
            }
            default -> throw ApiException.badRequest("Leave request cannot be cancelled in its current state");
        }
        return toRequest(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> calendar(LocalDate from, LocalDate to) {
        return leaveRequestRepository
                .findByStatusAndFromDateLessThanEqualAndToDateGreaterThanEqual(LeaveStatus.APPROVED, to, from)
                .stream().map(this::toRequest).toList();
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void deductBalanceOnApproval(LeaveRequest request) {
        LeaveType type = request.getLeaveType();
        if (!isTracked(type)) {
            return;
        }
        LeaveBalance balance = getOrCreateBalance(request.getEmployee(), type, request.getFromDate().getYear());
        BigDecimal newUsed = balance.getUsed().add(request.getDays());
        if (balance.getAllocated().subtract(newUsed).compareTo(BigDecimal.ZERO) < 0) {
            throw ApiException.conflict("Insufficient leave balance to approve this request");
        }
        balance.setUsed(newUsed);
    }

    private void restoreBalanceOnCancel(LeaveRequest request) {
        LeaveType type = request.getLeaveType();
        if (!isTracked(type)) {
            return;
        }
        LeaveBalance balance = getOrCreateBalance(request.getEmployee(), type, request.getFromDate().getYear());
        BigDecimal restored = balance.getUsed().subtract(request.getDays());
        balance.setUsed(restored.max(BigDecimal.ZERO));
    }

    private LeaveRequest loadRequest(Long id) {
        return leaveRequestRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));
    }

    private void assertNotSelfApproval(LeaveRequest request, AuthenticatedUser principal) {
        if (isOwner(request, principal)) {
            throw ApiException.forbidden("You cannot approve or reject your own leave request");
        }
    }

    private boolean isOwner(LeaveRequest request, AuthenticatedUser principal) {
        Employee employee = request.getEmployee();
        return employee.getUser() != null && employee.getUser().getId().equals(principal.id());
    }

    private boolean isTracked(LeaveType type) {
        return type.isPaid() && type.getDefaultAnnualQuota() > 0;
    }

    private BigDecimal computeDays(LocalDate from, LocalDate to, boolean halfDay) {
        if (halfDay) {
            return new BigDecimal("0.5");
        }
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(from, to) + 1);
    }

    private LeaveBalance getOrCreateBalance(Employee employee, LeaveType type, int year) {
        return leaveBalanceRepository.findByEmployee_IdAndLeaveType_IdAndYear(employee.getId(), type.getId(), year)
                .orElseGet(() -> {
                    LeaveBalance balance = new LeaveBalance();
                    balance.setEmployee(employee);
                    balance.setLeaveType(type);
                    balance.setYear(year);
                    balance.setAllocated(BigDecimal.valueOf(type.getDefaultAnnualQuota()));
                    balance.setUsed(BigDecimal.ZERO);
                    return leaveBalanceRepository.save(balance);
                });
    }

    private Employee currentEmployee(AuthenticatedUser principal) {
        return employeeRepository.findByUser_IdAndDeletedFalse(principal.id())
                .orElseThrow(() -> ApiException.badRequest("Your account is not linked to an employee profile"));
    }

    private void applyType(LeaveType type, LeaveTypeRequest r) {
        type.setCode(r.code().trim());
        type.setName(r.name().trim());
        type.setDescription(r.description());
        type.setDefaultAnnualQuota(r.defaultAnnualQuota());
        type.setPaid(r.paid() == null || r.paid());
        type.setActive(r.active() == null || r.active());
    }

    private LeaveTypeResponse toType(LeaveType t) {
        return new LeaveTypeResponse(t.getId(), t.getCode(), t.getName(), t.getDescription(),
                t.getDefaultAnnualQuota(), t.isPaid(), t.isActive());
    }

    private LeaveBalanceResponse toBalance(LeaveBalance b) {
        return new LeaveBalanceResponse(b.getId(), b.getEmployee().getId(), b.getLeaveType().getId(),
                b.getLeaveType().getCode(), b.getYear(), b.getAllocated(), b.getUsed(), b.remaining());
    }

    private LeaveRequestResponse toRequest(LeaveRequest r) {
        Employee e = r.getEmployee();
        LeaveType t = r.getLeaveType();
        return new LeaveRequestResponse(r.getId(), e.getId(), e.getFullName(), t.getId(), t.getCode(),
                r.getFromDate(), r.getToDate(), r.getDays(), r.isHalfDay(), r.getReason(), r.getStatus(),
                r.getManagerApprovedBy(), r.getManagerApprovedAt(), r.getHrApprovedBy(), r.getHrApprovedAt(),
                r.getDecisionRemarks());
    }
}
