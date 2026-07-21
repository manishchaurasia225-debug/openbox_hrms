package com.ogm.hrms.entity.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Employee social profile links (embedded). Per project-rules.md, GitHub and Portfolio links are
 * intentionally excluded (removed features); LinkedIn, Instagram, Facebook, and X are kept.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class SocialProfiles {

    @Column(name = "linkedin_url", length = 200)
    private String linkedinUrl;

    @Column(name = "instagram_url", length = 200)
    private String instagramUrl;

    @Column(name = "facebook_url", length = 200)
    private String facebookUrl;

    @Column(name = "x_url", length = 200)
    private String xUrl;
}
