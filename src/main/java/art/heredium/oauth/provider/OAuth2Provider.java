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
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.core.io.ClassPathResource;
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
  APPLE {
    @Override
    public ClientRegistration getClientRegistration(ClientRegistration clientRegistration) {

      String clientSecret = null;
      try {
        ClassPathResource resource = new ClassPathResource(clientRegistration.getPrivateKey());
        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();

        Date expirationDate =
            Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        clientSecret =
            Jwts.builder()
                .setHeaderParam("kid", clientRegistration.getKeyId()) // key id
                .setHeaderParam("alg", "ES256")
                .setIssuer(clientRegistration.getTeamId()) // team id
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientRegistration.getClientId()) // client id
                .signWith(converter.getPrivateKey(object), SignatureAlgorithm.ES256)
                .compact();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      clientRegistration.setClientSecret(clientSecret);
      return clientRegistration;
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
