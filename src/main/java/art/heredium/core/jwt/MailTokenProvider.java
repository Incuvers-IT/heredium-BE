package art.heredium.core.jwt;

import art.heredium.domain.account.type.AuthType;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class MailTokenProvider {

    private final Key key;

    public MailTokenProvider(String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }


    public AuthToken createAuthToken(String id, Date expiry) {
        return new AuthToken(id, expiry, key);
    }

    public AuthToken createAuthToken(String id, AuthType role, Date expiry) {
        return new AuthToken(id, role, expiry, key);
    }

    public AuthToken convertAuthToken(String token) {
        return new AuthToken(token, key);
    }
}
