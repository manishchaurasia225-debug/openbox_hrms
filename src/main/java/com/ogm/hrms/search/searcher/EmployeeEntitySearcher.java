package com.ogm.hrms.search.searcher;

import com.ogm.hrms.enums.SearchType;
import com.ogm.hrms.search.EntitySearcher;
import com.ogm.hrms.search.SearchHit;
import com.ogm.hrms.service.EmployeeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** Global-search source for employees. Wraps {@link EmployeeService}; requires EMPLOYEE:VIEW. */
@Component
public class EmployeeEntitySearcher implements EntitySearcher {

    private final EmployeeService employeeService;

    public EmployeeEntitySearcher(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public SearchType type() {
        return SearchType.EMPLOYEE;
    }

    @Override
    public String requiredAuthority() {
        return "EMPLOYEE:VIEW";
    }

    @Override
    public List<SearchHit> search(String query, int limit) {
        return employeeService.search(query, PageRequest.of(0, limit)).content().stream()
                .map(e -> new SearchHit(SearchType.EMPLOYEE, e.id(), e.fullName(), e.employeeCode()))
                .toList();
    }
}
