package com.sayedhesham.userservice.service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.sayedhesham.userservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // usually from .env but hardcoded since this is a playground project :)
    private static final String SECRET = "a_very_long_and_not_secretive_secret_key_12345678901234567890123456789012";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getEmail())
            .claim("name", user.getName())
            .claim("role", user.getRole())
            .claim("id", user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 3)) // 3 days
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Claims extractAllClaims(String token) throws JwtException {
        try {
            var x = Jwts.parser()
                    .setSigningKey(key).build()
                    .parseSignedClaims(token)
                    .getBody();
            System.out.println("claims:" + x);
            return x;
        } catch (JwtException e) {
            // catch null, wrong token, expired token
            throw new JwtException(e.getMessage());
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public Date getExpirationDate(String token) throws JwtException {
        return extractAllClaims(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public long getTimeUntilExpiration(String token) throws JwtException {
        Date expirationDate = getExpirationDate(token);
        return expirationDate.getTime() - System.currentTimeMillis();
    }

}
