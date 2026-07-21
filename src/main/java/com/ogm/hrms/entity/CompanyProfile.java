package com.ogm.hrms.entity;

import com.ogm.hrms.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The single company's profile (single-company platform — D-004; multi-tenancy would later make this
 * one row per tenant). Managed under the {@code COMPANY} RBAC module. A single row is seeded at
 * bootstrap and edited in place; the API never creates additional rows.
 */
@Entity
@Table(name = "company_profile")
@Getter
@Setter
@NoArgsConstructor
public class CompanyProfile extends BaseEntity {

    @Column(name = "legal_name", nullable = false, length = 200)
    private String legalName;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "registration_number", length = 60)
    private String registrationNumber;

    @Column(name = "email", length = 190)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "timezone", length = 60)
    private String timezone;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;
}
