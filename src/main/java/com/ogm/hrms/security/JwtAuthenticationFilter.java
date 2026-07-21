package com.ogm.hrms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authenticates requests bearing a valid {@code Authorization: Bearer <jwt>} access token. On a
 * valid token the security context is populated with an {@link AuthenticatedUser} principal and the
 * authorities carried in the token; invalid/expired tokens are ignored here and left for the
 * authentication entry point to reject as 401 when the endpoint requires authentication.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);

        if (header == null) {
            log.debug("JWT: no Authorization header on {} {}", request.getMethod(), request.getRequestURI());
        } else if (!header.startsWith(PREFIX)) {
            // Do not log the header value; just how it starts, to catch missing space / wrong scheme.
            int previewLen = Math.min(header.length(), 8);
            log.debug("JWT: Authorization header does not start with 'Bearer ' (starts with '{}…') on {} {}",
                    header.substring(0, previewLen), request.getMethod(), request.getRequestURI());
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("JWT: context already authenticated; skipping on {}", request.getRequestURI());
        } else {
            String token = header.substring(PREFIX.length());
            log.debug("JWT: extracted bearer token (length={}) on {} {}",
                    token.length(), request.getMethod(), request.getRequestURI());
            try {
                Claims claims = jwtService.parse(token);
                Long userId = claims.get(JwtService.CLAIM_USER_ID, Number.class).longValue();
                @SuppressWarnings("unchecked")
                List<String> authorities = claims.get(JwtService.CLAIM_AUTHORITIES, List.class);
                List<SimpleGrantedAuthority> grantedAuthorities = authorities == null
                        ? List.of()
                        : authorities.stream().map(SimpleGrantedAuthority::new).toList();

                var authentication = UsernamePasswordAuthenticationToken.authenticated(
                        new AuthenticatedUser(userId, claims.getSubject()), null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT: authenticated subject='{}' (uid={}) with {} authorities on {}",
                        claims.getSubject(), userId, grantedAuthorities.size(), request.getRequestURI());
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                log.debug("JWT: validation FAILED on {} {} -> {}: {}",
                        request.getMethod(), request.getRequestURI(),
                        ex.getClass().getSimpleName(), ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
