package art.heredium.ncloud.service.sens.biz.model.ncloud;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NCloudBizAlimTalkItem {
    private List<NCloudBizAlimTalkArticle> list;
    private NCloudBizAlimTalkArticle summary;
}
