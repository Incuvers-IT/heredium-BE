package art.heredium.domain.log.model.dto.request;

import art.heredium.domain.log.type.LogAction;
import art.heredium.domain.log.type.LogSearchType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetLogSearchRequest {
    private List<LogAction> action = new ArrayList<>();
    private LogSearchType type = LogSearchType.ALL;
    private String text;
}