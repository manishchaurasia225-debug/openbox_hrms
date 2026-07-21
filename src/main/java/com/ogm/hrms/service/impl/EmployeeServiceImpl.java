package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.employee.EmployeeRequest;
import com.ogm.hrms.dto.employee.EmployeeResponse;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.entity.embed.BankDetails;
import com.ogm.hrms.entity.embed.ContactInfo;
import com.ogm.hrms.entity.embed.GovernmentIds;
import com.ogm.hrms.entity.embed.SalaryInfo;
import com.ogm.hrms.entity.embed.SocialProfiles;
import com.ogm.hrms.enums.EmploymentStatus;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.mapper.EmployeeMapper;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.repository.DesignationRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.EmploymentTypeRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.service.EmployeeService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Default {@link EmployeeService}. Enforces unique employee code, resolves and validates the
 * organization master references (department/designation/employment type), and guarantees an at-most
 * one-to-one employee↔user link. Removed fields (per project-rules.md) are absent by construction.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final EmploymentTypeRepository employmentTypeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository,
                               DesignationRepository designationRepository,
                               EmploymentTypeRepository employmentTypeRepository, UserRepository userRepository,
                               EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.employmentTypeRepository = employmentTypeRepository;
        this.userRepository = userRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmployeeCodeIgnoreCase(request.employeeCode().trim())) {
            throw ApiException.conflict("An employee with this code already exists");
        }
        Employee employee = new Employee();
        apply(employee, request, null);
        return employeeMapper.toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(Pageable pageable) {
        return PageResponse.of(employeeRepository.findByDeletedFalse(pageable), employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> search(String query, Pageable pageable) {
        String q = query != null ? query.trim() : "";
        return PageResponse.of(employeeRepository.search(q, pageable), employeeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse get(Long id) {
        return employeeMapper.toResponse(load(id));
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = load(id);
        if (employeeRepository.existsByEmployeeCodeIgnoreCaseAndIdNot(request.employeeCode().trim(), id)) {
            throw ApiException.conflict("An employee with this code already exists");
        }
        apply(employee, request, id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = load(id);
        employee.setDeleted(true);
        employee.setDeletedAt(OffsetDateTime.now());
    }

    private Employee load(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    // --- mapping ---------------------------------------------------------------------------------

    private void apply(Employee e, EmployeeRequest r, Long existingId) {
        e.setEmployeeCode(r.employeeCode().trim());
        e.setFullName(r.fullName().trim());
        e.setGender(r.gender());
        e.setDateOfBirth(r.dateOfBirth());
        e.setBloodGroup(r.bloodGroup());
        e.setNationality(r.nationality());
        e.setMaritalStatus(r.maritalStatus());
        e.setPhotoUrl(r.photoUrl());
        applyContact(e, r.contact());
        applyEmployment(e, r.employment());
        applySalary(e, r.salary());
        applyBank(e, r.bank());
        applyGovernmentIds(e, r.governmentIds());
        applySocial(e, r.social());
        applyUser(e, r.userId(), existingId);
    }

    private void applyContact(Employee e, EmployeeRequest.Contact c) {
        ContactInfo ci = e.getContact() != null ? e.getContact() : new ContactInfo();
        if (c != null) {
            ci.setMobile(c.mobile());
            ci.setPersonalEmail(c.personalEmail());
            ci.setOfficialEmail(c.officialEmail());
            ci.setCurrentAddress(c.currentAddress());
            ci.setPermanentAddress(c.permanentAddress());
        }
        e.setContact(ci);
    }

    private void applyEmployment(Employee e, EmployeeRequest.Employment em) {
        if (em == null) {
            if (e.getEmploymentStatus() == null) {
                e.setEmploymentStatus(EmploymentStatus.ACTIVE);
            }
            return;
        }
        e.setDepartment(em.departmentId() == null ? null
                : departmentRepository.findByIdAndDeletedFalse(em.departmentId())
                .orElseThrow(() -> ApiException.badRequest("Unknown department: " + em.departmentId())));
        e.setDesignation(em.designationId() == null ? null
                : designationRepository.findByIdAndDeletedFalse(em.designationId())
                .orElseThrow(() -> ApiException.badRequest("Unknown designation: " + em.designationId())));
        e.setEmploymentType(em.employmentTypeId() == null ? null
                : employmentTypeRepository.findByIdAndDeletedFalse(em.employmentTypeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown employment type: " + em.employmentTypeId())));
        e.setJoiningDate(em.joiningDate());
        e.setEndDate(em.endDate());
        e.setNoticePeriodDays(em.noticePeriodDays());
        EmploymentStatus status = em.employmentStatus() != null ? em.employmentStatus()
                : (e.getEmploymentStatus() != null ? e.getEmploymentStatus() : EmploymentStatus.ACTIVE);
        e.setEmploymentStatus(status);
    }

    private void applySalary(Employee e, EmployeeRequest.Salary s) {
        SalaryInfo si = e.getSalary() != null ? e.getSalary() : new SalaryInfo();
        if (s != null) {
            si.setBasicSalary(s.basicSalary());
            si.setHra(s.hra());
            si.setSpecialAllowance(s.specialAllowance());
            si.setBonus(s.bonus());
            si.setIncentives(s.incentives());
            si.setOtherAllowances(s.otherAllowances());
        }
        e.setSalary(si);
    }

    private void applyBank(Employee e, EmployeeRequest.Bank b) {
        BankDetails bd = e.getBankDetails() != null ? e.getBankDetails() : new BankDetails();
        if (b != null) {
            bd.setBankName(b.bankName());
            bd.setAccountNumber(b.accountNumber());
            bd.setIfscCode(b.ifscCode());
            bd.setAccountHolderName(b.accountHolderName());
        }
        e.setBankDetails(bd);
    }

    private void applyGovernmentIds(Employee e, EmployeeRequest.GovernmentIds g) {
        GovernmentIds gi = e.getGovernmentIds() != null ? e.getGovernmentIds() : new GovernmentIds();
        if (g != null) {
            gi.setPan(g.pan());
            gi.setAadhaar(g.aadhaar());
            gi.setPassport(g.passport());
            gi.setDrivingLicense(g.drivingLicense());
        }
        e.setGovernmentIds(gi);
    }

    private void applySocial(Employee e, EmployeeRequest.Social soc) {
        SocialProfiles sp = e.getSocialProfiles() != null ? e.getSocialProfiles() : new SocialProfiles();
        if (soc != null) {
            sp.setLinkedinUrl(soc.linkedinUrl());
            sp.setInstagramUrl(soc.instagramUrl());
            sp.setFacebookUrl(soc.facebookUrl());
            sp.setXUrl(soc.xUrl());
        }
        e.setSocialProfiles(sp);
    }

    private void applyUser(Employee e, Long userId, Long existingId) {
        if (userId == null) {
            e.setUser(null);
            return;
        }
        boolean linkedElsewhere = existingId == null
                ? employeeRepository.existsByUser_Id(userId)
                : employeeRepository.existsByUser_IdAndIdNot(userId, existingId);
        if (linkedElsewhere) {
            throw ApiException.conflict("This user is already linked to another employee");
        }
        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> ApiException.badRequest("Unknown user: " + userId));
        e.setUser(user);
    }
}
