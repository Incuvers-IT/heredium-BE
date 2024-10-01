package art.heredium.ncloud.credential;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;
import lombok.NoArgsConstructor;

import art.heredium.ncloud.error.NCloudKitErrorCode;
import art.heredium.ncloud.error.NCloudKitException;

@Getter
public class SignatureHeader {

  private final Long epochTimestamps;
  private final String method;
  private final URI uri; // ex) /api/v1/mails

  public SignatureHeader(SignatureHeaderBuilder builder) {
    this.epochTimestamps = builder.epochTimestamps;
    this.method = builder.method;
    this.uri = builder.uri;
  }

  public String toSignature(Credentials credentials) {
    URI uri = this.uri;
    String pathWithQuery = uri.getRawPath();
    if (uri.getRawQuery() != null) {
      pathWithQuery = pathWithQuery + "?" + uri.getRawQuery();
    }
    StringBuilder message =
        new StringBuilder()
            .append(this.method)
            .append(" ")
            .append(pathWithQuery)
            .append("\n")
            .append(this.epochTimestamps)
            .append("\n")
            .append(credentials.getAccessKey());

    try {
      SecretKeySpec signingKey =
          new SecretKeySpec(
              credentials.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);

      byte[] rawHmac = mac.doFinal(message.toString().getBytes(StandardCharsets.UTF_8));
      return com.amazonaws.util.Base64.encodeAsString(rawHmac);
      //            return Base64.encodeBase64String(rawHmac); // InipaySample_v.1.3.jar 에서 덮어씌워 해당
      // 코드가 작동안됨
    } catch (Exception e) {
      throw new NCloudKitException(
          NCloudKitErrorCode.SDK_ERROR,
          "Failed to make signature for IAM credentials: " + e.getMessage());
    }
  }

  @NoArgsConstructor
  public static class SignatureHeaderBuilder {

    private Long epochTimestamps;
    private String method;
    private URI uri;

    public SignatureHeaderBuilder(String method, URI uri) {
      this.epochTimestamps = Instant.now().toEpochMilli();
      this.method = method;
      this.uri = uri;
    }

    public SignatureHeader build() {
      if (epochTimestamps == null)
        throw new NCloudKitException(NCloudKitErrorCode.CREDENTIAL_TIMSTAMP_INVALID);
      if (method == null)
        throw new NCloudKitException(NCloudKitErrorCode.CREDENTIAL_METHOD_INAVLID);

      return new SignatureHeader(this);
    }
  }
}
