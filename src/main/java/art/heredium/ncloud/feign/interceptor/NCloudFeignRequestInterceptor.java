package art.heredium.ncloud.feign.interceptor;

import java.net.URI;

import lombok.RequiredArgsConstructor;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import art.heredium.ncloud.credential.Credentials;
import art.heredium.ncloud.credential.SignatureHeader;

@RequiredArgsConstructor
public class NCloudFeignRequestInterceptor implements RequestInterceptor {

  private final Credentials credentials;

  @Override
  public void apply(RequestTemplate template) {
    URI uri = URI.create(template.url());
    SignatureHeader signatureHeader =
        new SignatureHeader.SignatureHeaderBuilder(template.method(), uri).build();
    String signature = signatureHeader.toSignature(credentials);
    template.header("x-ncp-apigw-timestamp", String.valueOf(signatureHeader.getEpochTimestamps()));
    template.header("x-ncp-iam-access-key", credentials.getAccessKey());
    template.header("x-ncp-apigw-signature-v2", signature);
  }
}
