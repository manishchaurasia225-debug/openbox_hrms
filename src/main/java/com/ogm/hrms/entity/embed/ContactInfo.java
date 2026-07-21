package com.ogm.hrms.entity.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Employee contact information (embedded in the employees table). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ContactInfo {

    @Column(name = "mobile", length = 30)
    private String mobile;

    @Column(name = "personal_email", length = 190)
    private String personalEmail;

    @Column(name = "official_email", length = 190)
    private String officialEmail;

    @Column(name = "current_address", length = 300)
    private String currentAddress;

    @Column(name = "permanent_address", length = 300)
    private String permanentAddress;
}
