package com.example.Spring;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.websocket.servlet.TomcatWebSocketServletWebServerCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;



@Component
public class JwtUtils {

    private final TomcatWebSocketServletWebServerCustomizer tomcatWebSocketServletWebServerCustomizer;
    public String jwtSecret = "YS1zdHJpbmctc2VjcmV0LWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmc=";
    public int jwtExpirationMs = 172800000;

    public JwtUtils(TomcatWebSocketServletWebServerCustomizer tomcatWebSocketServletWebServerCustomizer) {
        this.tomcatWebSocketServletWebServerCustomizer = tomcatWebSocketServletWebServerCustomizer;
    }

    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer "))
            return bearerToken.substring(7);
        return null;
    }

    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        return Jwts.builder()
                .subject(username)
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(new Date().getTime() + jwtExpirationMs))
                .signWith(Key())
                .compact();
    }

    public boolean validateJwtToken(String jwtToken){
        try{
            Jwts.parser().verifyWith((SecretKey) Key())
                    .build().parseSignedClaims(jwtToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Key Key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUsernameFromToken(String jwt) {
        return Jwts.parser().verifyWith((SecretKey) Key())
                .build().parseSignedClaims(jwt)
                .getPayload().getSubject();
    }

    public Claims getAllClaims(String jwt) {
        return Jwts.parser().verifyWith((SecretKey) Key())
                .build().parseSignedClaims(jwt)
                .getPayload();
    }
}
