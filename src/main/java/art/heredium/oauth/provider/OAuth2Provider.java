package art.heredium.oauth.provider;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.bouncycastle.openssl.PEMKeyPair;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriUtils;

import com.google.gson.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import art.heredium.domain.common.converter.GenericTypeConverter;
import art.heredium.domain.common.type.PersistableEnum;
import art.heredium.oauth.info.OAuth2UserInfo;
import art.heredium.oauth.info.impl.AppleOAuth2UserInfo;
import art.heredium.oauth.info.impl.GoogleOAuth2UserInfo;
import art.heredium.oauth.info.impl.KakaoOAuth2UserInfo;
import art.heredium.oauth.info.impl.NaverOAuth2UserInfo;

public enum OAuth2Provider implements PersistableEnum<String> {
  GOOGLE {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {
      return clientRegistration;
    }

    @Override
    public OAuth2UserInfo getUserInfo(Map<String, Object> attributes) {
      return new GoogleOAuth2UserInfo(attributes);
    }
  },
  NAVER {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {
      return clientRegistration;
    }

    @Override
    public OAuth2UserInfo getUserInfo(Map<String, Object> attributes) {
      return new NaverOAuth2UserInfo(attributes);
    }
  },
  KAKAO {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {
      return clientRegistration;
    }

    @Override
    public OAuth2UserInfo getUserInfo(Map<String, Object> attributes) {
      return new KakaoOAuth2UserInfo(attributes);
    }
  },
  
  // 여기
  APPLE {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {
      String clientSecret;
      try {
        // ✔ 1) 경로 해석(가벼운 방식): classpath:/file: 지원 + 폴백
        String location = clientRegistration.getPrivateKey();
        Resource resource = resolveResource(location);

        // ✔ 2) 스트림으로 키 읽기 (fat JAR 안전)
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKey privateKey;
        try (InputStream in = resource.getInputStream();
             Reader pemReader = new InputStreamReader(in, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(pemReader)) {

          Object parsed = pemParser.readObject();
          if (parsed == null) {
            throw new IllegalStateException("Apple private key is empty or unreadable: " + location);
          }
          if (parsed instanceof PrivateKeyInfo) {
            privateKey = converter.getPrivateKey((PrivateKeyInfo) parsed);
          } else if (parsed instanceof PEMKeyPair) {
            privateKey = converter.getKeyPair((PEMKeyPair) parsed).getPrivate();
          } else {
            throw new IllegalStateException("Unsupported key format: " + parsed.getClass());
          }
        }

        Date expirationDate = Date.from(
                LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant()
        );

        clientSecret = Jwts.builder()
                .setHeaderParam("kid", clientRegistration.getKeyId())
                .setHeaderParam("alg", "ES256")
                .setIssuer(clientRegistration.getTeamId())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientRegistration.getClientId())
                .signWith(privateKey, SignatureAlgorithm.ES256)
                .compact();

      } catch (IOException e) {
        throw new RuntimeException("Failed to load Apple private key", e);
      }

      clientRegistration.setClientSecret(clientSecret);
      return clientRegistration;
    }

    // ─────────────────────────────────────────────────────
    // 가벼운 리졸버: ResourceLoader 주입 없이도 OK
    // ─────────────────────────────────────────────────────
    private Resource resolveResource(String location) {
      if (location == null || location.isEmpty()) {
        throw new IllegalArgumentException("Apple privateKey location is empty");
      }

      // 1) 명시 접두사 처리
      if (location.startsWith("classpath:")) {
        String path = location.substring("classpath:".length());
        Resource res = new ClassPathResource(path);
        if (!res.exists()) throw new IllegalArgumentException("Classpath resource not found: " + location);
        return res;
      }
      if (location.startsWith("file:")) {
        String path = location.substring("file:".length());
        Resource res = new FileSystemResource(path);
        if (!res.exists()) throw new IllegalArgumentException("File resource not found: " + location);
        return res;
      }

      // 2) 접두사 없으면: classpath → file 순서로 시도
      Resource cp = new ClassPathResource(location);
      if (cp.exists()) return cp;

      Resource fs = new FileSystemResource(location);
      if (fs.exists()) return fs;

      throw new IllegalArgumentException("Resource not found (tried classpath: and file:): " + location);
    }

    @Override
    public OAuth2UserInfo getUserInfo(Map<String, Object> attributes) {
      return new AppleOAuth2UserInfo(attributes);
    }
  },

  EMAIL {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {
      return null;
    }

    @Override
    public OAuth2UserInfo getUserInfo(Map<String, Object> attributes) {
      return null;
    }
  };

  @Override
  public String getValue() {
    return this.name();
  }

  public static class Converter extends GenericTypeConverter<OAuth2Provider, String> {
    public Converter() {
      super(OAuth2Provider.class);
    }
  }

  public static OAuth2Provider fromUrl(String url) {
    return Arrays.stream(values())
        .filter(e -> e.name().equalsIgnoreCase(url))
        .findAny()
        .orElseThrow(() -> new RuntimeException(url + "타입을 찾을 수 없습니다."));
  }

  public abstract ClientRegistration getClientRegistration(ClientRegistration clientRegistration);

  public abstract OAuth2UserInfo getUserInfo(Map<String, Object> attributes);

  public String getLoginUrl(Map<String, ClientRegistration> registration) {
    DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
    uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
    ClientRegistration clientRegistration =
        this.getClientRegistration(registration.get(this.name().toLowerCase()));
    Map<String, Object> parameters = getParameters(clientRegistration); // Not encoded
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    parameters.forEach(
        (k, v) ->
            queryParams.set(encodeQueryParam(k), encodeQueryParam(String.valueOf(v)))); // Encoded
    UriBuilder uriBuilder =
        uriBuilderFactory
            .uriString(clientRegistration.getAuthorizationUri())
            .queryParams(queryParams);
    URI uri = uriBuilder.build();
    return uri.toString();
  }

  private Map<String, Object> getParameters(ClientRegistration clientRegistration) {
    Map<String, Object> parameters = new LinkedHashMap<>();
    String responseType = clientRegistration.getAuthorizationGrantType();
    if (clientRegistration.getAuthorizationGrantType().equalsIgnoreCase("authorization_code")) {
      responseType = "code";
    } else if (clientRegistration.getAuthorizationGrantType().equalsIgnoreCase("implicit")) {
      responseType = "token";
    }
    parameters.put("response_type", responseType);
    parameters.put("client_id", clientRegistration.getClientId());
    parameters.put("redirect_uri", clientRegistration.getRedirectUri());
    if (!CollectionUtils.isEmpty(clientRegistration.getScope())) {
      parameters.put(
          "scope", StringUtils.collectionToDelimitedString(clientRegistration.getScope(), " "));
    }
    return parameters;
  }

  private String encodeQueryParam(String value) {
    return UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
  }

  public Map<String, Object> getUserInfoFromJwt(
      Map<String, ClientRegistration> registration, String idToken) {
    ClientRegistration clientRegistration = registration.get(this.name().toLowerCase());

    StringBuffer result = new StringBuffer();
    try {
      URL url = new URL(clientRegistration.getJwkSetUri());
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      String line = "";
      while ((line = br.readLine()) != null) {
        result.append(line);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException();
    }

    Gson gson = new Gson();
    JsonObject keys = gson.fromJson(result.toString(), JsonObject.class);
    JsonArray keyArray = (JsonArray) keys.get("keys");

    String[] decodeArray = idToken.split("\\.");
    String header = new String(Base64.getDecoder().decode(decodeArray[0]));

    JsonElement kid = JsonParser.parseString(header).getAsJsonObject().get("kid");
    JsonElement alg = JsonParser.parseString(header).getAsJsonObject().get("alg");

    JsonObject avaliableObject = null;
    for (int i = 0; i < keyArray.size(); i++) {
      JsonObject appleObject = (JsonObject) keyArray.get(i);
      JsonElement appleKid = appleObject.get("kid");
      JsonElement appleAlg = appleObject.get("alg");

      if (Objects.equals(appleKid, kid) && Objects.equals(appleAlg, alg)) {
        avaliableObject = appleObject;
        break;
      }
    }

    if (ObjectUtils.isEmpty(avaliableObject)) throw new IllegalArgumentException();

    PublicKey publicKey = this.getPublicKey(avaliableObject);

    Claims userInfo =
        Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(idToken).getBody();
    return userInfo;
  }

  public PublicKey getPublicKey(JsonObject object) {
    String nStr = object.get("n").toString();
    String eStr = object.get("e").toString();

    byte[] nBytes = Base64.getUrlDecoder().decode(nStr.substring(1, nStr.length() - 1));
    byte[] eBytes = Base64.getUrlDecoder().decode(eStr.substring(1, eStr.length() - 1));

    BigInteger n = new BigInteger(1, nBytes);
    BigInteger e = new BigInteger(1, eBytes);

    try {
      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
      return publicKey;
    } catch (Exception exception) {
      throw new IllegalArgumentException();
    }
  }
}
