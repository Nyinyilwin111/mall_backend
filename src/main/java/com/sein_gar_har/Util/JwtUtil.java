package com.sein_gar_har.Util;

import com.sein_gar_har.config.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    // SecretKey for HS256
    private final SecretKey key =
            Keys.hmacShaKeyFor(Decoders.BASE64.decode(JwtConstants.SECRET_KEY));

    // 1️⃣ Generate Token (controller only passes username)
    public String generateToken(String username) {
        return Jwts.builder()
                .issuer(JwtConstants.ISSUER)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + JwtConstants.ACCESS_TOKEN_VALIDITY))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 2️⃣ Extract username
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // 3️⃣ Extract authorities (optional)
    public List<String> extractAuthorities(String token) {
        return getClaims(token).get(JwtConstants.AUTHORITIES, List.class);
    }

    // 4️⃣ Validate token
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Invalid token: " + e.getMessage());
            return false;
        }
    }

    // Helper – parse claims using new API
    private Claims getClaims(String token) {
        token = cleanToken(token);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String cleanToken(String token) {
        if (token.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return token.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return token;
    }
}
