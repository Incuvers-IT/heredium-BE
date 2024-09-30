package art.heredium.core.util;

import javax.servlet.http.HttpServletRequest;

public class HeaderUtil {

    public final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    public static String getAccessToken(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_AUTHORIZATION);

        if (headerValue == null) {
            return null;
        }

        if (headerValue.startsWith(TOKEN_PREFIX)) {
            String token = headerValue.substring(TOKEN_PREFIX.length());
            if (token.equals("null")) {
                return null;
            }
            return token;
        }

        return null;
    }
}
