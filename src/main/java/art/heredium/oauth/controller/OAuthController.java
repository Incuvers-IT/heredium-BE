package art.heredium.oauth.controller;

import java.beans.PropertyEditorSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import art.heredium.domain.account.model.dto.request.PostAccountSnsRequest;
import art.heredium.oauth.properties.OAuth2Properties;
import art.heredium.oauth.provider.OAuth2Provider;
import art.heredium.oauth.service.OAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuthController {

  private final OAuthService oAuthService;
  private final OAuth2Properties oAuth2Properties;

  @InitBinder
  private void initBinder(final WebDataBinder webdataBinder) {
    webdataBinder.registerCustomEditor(
        OAuth2Provider.class,
        new PropertyEditorSupport() {
          @Override
          public void setAsText(String url) {
            setValue(OAuth2Provider.fromUrl(url));
          }
        });
  }

  @GetMapping("/{provider}/authorization")
  public RedirectView authorization(
      RedirectView redirectView, @PathVariable(value = "provider") OAuth2Provider provider) {
    redirectView.setUrl(provider.getLoginUrl(oAuth2Properties.getRegistration()));
    return redirectView;
  }

  @PostMapping("/{provider}")
  public ResponseEntity<?> login(
      HttpServletResponse response,
      @PathVariable(value = "provider") OAuth2Provider provider,
      @RequestParam(value = "code") String code) {
    return ResponseEntity.ok(oAuthService.loginByCode(response, provider, code));
  }

  @PostMapping("/{provider}/sign-up")
  public ResponseEntity<?> signUp(
      HttpServletRequest request,
      HttpServletResponse response,
      @PathVariable(value = "provider") OAuth2Provider provider,
      @RequestBody @Valid PostAccountSnsRequest dto) {
    return ResponseEntity.ok(oAuthService.insert(request, response, provider, dto));
  }
}
