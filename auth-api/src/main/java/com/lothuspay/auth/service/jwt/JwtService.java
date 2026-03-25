package com.lothuspay.auth.service.jwt;

import com.lothuspay.auth.model.accounts.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    // Chave secreta para assinar o JWT. DEVE ser uma string longa e segura.
    // Idealmente, carregada de variáveis de ambiente ou Vault.
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Tempo de expiração do JWT em milissegundos
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // --- Métodos para extrair informações do token ---

    /**
     * Extrai o nome de usuário (subject) do token JWT.
     * @param token O token JWT.
     * @return O nome de usuário (geralmente o e-mail).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma claim específica do token JWT.
     * @param token O token JWT.
     * @param claimsResolver Função para resolver a claim desejada.
     * @param <T> Tipo da claim.
     * @return O valor da claim.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrai todas as claims do token JWT.
     * @param token O token JWT.
     * @return Objeto Claims contendo todas as informações do token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtém a chave de assinatura a partir da secretKey configurada.
     * @return A chave de assinatura.
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- Métodos para gerar e validar o token ---

    /**
     * Gera um token JWT para um UserDetails sem claims extras.
     * @param userDetails Detalhes do usuário.
     * @return O token JWT gerado.
     */
    public String generateToken(Account a, UserDetails userDetails) {
        return generateToken(new HashMap<>(), a, userDetails);
    }

    /**
     * Gera um token JWT com claims extras e detalhes do usuário.
     * @param extraClaims Claims adicionais a serem incluídas no token.
     * @param userDetails Detalhes do usuário.
     * @return O token JWT gerado.
     */
    public String generateToken(Map<String, Object> extraClaims, Account a, UserDetails userDetails) {
        extraClaims.put("id", a.getId());
        extraClaims.put("roles", userDetails.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.toList()));

        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida se um token JWT é válido para um determinado UserDetails.
     * @param token O token JWT.
     * @param userDetails Detalhes do usuário.
     * @return true se o token for válido, false caso contrário.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica se o token JWT expirou.
     * @param token O token JWT.
     * @return true se o token expirou, false caso contrário.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token JWT.
     * @param token O token JWT.
     * @return A data de expiração.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
