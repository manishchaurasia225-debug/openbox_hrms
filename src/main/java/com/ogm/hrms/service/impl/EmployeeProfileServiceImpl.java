package com.ogm.hrms.service.impl;

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
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.EmployeeEducation;
import com.ogm.hrms.entity.EmployeeExperience;
import com.ogm.hrms.entity.EmployeeTimelineEvent;
import com.ogm.hrms.entity.EmergencyContact;
import com.ogm.hrms.entity.FamilyMember;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.EmergencyContactRepository;
import com.ogm.hrms.repository.EmployeeEducationRepository;
import com.ogm.hrms.repository.EmployeeExperienceRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.EmployeeTimelineEventRepository;
import com.ogm.hrms.repository.FamilyMemberRepository;
import com.ogm.hrms.service.EmployeeProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Default {@link EmployeeProfileService}. Every operation validates the parent employee exists. */
@Service
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeRepository employeeRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final EmployeeEducationRepository educationRepository;
    private final EmployeeExperienceRepository experienceRepository;
    private final EmployeeTimelineEventRepository timelineRepository;

    public EmployeeProfileServiceImpl(EmployeeRepository employeeRepository,
                                      EmergencyContactRepository emergencyContactRepository,
                                      FamilyMemberRepository familyMemberRepository,
                                      EmployeeEducationRepository educationRepository,
                                      EmployeeExperienceRepository experienceRepository,
                                      EmployeeTimelineEventRepository timelineRepository) {
        this.employeeRepository = employeeRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.timelineRepository = timelineRepository;
    }

    // --- Emergency contacts ----------------------------------------------------------------------

    @Override
    @Transactional
    public EmergencyContactResponse addEmergencyContact(Long employeeId, EmergencyContactRequest r) {
        EmergencyContact c = new EmergencyContact();
        c.setEmployee(employee(employeeId));
        applyEmergency(c, r);
        return toEmergency(emergencyContactRepository.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyContactResponse> listEmergencyContacts(Long employeeId) {
        ensureEmployee(employeeId);
        return emergencyContactRepository.findByEmployee_IdAndDeletedFalseOrderByIdAsc(employeeId)
                .stream().map(this::toEmergency).toList();
    }

    @Override
    @Transactional
    public EmergencyContactResponse updateEmergencyContact(Long employeeId, Long id, EmergencyContactRequest r) {
        EmergencyContact c = emergencyContactRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", id));
        applyEmergency(c, r);
        return toEmergency(c);
    }

    @Override
    @Transactional
    public void deleteEmergencyContact(Long employeeId, Long id) {
        softDelete(emergencyContactRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmergencyContact", "id", id)));
    }

    private void applyEmergency(EmergencyContact c, EmergencyContactRequest r) {
        c.setName(r.name().trim());
        c.setRelationship(r.relationship());
        c.setPhone(r.phone());
        c.setAlternatePhone(r.alternatePhone());
        c.setEmail(r.email());
        c.setAddress(r.address());
    }

    private EmergencyContactResponse toEmergency(EmergencyContact c) {
        return new EmergencyContactResponse(c.getId(), c.getName(), c.getRelationship(), c.getPhone(),
                c.getAlternatePhone(), c.getEmail(), c.getAddress());
    }

    // --- Family members --------------------------------------------------------------------------

    @Override
    @Transactional
    public FamilyMemberResponse addFamilyMember(Long employeeId, FamilyMemberRequest r) {
        FamilyMember m = new FamilyMember();
        m.setEmployee(employee(employeeId));
        applyFamily(m, r);
        return toFamily(familyMemberRepository.save(m));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FamilyMemberResponse> listFamilyMembers(Long employeeId) {
        ensureEmployee(employeeId);
        return familyMemberRepository.findByEmployee_IdAndDeletedFalseOrderByIdAsc(employeeId)
                .stream().map(this::toFamily).toList();
    }

    @Override
    @Transactional
    public FamilyMemberResponse updateFamilyMember(Long employeeId, Long id, FamilyMemberRequest r) {
        FamilyMember m = familyMemberRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", "id", id));
        applyFamily(m, r);
        return toFamily(m);
    }

    @Override
    @Transactional
    public void deleteFamilyMember(Long employeeId, Long id) {
        softDelete(familyMemberRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("FamilyMember", "id", id)));
    }

    private void applyFamily(FamilyMember m, FamilyMemberRequest r) {
        m.setName(r.name().trim());
        m.setRelationship(r.relationship());
        m.setDateOfBirth(r.dateOfBirth());
        m.setOccupation(r.occupation());
        m.setPhone(r.phone());
        m.setDependent(r.dependent() != null && r.dependent());
    }

    private FamilyMemberResponse toFamily(FamilyMember m) {
        return new FamilyMemberResponse(m.getId(), m.getName(), m.getRelationship(), m.getDateOfBirth(),
                m.getOccupation(), m.getPhone(), m.isDependent());
    }

    // --- Education -------------------------------------------------------------------------------

    @Override
    @Transactional
    public EducationResponse addEducation(Long employeeId, EducationRequest r) {
        EmployeeEducation e = new EmployeeEducation();
        e.setEmployee(employee(employeeId));
        applyEducation(e, r);
        return toEducation(educationRepository.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EducationResponse> listEducation(Long employeeId) {
        ensureEmployee(employeeId);
        return educationRepository.findByEmployee_IdAndDeletedFalseOrderByIdAsc(employeeId)
                .stream().map(this::toEducation).toList();
    }

    @Override
    @Transactional
    public EducationResponse updateEducation(Long employeeId, Long id, EducationRequest r) {
        EmployeeEducation e = educationRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeEducation", "id", id));
        applyEducation(e, r);
        return toEducation(e);
    }

    @Override
    @Transactional
    public void deleteEducation(Long employeeId, Long id) {
        softDelete(educationRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeEducation", "id", id)));
    }

    private void applyEducation(EmployeeEducation e, EducationRequest r) {
        e.setInstitution(r.institution().trim());
        e.setDegree(r.degree());
        e.setFieldOfStudy(r.fieldOfStudy());
        e.setStartYear(r.startYear());
        e.setEndYear(r.endYear());
        e.setGrade(r.grade());
    }

    private EducationResponse toEducation(EmployeeEducation e) {
        return new EducationResponse(e.getId(), e.getInstitution(), e.getDegree(), e.getFieldOfStudy(),
                e.getStartYear(), e.getEndYear(), e.getGrade());
    }

    // --- Experience ------------------------------------------------------------------------------

    @Override
    @Transactional
    public ExperienceResponse addExperience(Long employeeId, ExperienceRequest r) {
        EmployeeExperience x = new EmployeeExperience();
        x.setEmployee(employee(employeeId));
        applyExperience(x, r);
        return toExperience(experienceRepository.save(x));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExperienceResponse> listExperience(Long employeeId) {
        ensureEmployee(employeeId);
        return experienceRepository.findByEmployee_IdAndDeletedFalseOrderByIdAsc(employeeId)
                .stream().map(this::toExperience).toList();
    }

    @Override
    @Transactional
    public ExperienceResponse updateExperience(Long employeeId, Long id, ExperienceRequest r) {
        EmployeeExperience x = experienceRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeExperience", "id", id));
        applyExperience(x, r);
        return toExperience(x);
    }

    @Override
    @Transactional
    public void deleteExperience(Long employeeId, Long id) {
        softDelete(experienceRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeExperience", "id", id)));
    }

    private void applyExperience(EmployeeExperience x, ExperienceRequest r) {
        x.setCompanyName(r.companyName().trim());
        x.setDesignation(r.designation());
        x.setFromDate(r.fromDate());
        x.setToDate(r.toDate());
        x.setDescription(r.description());
    }

    private ExperienceResponse toExperience(EmployeeExperience x) {
        return new ExperienceResponse(x.getId(), x.getCompanyName(), x.getDesignation(), x.getFromDate(),
                x.getToDate(), x.getDescription());
    }

    // --- Timeline (append-only) ------------------------------------------------------------------

    @Override
    @Transactional
    public TimelineEventResponse addTimelineEvent(Long employeeId, TimelineEventRequest r) {
        EmployeeTimelineEvent ev = new EmployeeTimelineEvent();
        ev.setEmployee(employee(employeeId));
        ev.setEventType(r.eventType());
        ev.setTitle(r.title().trim());
        ev.setDescription(r.description());
        ev.setEventDate(r.eventDate() != null ? r.eventDate() : LocalDate.now());
        return toTimeline(timelineRepository.save(ev));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimelineEventResponse> listTimeline(Long employeeId) {
        ensureEmployee(employeeId);
        return timelineRepository.findByEmployee_IdAndDeletedFalseOrderByEventDateDescIdDesc(employeeId)
                .stream().map(this::toTimeline).toList();
    }

    @Override
    @Transactional
    public void deleteTimelineEvent(Long employeeId, Long id) {
        softDelete(timelineRepository.findByIdAndEmployee_IdAndDeletedFalse(id, employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeTimelineEvent", "id", id)));
    }

    private TimelineEventResponse toTimeline(EmployeeTimelineEvent ev) {
        return new TimelineEventResponse(ev.getId(), ev.getEventType(), ev.getTitle(), ev.getDescription(),
                ev.getEventDate());
    }

    // --- shared ----------------------------------------------------------------------------------

    private Employee employee(Long employeeId) {
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
    }

    private void ensureEmployee(Long employeeId) {
        if (!employeeRepository.findByIdAndDeletedFalse(employeeId).isPresent()) {
            throw new ResourceNotFoundException("Employee", "id", employeeId);
        }
    }

    private void softDelete(com.ogm.hrms.common.BaseEntity entity) {
        entity.setDeleted(true);
        entity.setDeletedAt(OffsetDateTime.now());
    }
}
