package com.dp.notary.blockchain.auth;

import com.dp.notary.blockchain.Config;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
@Component
public class TokenProvider {
    private final Key key;
    private final long validityInMs; // например, 15 минут

    public TokenProvider(
            @Value("${tokens.TokenKey}") String secret,
            @Value("${tokens.ValideTime}") int ms
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityInMs = ms;
    }
    // 1. Генерация токена
    public String createToken(String name, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .setSubject(name)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. Проверка токена
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    public String refreshToken(String expiredToken) {
        // Парсим **без проверки срока действия**
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(expiredToken)
                .getBody();

        String name = claims.getSubject();
        String role = claims.get("role", String.class);

        // Генерируем новый токен
        return createToken(name, role);
    }

    // 3. Извлечение данных
    public String getName(String token) {
        Claims claims = Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getRole(String token) {
        Claims claims = Jwts.parser().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
        return claims.get("role", String.class);
    }
}

