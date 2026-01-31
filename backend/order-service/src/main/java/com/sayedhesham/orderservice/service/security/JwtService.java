package com.sayedhesham.orderservice.service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.sayedhesham.orderservice.model.User;

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
                .claim("role", user.getRole())
                .claim("id", user.getId())
                .setIssuedAt(new Date())
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

}
