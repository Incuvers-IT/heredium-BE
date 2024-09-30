package art.heredium.domain.docent.model.dto.response;

import art.heredium.domain.docent.entity.DocentInfo;
import art.heredium.domain.common.model.Storage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetUserDocentInfoResponse {
    private String title;

    private List<Info> infos;

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private Storage thumbnail;
        private String title;
        private String writer;
        private String position;

        public Info(DocentInfo entity) {
            this.id = entity.getId();
            this.thumbnail = entity.getThumbnail();
            this.title = entity.getTitle();
            this.writer = entity.getWriter();
            this.position = entity.getPosition();
        }
    }

    public GetUserDocentInfoResponse(String title, List<Info> infos) {
        this.title = title;
        this.infos = infos;
    }
}
