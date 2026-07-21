package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.leave.AllocateLeaveRequest;
import com.ogm.hrms.dto.leave.ApplyLeaveRequest;
import com.ogm.hrms.dto.leave.LeaveBalanceResponse;
import com.ogm.hrms.dto.leave.LeaveRequestResponse;
import com.ogm.hrms.dto.leave.LeaveTypeRequest;
import com.ogm.hrms.dto.leave.LeaveTypeResponse;
import com.ogm.hrms.enums.LeaveStatus;
import com.ogm.hrms.security.AuthenticatedUser;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/** Leave Management (RBAC module {@code LEAVE}): types, balances, and the request workflow. */
public interface LeaveService {

    LeaveTypeResponse createType(LeaveTypeRequest request);

    List<LeaveTypeResponse> listTypes();

    LeaveTypeResponse updateType(Long id, LeaveTypeRequest request);

    void deleteType(Long id);

    LeaveBalanceResponse allocate(AllocateLeaveRequest request);

    List<LeaveBalanceResponse> balances(Long employeeId, int year);

    LeaveRequestResponse apply(AuthenticatedUser principal, ApplyLeaveRequest request);

    PageResponse<LeaveRequestResponse> myRequests(AuthenticatedUser principal, Pageable pageable);

    PageResponse<LeaveRequestResponse> list(Long employeeId, LeaveStatus status, Pageable pageable);

    LeaveRequestResponse approve(Long id, AuthenticatedUser principal, String remarks);

    LeaveRequestResponse reject(Long id, AuthenticatedUser principal, String remarks);

    LeaveRequestResponse cancel(Long id, AuthenticatedUser principal);

    List<LeaveRequestResponse> calendar(LocalDate from, LocalDate to);
}
