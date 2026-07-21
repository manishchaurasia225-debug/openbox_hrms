package com.ogm.hrms.service;

import com.ogm.hrms.dto.search.GlobalSearchResponse;

/**
 * Global Search (Module 22): a single query fanned out across entity types, returning typed,
 * permission-filtered results. A caller only sees entity types they are authorized to view.
 */
public interface GlobalSearchService {

    GlobalSearchResponse search(String query, int limit);
}
