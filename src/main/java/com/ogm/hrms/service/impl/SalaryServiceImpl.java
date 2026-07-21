package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.document.DocumentDownload;
import com.ogm.hrms.dto.salary.GeneratePayslipRequest;
import com.ogm.hrms.dto.salary.PayslipResponse;
import com.ogm.hrms.dto.salary.SalaryStructureRequest;
import com.ogm.hrms.dto.salary.SalaryStructureResponse;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.Payslip;
import com.ogm.hrms.entity.SalaryStructure;
import com.ogm.hrms.entity.embed.SalaryInfo;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.PayslipStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.security.CurrentAccess;
import com.ogm.hrms.repository.CompanyProfileRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.PayslipRepository;
import com.ogm.hrms.repository.SalaryStructureRepository;
import com.ogm.hrms.service.EmailService;
import com.ogm.hrms.service.NotificationService;
import com.ogm.hrms.service.PayslipPdfGenerator;
import com.ogm.hrms.service.SalaryService;
import com.ogm.hrms.storage.StorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Default {@link SalaryService}. Adds dated salary revisions (syncing the employee's current salary
 * snapshot), and generates monthly payslips — snapshotting components, rendering a PDF into storage,
 * emailing it, and raising an in-app notification. Earnings only (no statutory deductions).
 */
@Service
public class SalaryServiceImpl implements SalaryService {

    private final EmployeeRepository employeeRepository;
    private final SalaryStructureRepository salaryStructureRepository;
    private final PayslipRepository payslipRepository;
    private final StorageService storageService;
    private final PayslipPdfGenerator payslipPdfGenerator;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final CompanyProfileRepository companyProfileRepository;

    private final CurrentAccess currentAccess;

    /**
     * Authorities that grant company-wide payroll scope (view anyone's salary/payslips). A caller
     * with only {@code PAYROLL:VIEW}/{@code PAYROLL:EXPORT} (a standard employee) is scoped to their
     * own payroll, per permissions-matrix.md.
     */
    private static final String[] PAYROLL_ALL_SCOPE = {
            "PAYROLL:CREATE", "PAYROLL:EDIT", "PAYROLL:APPROVE", "PAYROLL:ADMIN"
    };

    public SalaryServiceImpl(EmployeeRepository employeeRepository,
                             SalaryStructureRepository salaryStructureRepository, PayslipRepository payslipRepository,
                             StorageService storageService, PayslipPdfGenerator payslipPdfGenerator,
                             EmailService emailService, NotificationService notificationService,
                             CompanyProfileRepository companyProfileRepository, CurrentAccess currentAccess) {
        this.employeeRepository = employeeRepository;
        this.salaryStructureRepository = salaryStructureRepository;
        this.payslipRepository = payslipRepository;
        this.storageService = storageService;
        this.payslipPdfGenerator = payslipPdfGenerator;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.companyProfileRepository = companyProfileRepository;
        this.currentAccess = currentAccess;
    }

    /** Deny access to another employee's payroll when the caller is scoped to their own. */
    private void assertPayrollAccess(Long employeeId) {
        if (currentAccess.hasAnyAuthority(PAYROLL_ALL_SCOPE)) {
            return;
        }
        Long ownId = currentAccess.employeeId();
        if (ownId == null || !ownId.equals(employeeId)) {
            throw ApiException.forbidden("You can only access your own payroll");
        }
    }

    @Override
    @Transactional
    public SalaryStructureResponse addRevision(SalaryStructureRequest r) {
        Employee employee = loadEmployee(r.employeeId());
        SalaryStructure structure = new SalaryStructure();
        structure.setEmployee(employee);
        structure.setEffectiveFrom(r.effectiveFrom());
        structure.setBasic(zeroIfNull(r.basic()));
        structure.setHra(zeroIfNull(r.hra()));
        structure.setSpecialAllowance(zeroIfNull(r.specialAllowance()));
        structure.setBonus(zeroIfNull(r.bonus()));
        structure.setIncentives(zeroIfNull(r.incentives()));
        structure.setOtherAllowances(zeroIfNull(r.otherAllowances()));
        structure.setGrossMonthly(gross(structure.getBasic(), structure.getHra(), structure.getSpecialAllowance(),
                structure.getBonus(), structure.getIncentives(), structure.getOtherAllowances()));
        structure.setRemarks(r.remarks());
        SalaryStructure saved = salaryStructureRepository.save(structure);

        // Keep the employee's current salary snapshot in sync when this revision is already effective.
        if (!r.effectiveFrom().isAfter(LocalDate.now())) {
            SalaryInfo salary = employee.getSalary() != null ? employee.getSalary() : new SalaryInfo();
            salary.setBasicSalary(saved.getBasic());
            salary.setHra(saved.getHra());
            salary.setSpecialAllowance(saved.getSpecialAllowance());
            salary.setBonus(saved.getBonus());
            salary.setIncentives(saved.getIncentives());
            salary.setOtherAllowances(saved.getOtherAllowances());
            employee.setSalary(salary);
        }
        return toStructure(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> history(Long employeeId) {
        assertPayrollAccess(employeeId);
        return salaryStructureRepository.findByEmployee_IdAndDeletedFalseOrderByEffectiveFromDesc(employeeId)
                .stream().map(this::toStructure).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryStructureResponse current(Long employeeId) {
        assertPayrollAccess(employeeId);
        return salaryStructureRepository
                .findTopByEmployee_IdAndEffectiveFromLessThanEqualAndDeletedFalseOrderByEffectiveFromDesc(
                        employeeId, LocalDate.now())
                .map(this::toStructure)
                .orElseThrow(() -> new ResourceNotFoundException("Current salary structure", "employeeId", employeeId));
    }

    @Override
    @Transactional
    public PayslipResponse generatePayslip(GeneratePayslipRequest r) {
        Employee employee = loadEmployee(r.employeeId());
        if (payslipRepository.existsByEmployee_IdAndPeriodYearAndPeriodMonthAndDeletedFalse(
                employee.getId(), r.year(), r.month())) {
            throw ApiException.conflict("A payslip already exists for this employee and period");
        }

        LocalDate periodEnd = LocalDate.of(r.year(), r.month(), 1)
                .withDayOfMonth(LocalDate.of(r.year(), r.month(), 1).lengthOfMonth());
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setPeriodYear(r.year());
        payslip.setPeriodMonth(r.month());
        applyComponents(payslip, employee, periodEnd);
        payslip.setGrossPay(gross(payslip.getBasic(), payslip.getHra(), payslip.getSpecialAllowance(),
                payslip.getBonus(), payslip.getIncentives(), payslip.getOtherAllowances()));
        payslip.setNetPay(payslip.getGrossPay()); // no statutory deductions
        payslip.setGeneratedAt(OffsetDateTime.now());
        payslip.setStatus(PayslipStatus.GENERATED);
        Payslip saved = payslipRepository.save(payslip);

        byte[] pdf = payslipPdfGenerator.generate(saved, employee.getFullName(), employee.getEmployeeCode(),
                companyName());
        String filename = "payslip-" + r.year() + "-" + String.format("%02d", r.month()) + ".pdf";
        saved.setStorageKey(storageService.store("payslips/" + employee.getId(), filename, pdf));

        String recipientEmail = recipientEmail(employee);
        if (recipientEmail != null) {
            emailService.sendWithAttachment(recipientEmail, "Payslip for " + r.month() + "/" + r.year(),
                    "Please find attached your payslip for " + r.month() + "/" + r.year() + ".", filename, pdf);
            saved.setStatus(PayslipStatus.EMAILED);
        }
        notificationService.notify(employee.getUser(), NotificationChannel.IN_APP, "Payslip available",
                "Your payslip for " + r.month() + "/" + r.year() + " is ready.", "PAYSLIP", saved.getId());
        return toPayslip(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PayslipResponse> listPayslips(Long employeeId, Pageable pageable) {
        assertPayrollAccess(employeeId);
        return PageResponse.of(
                payslipRepository.findByEmployee_IdAndDeletedFalseOrderByPeriodYearDescPeriodMonthDesc(employeeId, pageable),
                this::toPayslip);
    }

    @Override
    @Transactional(readOnly = true)
    public PayslipResponse getPayslip(Long id) {
        Payslip payslip = loadPayslip(id);
        assertPayrollAccess(payslip.getEmployee().getId());
        return toPayslip(payslip);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDownload downloadPayslip(Long id) {
        Payslip payslip = loadPayslip(id);
        assertPayrollAccess(payslip.getEmployee().getId());
        if (payslip.getStorageKey() == null) {
            throw new ResourceNotFoundException("Payslip PDF", "id", id);
        }
        String filename = "payslip-" + payslip.getPeriodYear() + "-"
                + String.format("%02d", payslip.getPeriodMonth()) + ".pdf";
        return new DocumentDownload(storageService.load(payslip.getStorageKey()), filename, "application/pdf");
    }

    // --- helpers ---------------------------------------------------------------------------------

    private void applyComponents(Payslip payslip, Employee employee, LocalDate asOf) {
        SalaryStructure structure = salaryStructureRepository
                .findTopByEmployee_IdAndEffectiveFromLessThanEqualAndDeletedFalseOrderByEffectiveFromDesc(
                        employee.getId(), asOf)
                .orElse(null);
        if (structure != null) {
            payslip.setBasic(structure.getBasic());
            payslip.setHra(structure.getHra());
            payslip.setSpecialAllowance(structure.getSpecialAllowance());
            payslip.setBonus(structure.getBonus());
            payslip.setIncentives(structure.getIncentives());
            payslip.setOtherAllowances(structure.getOtherAllowances());
        } else {
            SalaryInfo salary = employee.getSalary();
            payslip.setBasic(zeroIfNull(salary != null ? salary.getBasicSalary() : null));
            payslip.setHra(zeroIfNull(salary != null ? salary.getHra() : null));
            payslip.setSpecialAllowance(zeroIfNull(salary != null ? salary.getSpecialAllowance() : null));
            payslip.setBonus(zeroIfNull(salary != null ? salary.getBonus() : null));
            payslip.setIncentives(zeroIfNull(salary != null ? salary.getIncentives() : null));
            payslip.setOtherAllowances(zeroIfNull(salary != null ? salary.getOtherAllowances() : null));
        }
    }

    private Employee loadEmployee(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + id));
    }

    private Payslip loadPayslip(Long id) {
        return payslipRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", id));
    }

    private String recipientEmail(Employee employee) {
        if (employee.getContact() != null && employee.getContact().getOfficialEmail() != null
                && !employee.getContact().getOfficialEmail().isBlank()) {
            return employee.getContact().getOfficialEmail();
        }
        return employee.getUser() != null ? employee.getUser().getEmail() : null;
    }

    private String companyName() {
        return companyProfileRepository.findTopByOrderByIdAsc()
                .map(c -> c.getDisplayName()).orElse("Company");
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal gross(BigDecimal... components) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal component : components) {
            sum = sum.add(zeroIfNull(component));
        }
        return sum;
    }

    private SalaryStructureResponse toStructure(SalaryStructure s) {
        return new SalaryStructureResponse(s.getId(), s.getEmployee().getId(), s.getEffectiveFrom(), s.getBasic(),
                s.getHra(), s.getSpecialAllowance(), s.getBonus(), s.getIncentives(), s.getOtherAllowances(),
                s.getGrossMonthly(), s.getRemarks());
    }

    private PayslipResponse toPayslip(Payslip p) {
        Employee e = p.getEmployee();
        return new PayslipResponse(p.getId(), e.getId(), e.getFullName(), p.getPeriodYear(), p.getPeriodMonth(),
                p.getBasic(), p.getHra(), p.getSpecialAllowance(), p.getBonus(), p.getIncentives(),
                p.getOtherAllowances(), p.getGrossPay(), p.getNetPay(), p.getStatus(), p.getGeneratedAt());
    }
}
