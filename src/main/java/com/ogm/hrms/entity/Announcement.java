package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import com.ogm.hrms.enums.AnnouncementCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * A company announcement (news, event, policy, holiday, office closure, training). Optionally
 * targeted to a single department (null = whole company). Becomes visible on the feed once
 * {@code published} and within its {@code publishAt}/{@code expiresAt} window. Managed under the
 * {@code ANNOUNCEMENT} RBAC module.
 */
@Entity
@Table(name = "announcements", indexes = {
        @Index(name = "idx_announcements_published", columnList = "published"),
        @Index(name = "idx_announcements_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
public class Announcement extends BaseEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", length = 4000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private AnnouncementCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id", foreignKey = @ForeignKey(name = "fk_announcements_department"))
    private Department targetDepartment;

    @Column(name = "publish_at")
    private OffsetDateTime publishAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "published", nullable = false)
    private boolean published = false;

    @Column(name = "pinned", nullable = false)
    private boolean pinned = false;
}
