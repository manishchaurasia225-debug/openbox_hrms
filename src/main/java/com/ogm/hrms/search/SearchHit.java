package com.ogm.hrms.search;

import com.ogm.hrms.enums.SearchType;

/**
 * A single global-search result.
 *
 * @param type     the entity type this hit belongs to
 * @param id       the entity id (for navigation/deep-linking)
 * @param title    primary label (e.g. employee name, department name)
 * @param subtitle secondary context (e.g. employee code, department code)
 */
public record SearchHit(SearchType type, Long id, String title, String subtitle) {
}
