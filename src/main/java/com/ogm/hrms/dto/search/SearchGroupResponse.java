package com.ogm.hrms.dto.search;

import com.ogm.hrms.enums.SearchType;
import com.ogm.hrms.search.SearchHit;

import java.util.List;

/** Global-search results for a single entity type. */
public record SearchGroupResponse(SearchType type, int count, List<SearchHit> hits) {
}
