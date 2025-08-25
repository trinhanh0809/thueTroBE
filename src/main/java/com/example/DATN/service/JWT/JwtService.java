package com.example.DATN.service.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    /** Nên đặt ở application.yml:
     * app.jwt.secret: (base64) 64 bytes trở lên
     * app.jwt.expiration-ms: 604800000 (7 ngày) */
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:604800000}")
    private long expirationMs;

    @PostConstruct
    public void checkSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Missing property app.jwt.secret. Define in application.yml/properties.");
        }
    }

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception ignore) {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /* ================= Issue token ================= */

    /** Phát hành token: username + roles + extra claims (id, email, ...) */
    public String generateToken(String username, List<String> roles, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        if (extraClaims != null) claims.putAll(extraClaims);
        if (roles != null && !roles.isEmpty()) claims.put("roles", roles); // ["ADMIN","HOST",...]

        Instant now = Instant.now();
        Instant exp = now.plusMillis(expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Overload tiện: chỉ có username (không roles/extra) */
    public String generateToken(String username) {
        return generateToken(username, Collections.emptyList(), null);
    }

    /* ================= Parse/validate ================= */

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public List<String> extractRoles(String token) {
        Claims claims = parseAllClaims(token);
        Object r = claims.get("roles");
        if (r instanceof Collection<?> col) {
            return col.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parseAllClaims(token);
        return resolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null
                && username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
    }

    // Alias cho code cũ nếu bạn đang gọi validateToken(...)
    public boolean validateToken(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims parseAllClaims(String token) {
        SecretKey key = (SecretKey) getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
