package com.ogm.hrms.enums;

/** How an attendance record was captured. Biometric is reserved for future integration. */
public enum AttendanceSource {
    WIFI_IP,
    MANUAL,
    CORRECTION,
    SYSTEM,
    BIOMETRIC
}
