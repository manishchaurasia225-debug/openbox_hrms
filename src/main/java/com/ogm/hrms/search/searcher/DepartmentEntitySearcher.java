package com.ogm.hrms.search.searcher;

import com.ogm.hrms.enums.SearchType;
import com.ogm.hrms.repository.DepartmentRepository;
import com.ogm.hrms.search.EntitySearcher;
import com.ogm.hrms.search.SearchHit;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Global-search source for departments; requires DEPARTMENT:VIEW. */
@Component
public class DepartmentEntitySearcher implements EntitySearcher {

    private final DepartmentRepository departmentRepository;

    public DepartmentEntitySearcher(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public SearchType type() {
        return SearchType.DEPARTMENT;
    }

    @Override
    public String requiredAuthority() {
        return "DEPARTMENT:VIEW";
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchHit> search(String query, int limit) {
        return departmentRepository.search(query, PageRequest.of(0, limit)).stream()
                .map(d -> new SearchHit(SearchType.DEPARTMENT, d.getId(), d.getName(), d.getCode()))
                .toList();
    }
}
