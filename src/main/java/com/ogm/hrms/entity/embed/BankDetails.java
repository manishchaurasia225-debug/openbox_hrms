package com.ogm.hrms.entity.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Employee bank details for salary disbursement (embedded). */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class BankDetails {

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "bank_account_number", length = 40)
    private String accountNumber;

    @Column(name = "bank_ifsc", length = 20)
    private String ifscCode;

    @Column(name = "bank_account_holder", length = 120)
    private String accountHolderName;
}
