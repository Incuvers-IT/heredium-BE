package art.heredium.controller.test;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import art.heredium.ncloud.service.sens.biz.component.NCloudBizAlimTalk;
import art.heredium.ncloud.service.sens.biz.model.ncloud.NCloudBizAlimTalkTemplate;

@RestController
@RequiredArgsConstructor
public class AlimtalkTestController {
  final NCloudBizAlimTalk nCloudBizAlimTalk;

  @GetMapping("/api/test/alim-talk/templates/{templateCode}")
  public List<NCloudBizAlimTalkTemplate> getProdTemplate(
      @PathVariable(value = "templateCode") String templateCode) {
    final String prodFriendId = "@heredium";
    return nCloudBizAlimTalk.getTemplate(prodFriendId, templateCode);
  }
}
