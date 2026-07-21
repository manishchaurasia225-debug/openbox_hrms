package com.ogm.hrms.entity.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Government-issued identifiers (embedded). Per project-rules.md, PF/UAN/ESI/tax identifiers are
 * intentionally excluded (removed features).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class GovernmentIds {

    @Column(name = "pan", length = 20)
    private String pan;

    @Column(name = "aadhaar", length = 20)
    private String aadhaar;

    @Column(name = "passport", length = 20)
    private String passport;

    @Column(name = "driving_license", length = 30)
    private String drivingLicense;
}
