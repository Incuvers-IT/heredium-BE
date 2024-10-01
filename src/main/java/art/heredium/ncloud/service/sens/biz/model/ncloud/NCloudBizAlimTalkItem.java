package art.heredium.ncloud.service.sens.biz.model.ncloud;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NCloudBizAlimTalkItem {
  private List<NCloudBizAlimTalkArticle> list;
  private NCloudBizAlimTalkArticle summary;
}
