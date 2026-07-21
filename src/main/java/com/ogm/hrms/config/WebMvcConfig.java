package com.ogm.hrms.config;

import com.ogm.hrms.audit.AuditInterceptor;
import com.ogm.hrms.service.AuditService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers web-layer cross-cutting concerns — currently the audit interceptor over the REST API. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditService auditService;

    public WebMvcConfig(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuditInterceptor(auditService)).addPathPatterns("/api/v1/**");
    }
}
