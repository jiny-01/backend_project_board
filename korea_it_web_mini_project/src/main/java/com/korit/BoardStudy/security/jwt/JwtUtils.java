package com.korit.BoardStudy.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private final Key KEY;
    public JwtUtils(@Value("${jwt.secret}") String secret) { // @Value 는 Springframework.beans꺼 들고오기
        KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateAccessToken(String id) { // 유효기간 1달짜리 토근이 만들어짐
        return Jwts.builder()
                .subject("AccessToken")
                .id(id)
                .expiration(new Date(new Date().getTime() + (1000L * 60L * 60L * 24L * 30L)))
                .signWith(KEY)
                .compact();
    }

    public String generateVerifyToken(String id) {
        // 메일 인증용(임시 사용자 -> 일반 사용자) 토큰
        return Jwts.builder()
                .subject("VerifyToken")
                .id(id)
                .expiration(new Date(new Date().getTime() + (1000L * 60L * 3L)))
                // 1초 * 60 * 3 = 3분
                .signWith(KEY)
                .compact();
    }

    public boolean isBearer(String token) {
        if (token == null) {
            return  false;
        }
        if (!token.startsWith("Bearer ")) { // Bearer 로 시작하지 않는 토큰은 false, 시작하면 true
            return false;
        }
        return true;
    }

    public String removeBearer(String token) {
        return token.replaceFirst("Bearer ", "");
        // Bearer로 들어온 토큰의 Bearer를 삭제 - 앞의 값을 뒤의 값으로 변경
    }

    public Claims getClaims(String token) {
        JwtParserBuilder jwtParserBuilder = Jwts.parser(); // 계속 parserBuilder(); 써라함... 근데 안됨...
        jwtParserBuilder.setSigningKey(KEY);
        JwtParser jwtParser = jwtParserBuilder.build();
        return jwtParser.parseClaimsJws(token).getBody();
    }
}