package com.ogm.hrms.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit test for the {@link SecurityConfig} beans that can be verified without a context. */
class SecurityConfigTest {

    @Test
    void passwordEncoder_isBCryptAndVerifiesCorrectly() {
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

        String hash = encoder.encode("secret");

        assertThat(hash).isNotEqualTo("secret");   // never store plaintext
        assertThat(hash).startsWith("$2");          // BCrypt prefix
        assertThat(encoder.matches("secret", hash)).isTrue();
        assertThat(encoder.matches("wrong", hash)).isFalse();
    }
}
