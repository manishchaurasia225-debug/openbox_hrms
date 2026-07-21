package com.ogm.hrms.service.impl;

import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.lifecycle.AddLifecycleTaskRequest;
import com.ogm.hrms.dto.lifecycle.InitiateLifecycleRequest;
import com.ogm.hrms.dto.lifecycle.LifecycleCaseResponse;
import com.ogm.hrms.dto.lifecycle.LifecycleTaskResponse;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.entity.LifecycleCase;
import com.ogm.hrms.entity.LifecycleTask;
import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.exception.ResourceNotFoundException;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.LifecycleCaseRepository;
import com.ogm.hrms.repository.LifecycleTaskRepository;
import com.ogm.hrms.service.LifecycleService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Default {@link LifecycleService}. Initiating a case seeds a type-specific checklist; completing
 * tasks advances the case to IN_PROGRESS and finally COMPLETED when every task is done.
 */
@Service
public class LifecycleServiceImpl implements LifecycleService {

    private static final List<String> ONBOARDING_TASKS = List.of(
            "Send welcome email", "Collect joining documents", "IT & workspace setup",
            "Orientation", "Assign reporting manager & buddy");
    private static final List<String> OFFBOARDING_TASKS = List.of(
            "Acknowledge resignation", "Knowledge transfer", "Asset clearance",
            "Finance clearance", "Issue relieving letter", "Hand over final documents");

    private final LifecycleCaseRepository caseRepository;
    private final LifecycleTaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;

    public LifecycleServiceImpl(LifecycleCaseRepository caseRepository, LifecycleTaskRepository taskRepository,
                                EmployeeRepository employeeRepository) {
        this.caseRepository = caseRepository;
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LifecycleCaseResponse initiate(InitiateLifecycleRequest r) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(r.employeeId())
                .orElseThrow(() -> ApiException.badRequest("Unknown employee: " + r.employeeId()));
        LifecycleCase lifecycleCase = new LifecycleCase();
        lifecycleCase.setEmployee(employee);
        lifecycleCase.setType(r.type());
        lifecycleCase.setStatus(LifecycleStatus.INITIATED);
        lifecycleCase.setInitiatedDate(LocalDate.now());
        lifecycleCase.setRemarks(r.remarks());

        List<String> titles = r.type() == LifecycleType.ONBOARDING ? ONBOARDING_TASKS : OFFBOARDING_TASKS;
        int sequence = 1;
        for (String title : titles) {
            LifecycleTask task = new LifecycleTask();
            task.setTitle(title);
            task.setSequence(sequence++);
            lifecycleCase.addTask(task);
        }
        return toResponse(caseRepository.save(lifecycleCase));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LifecycleCaseResponse> list(Long employeeId, LifecycleType type, LifecycleStatus status,
                                                    Pageable pageable) {
        if (employeeId != null) {
            return PageResponse.of(caseRepository.findByEmployee_Id(employeeId, pageable), this::toResponse);
        }
        if (type != null) {
            return PageResponse.of(caseRepository.findByType(type, pageable), this::toResponse);
        }
        if (status != null) {
            return PageResponse.of(caseRepository.findByStatus(status, pageable), this::toResponse);
        }
        return PageResponse.of(caseRepository.findAll(pageable), this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LifecycleCaseResponse get(Long id) {
        return toResponse(load(id));
    }

    @Override
    @Transactional
    public LifecycleCaseResponse addTask(Long caseId, AddLifecycleTaskRequest r) {
        LifecycleCase lifecycleCase = load(caseId);
        assertNotClosed(lifecycleCase);
        int nextSequence = lifecycleCase.getTasks().stream().mapToInt(LifecycleTask::getSequence).max().orElse(0) + 1;
        LifecycleTask task = new LifecycleTask();
        task.setTitle(r.title().trim());
        task.setNotes(r.notes());
        task.setSequence(nextSequence);
        lifecycleCase.addTask(task);
        recomputeStatus(lifecycleCase);
        return toResponse(lifecycleCase);
    }

    @Override
    @Transactional
    public LifecycleCaseResponse completeTask(Long caseId, Long taskId, String notes) {
        LifecycleCase lifecycleCase = load(caseId);
        assertNotClosed(lifecycleCase);
        LifecycleTask task = taskRepository.findByIdAndLifecycleCase_IdAndDeletedFalse(taskId, caseId)
                .orElseThrow(() -> new ResourceNotFoundException("LifecycleTask", "id", taskId));
        task.setCompleted(true);
        task.setCompletedAt(OffsetDateTime.now());
        if (notes != null) {
            task.setNotes(notes);
        }
        recomputeStatus(lifecycleCase);
        return toResponse(lifecycleCase);
    }

    @Override
    @Transactional
    public LifecycleCaseResponse cancel(Long caseId) {
        LifecycleCase lifecycleCase = load(caseId);
        if (lifecycleCase.getStatus() == LifecycleStatus.COMPLETED) {
            throw ApiException.badRequest("A completed case cannot be cancelled");
        }
        lifecycleCase.setStatus(LifecycleStatus.CANCELLED);
        return toResponse(lifecycleCase);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private LifecycleCase load(Long id) {
        return caseRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LifecycleCase", "id", id));
    }

    private void assertNotClosed(LifecycleCase lifecycleCase) {
        if (lifecycleCase.getStatus() == LifecycleStatus.COMPLETED
                || lifecycleCase.getStatus() == LifecycleStatus.CANCELLED) {
            throw ApiException.badRequest("This case is closed");
        }
    }

    private void recomputeStatus(LifecycleCase lifecycleCase) {
        List<LifecycleTask> tasks = lifecycleCase.getTasks();
        boolean allDone = !tasks.isEmpty() && tasks.stream().allMatch(LifecycleTask::isCompleted);
        if (allDone) {
            lifecycleCase.setStatus(LifecycleStatus.COMPLETED);
            lifecycleCase.setCompletedDate(LocalDate.now());
        } else {
            lifecycleCase.setStatus(LifecycleStatus.IN_PROGRESS);
            lifecycleCase.setCompletedDate(null);
        }
    }

    private LifecycleCaseResponse toResponse(LifecycleCase c) {
        Employee e = c.getEmployee();
        List<LifecycleTaskResponse> tasks = c.getTasks().stream()
                .map(t -> new LifecycleTaskResponse(t.getId(), t.getTitle(), t.getSequence(), t.isCompleted(),
                        t.getCompletedAt(), t.getNotes()))
                .toList();
        return new LifecycleCaseResponse(c.getId(), e.getId(), e.getFullName(), c.getType(), c.getStatus(),
                c.getInitiatedDate(), c.getCompletedDate(), c.getRemarks(), tasks);
    }
}
