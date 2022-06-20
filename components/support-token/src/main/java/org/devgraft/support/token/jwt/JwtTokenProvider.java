package org.devgraft.support.token.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.devgraft.support.provider.DateProvider;
import org.devgraft.support.provider.UUIDProvider;
import org.devgraft.support.token.jwt.config.properties.JwtProperties;
import org.devgraft.support.token.jwt.domain.JwtToken;
import org.devgraft.support.token.jwt.domain.JwtTokenInformation;
import org.devgraft.support.token.TokenProvider;
import org.springframework.stereotype.Component;

import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtTokenProvider implements TokenProvider<JwtToken, JwtTokenGenerateRequest> {
    private final JwtProperties jwtProperties;
    private final UUIDProvider uuidProvider;
    private final DateProvider dateProvider;

    @Override
    public JwtToken generate(JwtTokenGenerateRequest request) {
        Date date = dateProvider.now();
        String accessToken = _generate(date, request.getAccessTokenValidity());
        String refreshToken = _generate(date, request.getRefreshTokenValidity());

        return new JwtToken(new JwtTokenInformation(accessToken, refreshToken,
                request.getAccessTokenValidity(), request.getRefreshTokenValidity(), date));
    }

    private String _generate(Date currentDate, long validity) {
        Claims claims1 = Jwts.claims().setSubject(uuidProvider.randomUUID().toString());
        return Jwts.builder()
                .setClaims(claims1)
                .setIssuedAt(currentDate)
                .setExpiration(new Date(currentDate.getTime() + validity))
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
                .compact();
    }

    public boolean isExpiration(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException expiredJwtException) {
            expiredJwtException.printStackTrace();
            return true;
        }
        return false;
    }
}