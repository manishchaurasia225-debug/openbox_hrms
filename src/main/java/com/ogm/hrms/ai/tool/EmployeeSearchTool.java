package com.ogm.hrms.ai.tool;

import com.ogm.hrms.ai.AiTool;
import com.ogm.hrms.ai.AiToolRequest;
import com.ogm.hrms.ai.AiToolResult;
import com.ogm.hrms.common.PageResponse;
import com.ogm.hrms.dto.employee.EmployeeResponse;
import com.ogm.hrms.service.EmployeeService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/** AI tool: searches the employee directory. Wraps {@link EmployeeService}; requires EMPLOYEE:VIEW. */
@Component
public class EmployeeSearchTool implements AiTool {

    private static final int MAX_RESULTS = 10;

    private final EmployeeService employeeService;

    public EmployeeSearchTool(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    public String name() {
        return "employee_search";
    }

    @Override
    public String description() {
        return "Search employees by name, employee code, or official email. Parameter: q (search text).";
    }

    @Override
    public String requiredAuthority() {
        return "EMPLOYEE:VIEW";
    }

    @Override
    public AiToolResult execute(AiToolRequest request) {
        String q = request.param("q", "");
        PageResponse<EmployeeResponse> page = employeeService.search(q, PageRequest.of(0, MAX_RESULTS));
        List<Hit> hits = page.content().stream().map(this::toHit).toList();
        String summary = hits.isEmpty()
                ? "No employees found matching '" + q + "'."
                : "Found " + page.totalElements() + " employee(s) matching '" + q + "'"
                        + (page.totalElements() > hits.size() ? " (showing first " + hits.size() + ")" : "") + ".";
        return new AiToolResult(summary, hits);
    }

    private Hit toHit(EmployeeResponse e) {
        String department = e.employment() != null ? e.employment().departmentName() : null;
        String designation = e.employment() != null ? e.employment().designationName() : null;
        return new Hit(e.employeeCode(), e.fullName(), department, designation);
    }

    /** Compact, non-sensitive employee projection (no salary/bank details). */
    public record Hit(String code, String name, String department, String designation) {
    }
}
