package com.ogm.hrms.controller;

import com.ogm.hrms.common.ApiResponse;
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
import com.ogm.hrms.service.EmployeeProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Employee profile sub-resources (Module 4 pt2): emergency contacts, family, education, experience,
 * and the append-only timeline. Reads require {@code EMPLOYEE:VIEW}; mutations {@code EMPLOYEE:EDIT}.
 */
@Tag(name = "Employee Profile",
        description = "Employee profile sub-resources: emergency contacts, family, education, experience, and timeline.")
@RestController
@RequestMapping("/api/v1/employees/{employeeId}")
public class EmployeeProfileController {

    private final EmployeeProfileService profileService;

    public EmployeeProfileController(EmployeeProfileService profileService) {
        this.profileService = profileService;
    }

    // --- emergency contacts ---
    @Operation(summary = "Add emergency contact",
            description = "Adds an emergency contact to the employee profile. Requires EMPLOYEE:EDIT.")
    @PostMapping("/emergency-contacts")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<EmergencyContactResponse> addEmergencyContact(@PathVariable Long employeeId,
            @Valid @RequestBody EmergencyContactRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.addEmergencyContact(employeeId, request), "Created", http.getRequestURI());
    }

    @Operation(summary = "List emergency contacts",
            description = "Lists all emergency contacts for the employee. Requires EMPLOYEE:VIEW.")
    @GetMapping("/emergency-contacts")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<EmergencyContactResponse>> listEmergencyContacts(@PathVariable Long employeeId,
            HttpServletRequest http) {
        return ApiResponse.success(profileService.listEmergencyContacts(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update emergency contact",
            description = "Updates an existing emergency contact of the employee. Requires EMPLOYEE:EDIT.")
    @PutMapping("/emergency-contacts/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<EmergencyContactResponse> updateEmergencyContact(@PathVariable Long employeeId,
            @PathVariable Long id, @Valid @RequestBody EmergencyContactRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.updateEmergencyContact(employeeId, id, request), "Updated", http.getRequestURI());
    }

    @Operation(summary = "Delete emergency contact",
            description = "Removes an emergency contact from the employee profile. Requires EMPLOYEE:EDIT.")
    @DeleteMapping("/emergency-contacts/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<Void> deleteEmergencyContact(@PathVariable Long employeeId, @PathVariable Long id,
            HttpServletRequest http) {
        profileService.deleteEmergencyContact(employeeId, id);
        return ApiResponse.success(null, "Deleted", http.getRequestURI());
    }

    // --- family ---
    @Operation(summary = "Add family member",
            description = "Adds a family member to the employee profile. Requires EMPLOYEE:EDIT.")
    @PostMapping("/family")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<FamilyMemberResponse> addFamily(@PathVariable Long employeeId,
            @Valid @RequestBody FamilyMemberRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.addFamilyMember(employeeId, request), "Created", http.getRequestURI());
    }

    @Operation(summary = "List family members",
            description = "Lists all family members for the employee. Requires EMPLOYEE:VIEW.")
    @GetMapping("/family")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<FamilyMemberResponse>> listFamily(@PathVariable Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(profileService.listFamilyMembers(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update family member",
            description = "Updates an existing family member of the employee. Requires EMPLOYEE:EDIT.")
    @PutMapping("/family/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<FamilyMemberResponse> updateFamily(@PathVariable Long employeeId, @PathVariable Long id,
            @Valid @RequestBody FamilyMemberRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.updateFamilyMember(employeeId, id, request), "Updated", http.getRequestURI());
    }

    @Operation(summary = "Delete family member",
            description = "Removes a family member from the employee profile. Requires EMPLOYEE:EDIT.")
    @DeleteMapping("/family/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<Void> deleteFamily(@PathVariable Long employeeId, @PathVariable Long id, HttpServletRequest http) {
        profileService.deleteFamilyMember(employeeId, id);
        return ApiResponse.success(null, "Deleted", http.getRequestURI());
    }

    // --- education ---
    @Operation(summary = "Add education record",
            description = "Adds an education record to the employee profile. Requires EMPLOYEE:EDIT.")
    @PostMapping("/education")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<EducationResponse> addEducation(@PathVariable Long employeeId,
            @Valid @RequestBody EducationRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.addEducation(employeeId, request), "Created", http.getRequestURI());
    }

    @Operation(summary = "List education records",
            description = "Lists all education records for the employee. Requires EMPLOYEE:VIEW.")
    @GetMapping("/education")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<EducationResponse>> listEducation(@PathVariable Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(profileService.listEducation(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update education record",
            description = "Updates an existing education record of the employee. Requires EMPLOYEE:EDIT.")
    @PutMapping("/education/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<EducationResponse> updateEducation(@PathVariable Long employeeId, @PathVariable Long id,
            @Valid @RequestBody EducationRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.updateEducation(employeeId, id, request), "Updated", http.getRequestURI());
    }

    @Operation(summary = "Delete education record",
            description = "Removes an education record from the employee profile. Requires EMPLOYEE:EDIT.")
    @DeleteMapping("/education/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<Void> deleteEducation(@PathVariable Long employeeId, @PathVariable Long id, HttpServletRequest http) {
        profileService.deleteEducation(employeeId, id);
        return ApiResponse.success(null, "Deleted", http.getRequestURI());
    }

    // --- experience ---
    @Operation(summary = "Add experience record",
            description = "Adds a prior work experience record to the employee profile. Requires EMPLOYEE:EDIT.")
    @PostMapping("/experience")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<ExperienceResponse> addExperience(@PathVariable Long employeeId,
            @Valid @RequestBody ExperienceRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.addExperience(employeeId, request), "Created", http.getRequestURI());
    }

    @Operation(summary = "List experience records",
            description = "Lists all prior work experience records for the employee. Requires EMPLOYEE:VIEW.")
    @GetMapping("/experience")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<ExperienceResponse>> listExperience(@PathVariable Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(profileService.listExperience(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Update experience record",
            description = "Updates an existing work experience record of the employee. Requires EMPLOYEE:EDIT.")
    @PutMapping("/experience/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<ExperienceResponse> updateExperience(@PathVariable Long employeeId, @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.updateExperience(employeeId, id, request), "Updated", http.getRequestURI());
    }

    @Operation(summary = "Delete experience record",
            description = "Removes a work experience record from the employee profile. Requires EMPLOYEE:EDIT.")
    @DeleteMapping("/experience/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<Void> deleteExperience(@PathVariable Long employeeId, @PathVariable Long id, HttpServletRequest http) {
        profileService.deleteExperience(employeeId, id);
        return ApiResponse.success(null, "Deleted", http.getRequestURI());
    }

    // --- timeline (append-only: no update) ---
    @Operation(summary = "Add timeline event",
            description = "Appends an event to the employee's append-only timeline. Requires EMPLOYEE:EDIT.")
    @PostMapping("/timeline")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<TimelineEventResponse> addTimelineEvent(@PathVariable Long employeeId,
            @Valid @RequestBody TimelineEventRequest request, HttpServletRequest http) {
        return ApiResponse.success(profileService.addTimelineEvent(employeeId, request), "Created", http.getRequestURI());
    }

    @Operation(summary = "List timeline events",
            description = "Lists all events in the employee's append-only timeline. Requires EMPLOYEE:VIEW.")
    @GetMapping("/timeline")
    @PreAuthorize("hasAuthority('EMPLOYEE:VIEW')")
    public ApiResponse<List<TimelineEventResponse>> listTimeline(@PathVariable Long employeeId, HttpServletRequest http) {
        return ApiResponse.success(profileService.listTimeline(employeeId), "OK", http.getRequestURI());
    }

    @Operation(summary = "Delete timeline event",
            description = "Removes an event from the employee's timeline. Requires EMPLOYEE:EDIT.")
    @DeleteMapping("/timeline/{id}")
    @PreAuthorize("hasAuthority('EMPLOYEE:EDIT')")
    public ApiResponse<Void> deleteTimelineEvent(@PathVariable Long employeeId, @PathVariable Long id, HttpServletRequest http) {
        profileService.deleteTimelineEvent(employeeId, id);
        return ApiResponse.success(null, "Deleted", http.getRequestURI());
    }
}
