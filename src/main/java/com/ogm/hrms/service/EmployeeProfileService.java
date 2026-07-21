package com.ogm.hrms.service;

import com.ogm.hrms.dto.employee.EducationRequest;
import com.ogm.hrms.dto.employee.EducationResponse;
import com.ogm.hrms.dto.employee.EmergencyContactRequest;
import com.ogm.hrms.dto.employee.EmergencyContactResponse;
import com.ogm.hrms.dto.employee.ExperienceRequest;
import com.ogm.hrms.dto.employee.ExperienceResponse;
import com.ogm.hrms.dto.employee.FamilyMemberRequest;
import com.ogm.hrms.dto.employee.FamilyMemberResponse;
import com.ogm.hrms.dto.employee.TimelineEventRequest;
import com.ogm.hrms.dto.employee.TimelineEventResponse;

import java.util.List;

/**
 * Manages an employee's child collections (emergency contacts, family, education, experience) and
 * the append-only timeline. All operations are scoped to a specific employee id.
 */
public interface EmployeeProfileService {

    EmergencyContactResponse addEmergencyContact(Long employeeId, EmergencyContactRequest request);

    List<EmergencyContactResponse> listEmergencyContacts(Long employeeId);

    EmergencyContactResponse updateEmergencyContact(Long employeeId, Long id, EmergencyContactRequest request);

    void deleteEmergencyContact(Long employeeId, Long id);

    FamilyMemberResponse addFamilyMember(Long employeeId, FamilyMemberRequest request);

    List<FamilyMemberResponse> listFamilyMembers(Long employeeId);

    FamilyMemberResponse updateFamilyMember(Long employeeId, Long id, FamilyMemberRequest request);

    void deleteFamilyMember(Long employeeId, Long id);

    EducationResponse addEducation(Long employeeId, EducationRequest request);

    List<EducationResponse> listEducation(Long employeeId);

    EducationResponse updateEducation(Long employeeId, Long id, EducationRequest request);

    void deleteEducation(Long employeeId, Long id);

    ExperienceResponse addExperience(Long employeeId, ExperienceRequest request);

    List<ExperienceResponse> listExperience(Long employeeId);

    ExperienceResponse updateExperience(Long employeeId, Long id, ExperienceRequest request);

    void deleteExperience(Long employeeId, Long id);

    TimelineEventResponse addTimelineEvent(Long employeeId, TimelineEventRequest request);

    List<TimelineEventResponse> listTimeline(Long employeeId);

    void deleteTimelineEvent(Long employeeId, Long id);
}
