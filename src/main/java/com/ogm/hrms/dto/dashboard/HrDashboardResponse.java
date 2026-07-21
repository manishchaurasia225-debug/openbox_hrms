package com.ogm.hrms.dto.dashboard;

import java.util.List;

/** Aggregated HR dashboard widgets. */
public record HrDashboardResponse(
        long totalEmployees,
        long totalDepartments,
        long presentToday,
        long onLeaveToday,
        long pendingLeaveApprovals,
        long pendingWfhApprovals,
        long newJoinersLast30Days,
        List<PersonDate> upcomingBirthdays,
        List<PersonDate> upcomingAnniversaries,
        List<CountEntry> departmentDistribution,
        List<CountEntry> genderDistribution
) {
}
