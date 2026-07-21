package com.ogm.hrms.security;

import com.ogm.hrms.config.HrmsSecurityProperties;
import com.ogm.hrms.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates stateless JWT access tokens (HMAC-SHA256). The token subject is the user's
 * email; custom claims carry the user id and the flattened authority list (role names + permission
 * codes) so authorization can be enforced without a database round-trip on every request.
 *
 * <p>Refresh tokens are intentionally <em>not</em> JWTs — they are opaque random values stored
 * hashed (see {@link com.ogm.hrms.service.AuthService}); this class only handles access tokens.</p>
 */
@Service
public class JwtService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_AUTHORITIES = "authorities";

    private final SecretKey signingKey;
    private final HrmsSecurityProperties.Jwt jwtProps;

    public JwtService(HrmsSecurityProperties properties) {
        String secret = properties.jwt().secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "HRMS_JWT_SECRET is not configured. A strong (>= 32 byte) JWT signing secret is required.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtProps = properties.jwt();
    }

    public String generateAccessToken(User user, List<String> authorities) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiry = now.plus(jwtProps.accessTokenTtl());
        return Jwts.builder()
                .issuer(jwtProps.issuer())
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_AUTHORITIES, authorities)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiry.toInstant()))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a token's signature and expiry.
     *
     * @return the token claims
     * @throws JwtException if the token is malformed, tampered, or expired
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProps.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
