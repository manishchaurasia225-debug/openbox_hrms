package com.ogm.hrms.enums;

/**
 * The category of a day's attendance, per project-rules.md. Location-based types are Wi-Fi/IP driven
 * (no GPS/geolocation).
 */
public enum AttendanceType {
    OFFICE,
    WORK_FROM_HOME,
    CLIENT_VISIT,
    BUSINESS_TRAVEL,
    CASUAL_LEAVE,
    SICK_LEAVE,
    EARNED_LEAVE,
    HALF_DAY,
    EARLY_DEPARTURE,
    COMP_OFF,
    HOLIDAY,
    WEEKEND,
    ABSENT
}
