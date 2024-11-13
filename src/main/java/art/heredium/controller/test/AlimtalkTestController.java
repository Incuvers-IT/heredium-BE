package art.heredium.controller.test;

import java.util.Arrays;
import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.ncloud.service.sens.biz.component.NCloudBizAlimTalk;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkTemplate;

@RestController
@RequiredArgsConstructor
public class AlimtalkTestController {
  private static final List<String> ALLOWED_TEMPLATE_CODES_FOR_TESTING =
      Arrays.asList("HEREDIUM021", "HEREDIUM018", "HEREDIUM019", "HEREDIUM020");
  final NCloudBizAlimTalk nCloudBizAlimTalk;
  final AlimTalkTestService alimTalkTestService;

  @GetMapping("/api/test/alim-talk/templates/{templateCode}")
  public List<NCloudBizAlimTalkTemplate> getProdTemplate(
      @PathVariable(value = "templateCode") String templateCode) {
    final String prodFriendId = "@heredium";
    return nCloudBizAlimTalk.getTemplate(prodFriendId, templateCode);
  }

  @PostMapping("/api/test/alim-talk/{templateCode}/{phoneNumber}/send")
  public ResponseEntity<Void> sendMessageToAlimTalk(
      @PathVariable(value = "templateCode") String templateCode,
      @PathVariable(value = "phoneNumber") String phoneNumber) {
    this.validateTemplateCode(templateCode);
    this.alimTalkTestService.sendMessageToAlimTalk(templateCode, phoneNumber);
    return ResponseEntity.noContent().build();
  }

  private void validateTemplateCode(@NonNull String templateCode) {
    if (!ALLOWED_TEMPLATE_CODES_FOR_TESTING.contains(templateCode))
      throw new RuntimeException(
          "Invalid template code. TemplateCode must in [HEREDIUM021, HEREDIUM018, HEREDIUM019, HEREDIUM020]");
  }
}
