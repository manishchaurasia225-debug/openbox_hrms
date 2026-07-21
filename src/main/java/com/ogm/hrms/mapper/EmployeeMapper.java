package com.ogm.hrms.mapper;

import com.ogm.hrms.dto.employee.EmployeeResponse;
import com.ogm.hrms.entity.Department;
import com.ogm.hrms.entity.Designation;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.EmploymentType;
import com.ogm.hrms.entity.embed.BankDetails;
import com.ogm.hrms.entity.embed.ContactInfo;
import com.ogm.hrms.entity.embed.GovernmentIds;
import com.ogm.hrms.entity.embed.SalaryInfo;
import com.ogm.hrms.entity.embed.SocialProfiles;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Employee} entities to {@link EmployeeResponse} DTOs. Must run within a transaction so
 * the lazy department/designation/employment-type/user associations are available. Null-safe against
 * absent embeddables.
 */
@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee e) {
        ContactInfo contact = e.getContact() != null ? e.getContact() : new ContactInfo();
        SalaryInfo salary = e.getSalary() != null ? e.getSalary() : new SalaryInfo();
        BankDetails bank = e.getBankDetails() != null ? e.getBankDetails() : new BankDetails();
        GovernmentIds gov = e.getGovernmentIds() != null ? e.getGovernmentIds() : new GovernmentIds();
        SocialProfiles social = e.getSocialProfiles() != null ? e.getSocialProfiles() : new SocialProfiles();

        Department dept = e.getDepartment();
        Designation desig = e.getDesignation();
        EmploymentType type = e.getEmploymentType();

        return new EmployeeResponse(
                e.getId(),
                e.getEmployeeCode(),
                e.getFullName(),
                e.getGender(),
                e.getDateOfBirth(),
                e.getBloodGroup(),
                e.getNationality(),
                e.getMaritalStatus(),
                e.getPhotoUrl(),
                new EmployeeResponse.Contact(contact.getMobile(), contact.getPersonalEmail(),
                        contact.getOfficialEmail(), contact.getCurrentAddress(), contact.getPermanentAddress()),
                new EmployeeResponse.Employment(
                        dept != null ? dept.getId() : null, dept != null ? dept.getName() : null,
                        desig != null ? desig.getId() : null, desig != null ? desig.getName() : null,
                        type != null ? type.getId() : null, type != null ? type.getName() : null,
                        e.getJoiningDate(), e.getEndDate(), e.getNoticePeriodDays(), e.getEmploymentStatus()),
                new EmployeeResponse.Salary(salary.getBasicSalary(), salary.getHra(), salary.getSpecialAllowance(),
                        salary.getBonus(), salary.getIncentives(), salary.getOtherAllowances()),
                new EmployeeResponse.Bank(bank.getBankName(), bank.getAccountNumber(), bank.getIfscCode(),
                        bank.getAccountHolderName()),
                new EmployeeResponse.GovernmentIds(gov.getPan(), gov.getAadhaar(), gov.getPassport(),
                        gov.getDrivingLicense()),
                new EmployeeResponse.Social(social.getLinkedinUrl(), social.getInstagramUrl(),
                        social.getFacebookUrl(), social.getXUrl()),
                e.getUser() != null ? e.getUser().getId() : null,
                e.getCreatedAt(),
                e.getUpdatedAt());
    }
}
