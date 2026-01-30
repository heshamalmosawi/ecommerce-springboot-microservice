package com.sayedhesham.productservice.service.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sayedhesham.productservice.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(User user) {
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
