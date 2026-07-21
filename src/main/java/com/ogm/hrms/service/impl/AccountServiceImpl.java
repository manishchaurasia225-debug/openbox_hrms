package com.ogm.hrms.service.impl;

import com.ogm.hrms.config.HrmsSecurityProperties;
import com.ogm.hrms.entity.AccountToken;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.TokenType;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.repository.AccountTokenRepository;
import com.ogm.hrms.repository.RefreshTokenRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.security.TokenHasher;
import com.ogm.hrms.service.AccountService;
import com.ogm.hrms.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Default {@link AccountService}. Tokens are random opaque values stored only as SHA-256 hashes,
 * single-use, and time-limited. A successful password reset also clears any lockout and revokes all
 * refresh tokens ("logout everywhere").
 */
@Service
public class AccountServiceImpl implements AccountService {

    private final UserRepository userRepository;
    private final AccountTokenRepository accountTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final HrmsSecurityProperties.Tokens tokenProps;

    public AccountServiceImpl(UserRepository userRepository, AccountTokenRepository accountTokenRepository,
                              RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                              EmailService emailService, HrmsSecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.accountTokenRepository = accountTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenProps = securityProperties.tokens();
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        activeUser(email).ifPresent(user -> {
            String rawToken = issueToken(user, TokenType.PASSWORD_RESET, tokenProps.passwordResetTtl());
            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        });
        // Always returns quietly — never reveals whether the account exists.
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String rawToken, String newPassword) {
        AccountToken token = usableToken(rawToken, TokenType.PASSWORD_RESET);
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setAccountNonLocked(true);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        token.setUsedAt(OffsetDateTime.now());
        refreshTokenRepository.revokeAllForUser(user);
    }

    @Override
    @Transactional
    public void resendEmailVerification(String email) {
        activeUser(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> {
                    String rawToken = issueToken(user, TokenType.EMAIL_VERIFICATION,
                            tokenProps.emailVerificationTtl());
                    emailService.sendEmailVerificationEmail(user.getEmail(), rawToken);
                });
    }

    @Override
    @Transactional
    public void confirmEmailVerification(String rawToken) {
        AccountToken token = usableToken(rawToken, TokenType.EMAIL_VERIFICATION);
        token.getUser().setEmailVerified(true);
        token.setUsedAt(OffsetDateTime.now());
    }

    // --- helpers ---------------------------------------------------------------------------------

    private Optional<User> activeUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(user -> !user.isDeleted());
    }

    private String issueToken(User user, TokenType type, Duration ttl) {
        String rawToken = TokenHasher.randomToken();
        AccountToken token = new AccountToken();
        token.setTokenHash(TokenHasher.sha256Hex(rawToken));
        token.setTokenType(type);
        token.setUser(user);
        token.setExpiresAt(OffsetDateTime.now().plus(ttl));
        accountTokenRepository.save(token);
        return rawToken;
    }

    private AccountToken usableToken(String rawToken, TokenType type) {
        AccountToken token = accountTokenRepository
                .findByTokenHashAndTokenType(TokenHasher.sha256Hex(rawToken), type)
                .orElseThrow(() -> ApiException.badRequest("The token is invalid or has already been used"));
        if (!token.isUsable(OffsetDateTime.now())) {
            throw ApiException.badRequest("The token is invalid or has expired");
        }
        return token;
    }
}
