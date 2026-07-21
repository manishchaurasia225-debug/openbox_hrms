package com.ogm.hrms.service.impl;

import com.ogm.hrms.dto.search.GlobalSearchResponse;
import com.ogm.hrms.dto.search.SearchGroupResponse;
import com.ogm.hrms.search.EntitySearcher;
import com.ogm.hrms.search.SearchHit;
import com.ogm.hrms.service.GlobalSearchService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default global search. Runs only the {@link EntitySearcher}s the caller is authorized for (each
 * gated by a module {@code VIEW} authority), so results never cross a permission boundary. Blank
 * queries and over-large limits are normalised.
 */
@Service
public class GlobalSearchServiceImpl implements GlobalSearchService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 25;

    private final List<EntitySearcher> searchers;

    public GlobalSearchServiceImpl(List<EntitySearcher> searchers) {
        this.searchers = searchers;
    }

    @Override
    public GlobalSearchResponse search(String query, int limit) {
        String q = query != null ? query.trim() : "";
        int perType = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        if (q.isEmpty()) {
            return new GlobalSearchResponse(q, 0, List.of());
        }

        Set<String> authorities = currentAuthorities();
        List<SearchGroupResponse> groups = new ArrayList<>();
        int total = 0;
        for (EntitySearcher searcher : searchers) {
            if (!authorities.contains(searcher.requiredAuthority())) {
                continue;
            }
            List<SearchHit> hits = searcher.search(q, perType);
            if (!hits.isEmpty()) {
                groups.add(new SearchGroupResponse(searcher.type(), hits.size(), hits));
                total += hits.size();
            }
        }
        return new GlobalSearchResponse(q, total, groups);
    }

    private Set<String> currentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
