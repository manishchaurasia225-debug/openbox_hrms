package com.ogm.hrms.security;

import com.ogm.hrms.config.HrmsSecurityProperties;
import com.ogm.hrms.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Unit tests for {@link JwtService} — token generation, claims, and validation. */
class JwtServiceTest {

    private JwtService jwtService;

    /** JwtService only reads the {@code jwt} section; CORS is irrelevant here, so keep it minimal. */
    private static HrmsSecurityProperties.Cors testCors() {
        return new HrmsSecurityProperties.Cors(
                List.of(), List.of("GET"), List.of("Authorization"), List.of(), false, Duration.ofMinutes(30));
    }

    @BeforeEach
    void setUp() {
        var jwt = new HrmsSecurityProperties.Jwt(
                "unit-test-secret-unit-test-secret-0123456789ABCDEF",
                Duration.ofMinutes(15), Duration.ofDays(7), "ogm-hrms");
        var lockout = new HrmsSecurityProperties.Lockout(5, Duration.ofMinutes(15));
        var tokens = new HrmsSecurityProperties.Tokens(Duration.ofMinutes(30), Duration.ofDays(1));
        jwtService = new JwtService(new HrmsSecurityProperties(jwt, lockout, tokens, testCors()));
    }

    @Test
    void generatesTokenCarryingSubjectUserIdAndAuthorities() {
        User user = new User();
        user.setId(42L);
        user.setEmail("admin@ogm.test");

        String token = jwtService.generateAccessToken(user, List.of("ROLE_SUPER_ADMIN", "EMPLOYEE:VIEW"));
        Claims claims = jwtService.parse(token);

        assertThat(claims.getSubject()).isEqualTo("admin@ogm.test");
        assertThat(claims.getIssuer()).isEqualTo("ogm-hrms");
        assertThat(claims.get(JwtService.CLAIM_USER_ID, Number.class).longValue()).isEqualTo(42L);
        assertThat(claims.get(JwtService.CLAIM_AUTHORITIES, List.class))
                .contains("ROLE_SUPER_ADMIN", "EMPLOYEE:VIEW");
    }

    @Test
    void rejectsTamperedToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.test");
        String token = jwtService.generateAccessToken(user, List.of());

        String tampered = token.substring(0, token.length() - 3) + "xyz";

        assertThatThrownBy(() -> jwtService.parse(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingSecret() {
        var jwt = new HrmsSecurityProperties.Jwt("  ", Duration.ofMinutes(15), Duration.ofDays(7), "ogm-hrms");
        var props = new HrmsSecurityProperties(jwt, new HrmsSecurityProperties.Lockout(5, Duration.ofMinutes(15)),
                new HrmsSecurityProperties.Tokens(Duration.ofMinutes(30), Duration.ofDays(1)), testCors());

        assertThatThrownBy(() -> new JwtService(props)).isInstanceOf(IllegalStateException.class);
    }
}
