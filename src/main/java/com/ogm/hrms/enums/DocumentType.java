package com.ogm.hrms.enums;

/**
 * Supported document categories. Per project-rules.md, removed document types (appointment letter,
 * education certificates, cancelled cheque, police verification, medical reports, custom documents)
 * are intentionally excluded.
 */
public enum DocumentType {
    RESUME,
    OFFER_LETTER,
    JOINING_LETTER,
    EXPERIENCE_LETTER,
    SALARY_SLIP,
    COMPANY_POLICY
}
