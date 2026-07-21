package com.ogm.hrms.service;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.salary.GeneratePayslipRequest;
import com.ogm.hrms.dto.salary.PayslipResponse;
import com.ogm.hrms.dto.salary.SalaryStructureRequest;
import com.ogm.hrms.dto.salary.SalaryStructureResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/** Salary & Compensation (RBAC module {@code PAYROLL}): revision history and payslips. */
public interface SalaryService {

    SalaryStructureResponse addRevision(SalaryStructureRequest request);

    List<SalaryStructureResponse> history(Long employeeId);

    SalaryStructureResponse current(Long employeeId);

    PayslipResponse generatePayslip(GeneratePayslipRequest request);

    PageResponse<PayslipResponse> listPayslips(Long employeeId, Pageable pageable);

    PayslipResponse getPayslip(Long id);

    DocumentDownload downloadPayslip(Long id);
}
