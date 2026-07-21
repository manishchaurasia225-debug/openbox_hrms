package com.ogm.hrms.automation.evaluator;

import com.ogm.hrms.automation.AutomationEvaluator;
import com.ogm.hrms.automation.AutomationTarget;
import com.ogm.hrms.automation.AutomationVariables;
import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.Employee;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.DocumentType;
import com.ogm.hrms.repository.DocumentRepository;
import com.ogm.hrms.repository.EmployeeRepository;
import com.ogm.hrms.repository.SystemSettingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reminds active employees whose mandatory documents are missing. The required document set is
 * configuration, not code: it is read from the {@code automation.missing-documents.required-types}
 * system setting (comma-separated {@link DocumentType} names), falling back to a sensible default.
 * The missing types are exposed as the {@code missing} template variable.
 */
@Component
public class MissingDocumentsEvaluator implements AutomationEvaluator {

    private static final Logger log = LoggerFactory.getLogger(MissingDocumentsEvaluator.class);

    /** Setting key controlling which document types every employee must have on file. */
    static final String REQUIRED_TYPES_SETTING = "automation.missing-documents.required-types";

    private static final Set<DocumentType> DEFAULT_REQUIRED =
            EnumSet.of(DocumentType.RESUME, DocumentType.OFFER_LETTER, DocumentType.JOINING_LETTER);

    private final EmployeeRepository employees;
    private final DocumentRepository documents;
    private final SystemSettingRepository settings;

    public MissingDocumentsEvaluator(EmployeeRepository employees, DocumentRepository documents,
                                     SystemSettingRepository settings) {
        this.employees = employees;
        this.documents = documents;
        this.settings = settings;
    }

    @Override
    public AutomationType type() {
        return AutomationType.MISSING_DOCUMENTS;
    }

    @Override
    public List<AutomationTarget> resolve(AutomationRule rule, LocalDate today) {
        Set<DocumentType> required = resolveRequiredTypes();
        if (required.isEmpty()) {
            return List.of();
        }
        Map<Long, Set<DocumentType>> byEmployee = documentTypesByEmployee();

        List<AutomationTarget> targets = new ArrayList<>();
        for (Employee employee : employees.findAllActiveWithUser()) {
            if (employee.getUser() == null) {
                continue;
            }
            Set<DocumentType> present = byEmployee.getOrDefault(employee.getId(), Set.of());
            List<DocumentType> missing = required.stream().filter(t -> !present.contains(t)).toList();
            if (missing.isEmpty()) {
                continue;
            }
            String missingList = missing.stream().map(Enum::name).collect(Collectors.joining(", "));
            targets.add(new AutomationTarget(employee.getUser(),
                    AutomationVariables.of("name", employee.getFullName(), "code", employee.getEmployeeCode(),
                            "missing", missingList)));
        }
        return targets;
    }

    private Map<Long, Set<DocumentType>> documentTypesByEmployee() {
        Map<Long, Set<DocumentType>> byEmployee = new HashMap<>();
        for (Object[] row : documents.findEmployeeDocumentTypes()) {
            Long employeeId = (Long) row[0];
            DocumentType docType = (DocumentType) row[1];
            byEmployee.computeIfAbsent(employeeId, k -> EnumSet.noneOf(DocumentType.class)).add(docType);
        }
        return byEmployee;
    }

    private Set<DocumentType> resolveRequiredTypes() {
        String raw = settings.findBySettingKey(REQUIRED_TYPES_SETTING)
                .map(s -> s.getSettingValue())
                .orElse(null);
        if (raw == null || raw.isBlank()) {
            return DEFAULT_REQUIRED;
        }
        Set<DocumentType> required = EnumSet.noneOf(DocumentType.class);
        for (String token : raw.split(",")) {
            String name = token.trim();
            if (name.isEmpty()) {
                continue;
            }
            try {
                required.add(DocumentType.valueOf(name));
            } catch (IllegalArgumentException ex) {
                log.warn("Ignoring unknown document type '{}' in setting {}", name, REQUIRED_TYPES_SETTING);
            }
        }
        return required.isEmpty() ? DEFAULT_REQUIRED : required;
    }
}
