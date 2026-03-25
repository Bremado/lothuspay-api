package com.lothuspay.gateway.provider;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    private Key getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            System.out.println("[JWT PROVIDER] Erro ao decodificar chave Base64: " + e.getMessage());
            throw new RuntimeException("Erro ao decodificar secret JWT (Base64 inválido).");
        }
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);

            System.out.println("[JWT PROVIDER] Token validado com sucesso!");
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JWT PROVIDER] Token expirado: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("[JWT PROVIDER] Token com formato não suportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("[JWT PROVIDER] Token malformado: " + e.getMessage());
        } catch (SignatureException e) {
            System.out.println("[JWT PROVIDER] Assinatura inválida do token: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[JWT PROVIDER] Token vazio ou nulo: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[JWT PROVIDER] Erro inesperado ao validar token: " + e.getMessage());
        }

        return false;
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("[JWT PROVIDER] Erro ao extrair claims do token: " + e.getMessage());
            throw new RuntimeException("Erro ao extrair claims do token JWT.");
        }
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> getRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }

    public Date getExpiration(String token) {
        return getClaims(token).getExpiration();
    }
}
