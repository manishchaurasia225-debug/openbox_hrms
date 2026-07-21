package com.ogm.hrms.config;

import com.ogm.hrms.entity.AutomationRule;
import com.ogm.hrms.entity.CompanyProfile;
import com.ogm.hrms.entity.EmailTemplate;
import com.ogm.hrms.entity.LeaveType;
import com.ogm.hrms.entity.Permission;
import com.ogm.hrms.entity.Role;
import com.ogm.hrms.entity.SystemSetting;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.entity.WhatsAppTemplate;
import com.ogm.hrms.enums.AutomationType;
import com.ogm.hrms.enums.EmailTemplateCategory;
import com.ogm.hrms.enums.NotificationChannel;
import com.ogm.hrms.enums.WhatsAppTemplateCategory;
import com.ogm.hrms.enums.PermissionAction;
import com.ogm.hrms.enums.PermissionModule;
import com.ogm.hrms.enums.RoleName;
import com.ogm.hrms.repository.AutomationRuleRepository;
import com.ogm.hrms.repository.CompanyProfileRepository;
import com.ogm.hrms.repository.EmailTemplateRepository;
import com.ogm.hrms.repository.LeaveTypeRepository;
import com.ogm.hrms.repository.PermissionRepository;
import com.ogm.hrms.repository.RoleRepository;
import com.ogm.hrms.repository.SystemSettingRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.repository.WhatsAppTemplateRepository;
import com.ogm.hrms.security.RolePermissionMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Idempotently seeds the RBAC baseline at startup: the full permission catalogue (every
 * {@link PermissionModule} × {@link PermissionAction}), the nine canonical {@link RoleName}s, and a
 * Super Admin grant of all permissions. Optionally bootstraps the first Super Admin user from the
 * environment ({@code hrms.bootstrap.super-admin.*}) so the platform is usable on first boot without
 * any hardcoded credentials.
 *
 * <p>Detailed per-role grants for the other eight roles (mapping the full permissions matrix) are a
 * dedicated follow-up; until then deny-by-default keeps unseeded roles safe.</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CompanyProfileRepository companyProfileRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final WhatsAppTemplateRepository whatsAppTemplateRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hrms.bootstrap.super-admin.email:}")
    private String superAdminEmail;
    @Value("${hrms.bootstrap.super-admin.password:}")
    private String superAdminPassword;
    @Value("${hrms.bootstrap.super-admin.full-name:System Administrator}")
    private String superAdminFullName;

    public DataInitializer(PermissionRepository permissionRepository, RoleRepository roleRepository,
                           UserRepository userRepository, CompanyProfileRepository companyProfileRepository,
                           SystemSettingRepository systemSettingRepository, LeaveTypeRepository leaveTypeRepository,
                           AutomationRuleRepository automationRuleRepository,
                           EmailTemplateRepository emailTemplateRepository,
                           WhatsAppTemplateRepository whatsAppTemplateRepository, PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.companyProfileRepository = companyProfileRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.automationRuleRepository = automationRuleRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.whatsAppTemplateRepository = whatsAppTemplateRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int createdPermissions = ensurePermissionCatalogue();
        int createdRoles = ensureRoles();
        applyRoleGrants();
        ensureCompanyProfile();
        int createdSettings = ensureDefaultSettings();
        int createdLeaveTypes = ensureLeaveTypes();
        int createdAutomations = ensureAutomationRules();
        int createdTemplates = ensureEmailTemplates();
        int createdWhatsAppTemplates = ensureWhatsAppTemplates();
        boolean adminCreated = bootstrapSuperAdminUser();
        log.info("Bootstrap complete: +{} permissions, +{} roles, +{} settings, +{} leave types, "
                        + "+{} automation rules, +{} email templates, +{} whatsapp templates, superAdminCreated={}",
                createdPermissions, createdRoles, createdSettings, createdLeaveTypes, createdAutomations,
                createdTemplates, createdWhatsAppTemplates, adminCreated);
    }

    /**
     * Seeds a small library of starter WhatsApp templates (project-rules.md: never hardcode wording).
     * Bodies use {@code {placeholder}} tokens resolved per send by the WhatsApp engine.
     */
    private int ensureWhatsAppTemplates() {
        int created = 0;
        created += seedWhatsAppTemplate("WA_WELCOME", "Welcome", WhatsAppTemplateCategory.UTILITY,
                "Hi {name}, welcome to the team! Your employee code is {code}.",
                "Sent to a new employee on joining");
        created += seedWhatsAppTemplate("WA_BIRTHDAY", "Birthday Wish", WhatsAppTemplateCategory.MARKETING,
                "Happy Birthday, {name}! Wishing you a wonderful year ahead.",
                "Birthday greeting");
        created += seedWhatsAppTemplate("WA_LEAVE_APPROVED", "Leave Approved", WhatsAppTemplateCategory.UTILITY,
                "Hi {name}, your leave from {fromDate} to {toDate} has been approved.",
                "Sent when a leave request is approved");
        return created;
    }

    private int seedWhatsAppTemplate(String code, String name, WhatsAppTemplateCategory category, String bodyText,
                                     String description) {
        if (whatsAppTemplateRepository.existsByCodeIgnoreCase(code)) {
            return 0;
        }
        WhatsAppTemplate template = new WhatsAppTemplate();
        template.setCode(code);
        template.setName(name);
        template.setCategory(category);
        template.setBodyText(bodyText);
        template.setActive(true);
        template.setDescription(description);
        whatsAppTemplateRepository.save(template);
        return 1;
    }

    /**
     * Seeds a small library of starter email templates (project-rules.md: never hardcode wording).
     * Subjects/bodies use {@code {placeholder}} tokens resolved per send by the Email Template Engine.
     */
    private int ensureEmailTemplates() {
        int created = 0;
        created += seedEmailTemplate("WELCOME", "Welcome Email", EmailTemplateCategory.ONBOARDING,
                "Welcome aboard, {name}!",
                "<p>Hi {name},</p><p>Welcome to the team! Your employee code is <b>{code}</b>. "
                        + "We're excited to have you on board.</p><p>— HR Team</p>",
                "Sent to a new employee on joining");
        created += seedEmailTemplate("LEAVE_APPROVED", "Leave Approved", EmailTemplateCategory.LEAVE,
                "Your leave request has been approved",
                "<p>Hi {name},</p><p>Your leave from <b>{fromDate}</b> to <b>{toDate}</b> has been "
                        + "approved.</p><p>— HR Team</p>",
                "Sent when a leave request is approved");
        created += seedEmailTemplate("PAYSLIP_AVAILABLE", "Payslip Available", EmailTemplateCategory.PAYROLL,
                "Your payslip for {month}/{year} is available",
                "<p>Hi {name},</p><p>Your payslip for <b>{month}/{year}</b> is now available in the HR "
                        + "portal.</p><p>— Finance Team</p>",
                "Sent when a payslip is generated");
        return created;
    }

    private int seedEmailTemplate(String code, String name, EmailTemplateCategory category, String subject,
                                  String bodyHtml, String description) {
        if (emailTemplateRepository.existsByCodeIgnoreCase(code)) {
            return 0;
        }
        EmailTemplate template = new EmailTemplate();
        template.setCode(code);
        template.setName(name);
        template.setCategory(category);
        template.setSubject(subject);
        template.setBodyHtml(bodyHtml);
        template.setActive(true);
        template.setDescription(description);
        emailTemplateRepository.save(template);
        return 1;
    }

    /**
     * Seeds one editable automation rule per {@link AutomationType} (project-rules.md: never hardcode
     * business rules). Channels default to in-app + email; WhatsApp is opt-in once Module 20 is
     * configured. Templates use {@code {placeholder}} tokens resolved per recipient at dispatch.
     */
    private int ensureAutomationRules() {
        int created = 0;
        created += seedAutomationRule(AutomationType.BIRTHDAY_WISH, null,
                "Happy Birthday, {name}!",
                "Wishing you a wonderful birthday and a fantastic year ahead from all of us.");
        created += seedAutomationRule(AutomationType.FESTIVAL_WISH, null,
                "Happy {occasion}!",
                "Warm wishes on {occasion} to you and your family from the entire team.");
        created += seedAutomationRule(AutomationType.WELCOME_MESSAGE, null,
                "Welcome aboard, {name}!",
                "We're delighted to have you join us ({code}). Wishing you a great start!");
        created += seedAutomationRule(AutomationType.ATTENDANCE_REMINDER, null,
                "Attendance reminder",
                "Hi {name}, your attendance for {date} isn't recorded yet. Please mark it.");
        created += seedAutomationRule(AutomationType.LEAVE_REMINDER, null,
                "Pending leave request",
                "Hi {name}, you have a leave request still awaiting approval.");
        created += seedAutomationRule(AutomationType.MISSING_DOCUMENTS, null,
                "Documents required",
                "Hi {name}, these documents are missing from your profile: {missing}. Please upload them.");
        created += seedAutomationRule(AutomationType.PROMOTION_CONGRATULATIONS, null,
                "Congratulations, {name}!",
                "Congratulations on your promotion — {title}. Well deserved!");
        created += seedAutomationRule(AutomationType.CONFIRMATION_REMINDER, 7,
                "Confirmation due in {days} day(s)",
                "Hi {name}, your probation ends on {date}. Confirmation is due soon.");
        created += seedAutomationRule(AutomationType.CONTRACT_EXPIRY, 30,
                "Contract expiring in {days} day(s)",
                "Hi {name}, your engagement/contract ends on {date}. Please plan for renewal.");
        return created;
    }

    private int seedAutomationRule(AutomationType type, Integer leadDays, String titleTemplate,
                                   String messageTemplate) {
        if (automationRuleRepository.existsByType(type)) {
            return 0;
        }
        AutomationRule rule = new AutomationRule();
        rule.setType(type);
        rule.setEnabled(true);
        rule.setChannels(new java.util.LinkedHashSet<>(Set.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL)));
        rule.setTitleTemplate(titleTemplate);
        rule.setMessageTemplate(messageTemplate);
        rule.setLeadDays(leadDays);
        automationRuleRepository.save(rule);
        return 1;
    }

    /** Seeds the standard, but editable, leave types (project-rules.md: never hardcode leave types). */
    private int ensureLeaveTypes() {
        int created = 0;
        created += seedLeaveType("CASUAL", "Casual Leave", 12, true);
        created += seedLeaveType("SICK", "Sick Leave", 8, true);
        created += seedLeaveType("EARNED", "Earned Leave", 15, true);
        created += seedLeaveType("MATERNITY", "Maternity Leave", 180, true);
        created += seedLeaveType("PATERNITY", "Paternity Leave", 15, true);
        created += seedLeaveType("WFH", "Work From Home", 0, true);
        created += seedLeaveType("LOP", "Loss of Pay", 0, false);
        return created;
    }

    private int seedLeaveType(String code, String name, int quota, boolean paid) {
        if (leaveTypeRepository.existsByCodeIgnoreCase(code)) {
            return 0;
        }
        LeaveType type = new LeaveType();
        type.setCode(code);
        type.setName(name);
        type.setDefaultAnnualQuota(quota);
        type.setPaid(paid);
        type.setActive(true);
        leaveTypeRepository.save(type);
        return 1;
    }

    private void ensureCompanyProfile() {
        if (companyProfileRepository.count() == 0) {
            CompanyProfile company = new CompanyProfile();
            company.setLegalName("Your Company");
            company.setDisplayName("OGM HRMS");
            company.setTimezone("UTC");
            company.setCurrency("INR");
            company.setCountry("India");
            companyProfileRepository.save(company);
            log.info("Seeded default company profile");
        }
    }

    /** Seeds tunable defaults (attendance policies + general) so feature modules never hardcode them. */
    private int ensureDefaultSettings() {
        int created = 0;
        created += seedSetting("attendance.working-hours-per-day", "8", "attendance",
                "Standard working hours per day");
        created += seedSetting("attendance.late-arrival-grace-minutes", "15", "attendance",
                "Grace period before an arrival is marked late");
        created += seedSetting("attendance.max-wfh-days-per-month", "10", "attendance",
                "Maximum work-from-home days allowed per month");
        created += seedSetting("attendance.office-ip-allowlist", "", "attendance",
                "Comma-separated office IP/CIDR ranges that auto-mark office attendance");
        created += seedSetting("attendance.wfh-auto-approve", "false", "attendance",
                "Whether work-from-home requests are auto-approved");
        created += seedSetting("general.date-format", "yyyy-MM-dd", "general", "Default display date format");
        created += seedSetting("automation.missing-documents.required-types",
                "RESUME,OFFER_LETTER,JOINING_LETTER", "automation",
                "Comma-separated document types every employee must have on file (drives the Missing Documents automation)");
        return created;
    }

    private int seedSetting(String key, String value, String category, String description) {
        if (systemSettingRepository.existsBySettingKey(key)) {
            return 0;
        }
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setCategory(category);
        setting.setDescription(description);
        setting.setEditable(true);
        systemSettingRepository.save(setting);
        return 1;
    }

    private int ensurePermissionCatalogue() {
        int created = 0;
        for (PermissionModule module : PermissionModule.values()) {
            for (PermissionAction action : PermissionAction.values()) {
                String code = module.name() + ":" + action.name();
                if (!permissionRepository.existsByCode(code)) {
                    permissionRepository.save(new Permission(module, action,
                            action.name() + " access to the " + module.name() + " module"));
                    created++;
                }
            }
        }
        return created;
    }

    private int ensureRoles() {
        int created = 0;
        for (RoleName name : RoleName.values()) {
            if (!roleRepository.existsByName(name)) {
                roleRepository.save(new Role(name, humanize(name)));
                created++;
            }
        }
        return created;
    }

    /**
     * Applies the authoritative {@link RolePermissionMatrix} to every role, idempotently. Grants
     * stay editable afterwards; a later run reconciles any role whose grants drift from the matrix.
     */
    private void applyRoleGrants() {
        Map<String, Permission> byCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, permission -> permission));

        RolePermissionMatrix.grants().forEach((roleName, codes) -> {
            Role role = roleRepository.findByName(roleName).orElseThrow();
            Set<Permission> desired = codes.stream()
                    .map(byCode::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
            if (!sameCodes(role.getPermissions(), desired)) {
                role.getPermissions().clear();
                role.getPermissions().addAll(desired);
                roleRepository.save(role);
            }
        });
    }

    private boolean sameCodes(Set<Permission> current, Set<Permission> desired) {
        if (current.size() != desired.size()) {
            return false;
        }
        Set<String> currentCodes = current.stream().map(Permission::getCode).collect(Collectors.toSet());
        Set<String> desiredCodes = desired.stream().map(Permission::getCode).collect(Collectors.toSet());
        return currentCodes.equals(desiredCodes);
    }

    private boolean bootstrapSuperAdminUser() {
        if (superAdminEmail == null || superAdminEmail.isBlank()
                || superAdminPassword == null || superAdminPassword.isBlank()) {
            log.info("No hrms.bootstrap.super-admin credentials configured; skipping admin bootstrap.");
            return false;
        }
        String email = superAdminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("Super Admin '{}' already exists; skipping bootstrap (idempotent).", email);
            return false;
        }
        Role superAdmin = roleRepository.findByName(RoleName.SUPER_ADMIN).orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(superAdminPassword));
        user.setFullName(superAdminFullName);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.addRole(superAdmin);
        userRepository.save(user);
        log.info("Bootstrapped Super Admin user '{}'", email);
        return true;
    }

    private String humanize(RoleName name) {
        String[] parts = name.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(' ');
        }
        return sb.toString().trim();
    }
}
