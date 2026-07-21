package com.ogm.hrms.service.impl;

import com.ogm.hrms.config.HrmsSecurityProperties;
import com.ogm.hrms.dto.auth.CurrentUserResponse;
import com.ogm.hrms.dto.auth.LoginRequest;
import com.ogm.hrms.dto.auth.TokenResponse;
import com.ogm.hrms.entity.LoginHistory;
import com.ogm.hrms.entity.Permission;
import com.ogm.hrms.entity.RefreshToken;
import com.ogm.hrms.entity.User;
import com.ogm.hrms.enums.AuditAction;
import com.ogm.hrms.enums.AuditOutcome;
import com.ogm.hrms.exception.ApiException;
import com.ogm.hrms.repository.LoginHistoryRepository;
import com.ogm.hrms.repository.RefreshTokenRepository;
import com.ogm.hrms.repository.UserRepository;
import com.ogm.hrms.security.AuthenticatedUser;
import com.ogm.hrms.security.JwtService;
import com.ogm.hrms.security.TokenHasher;
import com.ogm.hrms.service.AuditService;
import com.ogm.hrms.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default {@link AuthService} implementation. Enforces the account-security business rules:
 * soft-deleted/disabled accounts cannot log in, failed attempts are counted and lock the account
 * after a configurable threshold, every attempt is recorded to login history, and refresh tokens are
 * rotated on use and revocable.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final int USER_AGENT_MAX = 255;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final HrmsSecurityProperties.Jwt jwtProps;
    private final HrmsSecurityProperties.Lockout lockoutProps;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           LoginHistoryRepository loginHistoryRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuditService auditService,
                           HrmsSecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.jwtService = jwtService;
        this.jwtProps = securityProperties.jwt();
        this.lockoutProps = securityProperties.lockout();
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = normalize(request.email());
        User user = userRepository.findActiveByEmailWithRolesAndPermissions(email).orElse(null);
        if (user == null) {
            recordAttempt(email, null, false, ipAddress, userAgent, "USER_NOT_FOUND");
            throw ApiException.unauthorized("Invalid email or password");
        }

        unlockIfLockExpired(user);
        if (!user.isAccountNonLocked()) {
            recordAttempt(email, user, false, ipAddress, userAgent, "ACCOUNT_LOCKED");
            throw ApiException.locked("Account is locked due to too many failed attempts. Try again later.");
        }
        if (!user.isEnabled()) {
            recordAttempt(email, user, false, ipAddress, userAgent, "ACCOUNT_DISABLED");
            throw ApiException.forbidden("Account is disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            recordAttempt(email, user, false, ipAddress, userAgent, "BAD_CREDENTIALS");
            throw ApiException.unauthorized("Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        user.setLockedUntil(null);
        user.setLastLoginAt(OffsetDateTime.now());
        recordAttempt(email, user, true, ipAddress, userAgent, null);
        return issueTokens(user, ipAddress, userAgent);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken, String ipAddress, String userAgent) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refreshToken))
                .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));
        if (!token.isActive(OffsetDateTime.now())) {
            throw ApiException.unauthorized("Refresh token is expired or revoked");
        }
        token.setRevoked(true); // rotation: single-use refresh tokens
        User user = userRepository.findActiveByEmailWithRolesAndPermissions(token.getUser().getEmail())
                .orElseThrow(() -> ApiException.unauthorized("Account is no longer active"));
        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw ApiException.forbidden("Account is disabled or locked");
        }
        return issueTokens(user, ipAddress, userAgent);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    User user = token.getUser();
                    auditService.recordAuthEvent(AuditAction.LOGOUT, AuditOutcome.SUCCESS,
                            user != null ? user.getEmail() : null, user != null ? user.getId() : null,
                            "Logout", null, null);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(AuthenticatedUser principal) {
        User user = userRepository.findActiveByEmailWithRolesAndPermissions(principal.email())
                .orElseThrow(() -> ApiException.unauthorized("Account is no longer active"));
        return toCurrentUser(user);
    }

    // --- helpers ---------------------------------------------------------------------------------

    private TokenResponse issueTokens(User user, String ipAddress, String userAgent) {
        String accessToken = jwtService.generateAccessToken(user, grantedAuthorities(user));

        String rawRefresh = TokenHasher.randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenHash(TokenHasher.sha256Hex(rawRefresh));
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(OffsetDateTime.now().plus(jwtProps.refreshTokenTtl()));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(truncate(userAgent));
        refreshTokenRepository.save(refreshToken);

        return TokenResponse.bearer(accessToken, rawRefresh, jwtProps.accessTokenTtl().toSeconds(),
                toCurrentUser(user));
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= lockoutProps.maxFailedAttempts()) {
            user.setAccountNonLocked(false);
            user.setLockedUntil(OffsetDateTime.now().plus(lockoutProps.lockDuration()));
        }
    }

    private void unlockIfLockExpired(User user) {
        if (!user.isAccountNonLocked()
                && user.getLockedUntil() != null
                && !user.getLockedUntil().isAfter(OffsetDateTime.now())) {
            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        }
    }

    private void recordAttempt(String email, User user, boolean successful, String ip, String userAgent,
                               String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setEmail(email);
        history.setUser(user);
        history.setSuccessful(successful);
        history.setIpAddress(ip);
        history.setUserAgent(truncate(userAgent));
        history.setFailureReason(failureReason);
        loginHistoryRepository.save(history);

        auditService.recordAuthEvent(
                successful ? AuditAction.LOGIN : AuditAction.LOGIN_FAILED,
                successful ? AuditOutcome.SUCCESS : AuditOutcome.FAILURE,
                email, user != null ? user.getId() : null,
                successful ? "Login successful" : "Login failed: " + failureReason,
                ip, userAgent);
    }

    private List<String> grantedAuthorities(User user) {
        List<String> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add("ROLE_" + role.getName().name()));
        authorities.addAll(permissionCodes(user));
        return authorities.stream().distinct().toList();
    }

    private Set<String> roleNames(User user) {
        return user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> permissionCodes(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private CurrentUserResponse toCurrentUser(User user) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFullName(),
                roleNames(user), permissionCodes(user));
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= USER_AGENT_MAX ? value : value.substring(0, USER_AGENT_MAX);
    }
}
