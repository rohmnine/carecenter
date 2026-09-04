package com.example.carecenter.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtUtil {

    @Autowired
    private JwtEncoder encoder;

    public String extractUsername(String token) {
        // This method needs to be re-implemented if you need to extract claims.
        // For now, we'll just return the subject from the decoded token.
        // Note: You'll need a JwtDecoder bean for this.
        return null;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.HOURS))
                .subject(userDetails.getUsername())
                .claim("role", userDetails.getAuthorities().stream().findFirst().get().getAuthority())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        // Token validation is now handled by the JwtRequestFilter and Spring Security.
        // This method can be removed or re-implemented if needed.
        return null;
    }
}