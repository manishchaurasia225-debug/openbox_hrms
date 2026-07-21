package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** A single checklist item within a {@link LifecycleCase}. */
@Entity
@Table(name = "lifecycle_tasks",
        indexes = @Index(name = "idx_lifecycle_tasks_case", columnList = "lifecycle_case_id"))
@Getter
@Setter
@NoArgsConstructor
public class LifecycleTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lifecycle_case_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lifecycle_tasks_case"))
    private LifecycleCase lifecycleCase;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "sequence", nullable = false)
    private int sequence;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "notes", length = 300)
    private String notes;
}
