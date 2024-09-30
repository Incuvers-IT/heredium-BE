package art.heredium.core.jwt;

import art.heredium.domain.account.type.AuthType;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class AuthToken {

    @Getter
    private final String token;
    private final Key key;

    public static final String AUTHORITIES_KEY = "role";

    AuthToken(String id, Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(id, expiry);
    }

    AuthToken(String id, AuthType auth, Date expiry, Key key) {
        this.key = key;
        this.token = createAuthToken(id, auth, expiry);
    }

    private String createAuthToken(String id, Date expiry) {
        return Jwts.builder()
                .setSubject(id)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    private String createAuthToken(String id, AuthType auth, Date expiry) {
        return Jwts.builder()
                .setSubject(id)
                .claim(AUTHORITIES_KEY, auth.name())
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(expiry)
                .compact();
    }

    public boolean validate() {
        return this.getTokenClaims() != null;
    }

    public Claims getTokenClaims() {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SecurityException e) {
//            log.info("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
//            log.info("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
//            log.info("Expired JWT token.");
            return e.getClaims();
        } catch (UnsupportedJwtException e) {
//            log.info("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
//            log.info("JWT token compact of handler are invalid.");
        } catch (Exception e) {
//            log.info("JWT exception");
        }
        return null;
    }

    public boolean isExpired() {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public Date getExpiration() {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().getExpiration();
    }

    public AuthType getAuth(Claims claims) {
        return AuthType.valueOf(claims.get(AUTHORITIES_KEY, String.class));
    }
}
