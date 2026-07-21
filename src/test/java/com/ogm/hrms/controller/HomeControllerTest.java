package com.ogm.hrms.controller;

import com.ogm.hrms.config.SecurityConfig;
import com.ogm.hrms.security.JwtService;
import com.ogm.hrms.security.RestAccessDeniedHandler;
import com.ogm.hrms.security.RestAuthenticationEntryPoint;
import com.ogm.hrms.service.AuditService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Web-slice tests for {@link HomeController}, exercised through the real
 * {@link SecurityConfig} filter chain. {@code @WebMvcTest} starts no datasource/JPA
 * context, so these run without a database.
 */
@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // The imported SecurityConfig also declares the stateless API chain, whose collaborators are not
    // part of this web slice; mock them so the context builds. Only the web chain is exercised here.
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private RestAuthenticationEntryPoint authenticationEntryPoint;
    @MockitoBean
    private RestAccessDeniedHandler accessDeniedHandler;
    // The audit interceptor (registered by WebMvcConfig) needs the audit service, which is outside
    // this web slice.
    @MockitoBean
    private AuditService auditService;

    @Test
    void homePageIsPublicAndRendersIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(
                        Matchers.containsString("OGM Human Resource Management System")));
    }

    @Test
    void protectedRequestWithoutAuthenticationRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/some-protected-path"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser
    void authenticatedRequestPassesSecurity() throws Exception {
        // No mapping exists for this path, so once security admits the request it
        // resolves to 404 rather than a redirect to the login page.
        mockMvc.perform(get("/some-protected-path"))
                .andExpect(status().isNotFound());
    }
}
