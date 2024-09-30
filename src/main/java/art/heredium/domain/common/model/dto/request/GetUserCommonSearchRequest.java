package art.heredium.domain.common.model.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class GetUserCommonSearchRequest {
    private String text;
    @NotNull
    private SearchDateType type;

    public GetUserCommonSearchRequest(String text, SearchDateType type) {
        this.text = text;
        this.type = type;
    }

    @Getter
    public enum SearchDateType {
        EXHIBITION(0, "전시"),
        PROGRAM(1, "프로그램"),
        EVENT(2, "이벤트"),
        NOTICE(3, "공지사항"),
        ;

        private int code;
        private String desc;

        SearchDateType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }
}