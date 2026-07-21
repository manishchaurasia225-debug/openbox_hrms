package com.ogm.hrms.search;

import com.ogm.hrms.enums.SearchType;

import java.util.List;

/**
 * Searches one entity type for the global search. Each searcher declares the {@code MODULE:ACTION}
 * authority a caller must hold; the aggregator runs only the searchers the caller is permitted to use,
 * so results a user cannot view are never returned (no cross-module leakage).
 */
public interface EntitySearcher {

    SearchType type();

    /** The authority a caller must hold for this entity type to be searched. */
    String requiredAuthority();

    List<SearchHit> search(String query, int limit);
}
