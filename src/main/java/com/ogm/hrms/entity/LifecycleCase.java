package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.LifecycleStatus;
import com.ogm.hrms.enums.LifecycleType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * An onboarding or offboarding case for an employee, driving a checklist of {@link LifecycleTask}s.
 * The case completes when all tasks are done. Managed under the {@code EMPLOYEE} RBAC module.
 */
@Entity
@Table(name = "lifecycle_cases", indexes = {
        @Index(name = "idx_lifecycle_employee", columnList = "employee_id"),
        @Index(name = "idx_lifecycle_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
public class LifecycleCase extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lifecycle_employee"))
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private LifecycleType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LifecycleStatus status = LifecycleStatus.INITIATED;

    @Column(name = "initiated_date", nullable = false)
    private LocalDate initiatedDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @OneToMany(mappedBy = "lifecycleCase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence asc, id asc")
    private List<LifecycleTask> tasks = new ArrayList<>();

    public void addTask(LifecycleTask task) {
        task.setLifecycleCase(this);
        this.tasks.add(task);
    }
}
