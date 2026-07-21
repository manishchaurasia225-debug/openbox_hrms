package com.ogm.hrms.search;

import com.ogm.hrms.dto.search.GlobalSearchResponse;
import com.ogm.hrms.enums.SearchType;
import com.ogm.hrms.service.impl.GlobalSearchServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the global-search permission filter: a searcher whose required authority the caller
 * does not hold must be skipped entirely (no cross-module leakage). This is the enforcement that
 * cannot be shown over HTTP because every seeded role happens to hold VIEW on the master-data types.
 */
class GlobalSearchServiceImplTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsSearchersTheCallerIsNotAuthorizedFor() {
        EntitySearcher employeeSearcher = new StubSearcher(SearchType.EMPLOYEE, "EMPLOYEE:VIEW",
                List.of(new SearchHit(SearchType.EMPLOYEE, 1L, "Alice", "EMP-1")));
        EntitySearcher departmentSearcher = new StubSearcher(SearchType.DEPARTMENT, "DEPARTMENT:VIEW",
                List.of(new SearchHit(SearchType.DEPARTMENT, 2L, "Alpha", "ALPHA")));
        GlobalSearchServiceImpl service =
                new GlobalSearchServiceImpl(List.of(employeeSearcher, departmentSearcher));

        // Caller holds only EMPLOYEE:VIEW.
        authenticateWithAuthorities("EMPLOYEE:VIEW");

        GlobalSearchResponse response = service.search("al", 5);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).type()).isEqualTo(SearchType.EMPLOYEE);
    }

    @Test
    void returnsNothingForBlankQuery() {
        GlobalSearchServiceImpl service = new GlobalSearchServiceImpl(List.of(
                new StubSearcher(SearchType.EMPLOYEE, "EMPLOYEE:VIEW",
                        List.of(new SearchHit(SearchType.EMPLOYEE, 1L, "Alice", "EMP-1")))));
        authenticateWithAuthorities("EMPLOYEE:VIEW");

        assertThat(service.search("   ", 5).total()).isZero();
    }

    private void authenticateWithAuthorities(String... authorities) {
        var granted = java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("tester", null, granted));
    }

    private record StubSearcher(SearchType type, String authority, List<SearchHit> hits)
            implements EntitySearcher {

        @Override
        public SearchType type() {
            return type;
        }

        @Override
        public String requiredAuthority() {
            return authority;
        }

        @Override
        public List<SearchHit> search(String query, int limit) {
            return hits;
        }
    }
}
