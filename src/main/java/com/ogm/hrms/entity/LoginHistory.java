package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * An append-only record of an authentication attempt (success or failure), supporting the
 * login-history and failed-login-tracking requirements. The attempt time is {@code created_at}
 * (from {@link BaseEntity}). {@code user} is null when the email did not resolve to an account.
 */
@Entity
@Table(name = "login_history", indexes = {
        @Index(name = "idx_login_history_user", columnList = "user_id"),
        @Index(name = "idx_login_history_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class LoginHistory extends BaseEntity {

    @Column(name = "email", nullable = false, length = 190)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @jakarta.persistence.ForeignKey(name = "fk_login_history_user"))
    private User user;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "failure_reason", length = 100)
    private String failureReason;
}
