package com.ogm.hrms.dto.search;

import java.util.List;

/** Aggregated, permission-filtered global-search results grouped by entity type. */
public record GlobalSearchResponse(String query, int total, List<SearchGroupResponse> groups) {
}
