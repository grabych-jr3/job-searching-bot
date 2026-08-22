package com.ogidazepam.job_api_service.auth.service;

import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long validity;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long validity) {
        this.secret = secret;
        this.validity = validity;
    }

    public String generateToken(CustomUserDetails userDetails){
        return Jwts.builder()
                .signWith(generateKey())
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(validity)))
                .compact();
    }

    private SecretKey generateKey(){
        byte[] k = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(k);
    }

    public String extractUsername(String jwt){
        Claims claims = getClaims(jwt);
        return claims.getSubject();
    }

    public boolean isTokenValid(String jwt){
        Claims claims = getClaims(jwt);
        return claims.getExpiration().after(Date.from(Instant.now()));
    }

    private Claims getClaims(String jwt){
        return Jwts.parser()
                .verifyWith(generateKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
