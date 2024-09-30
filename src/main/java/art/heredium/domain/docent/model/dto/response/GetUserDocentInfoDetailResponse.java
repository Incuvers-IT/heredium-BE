package art.heredium.domain.docent.model.dto.response;

import art.heredium.domain.docent.entity.DocentInfo;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.model.dto.response.NextRecord;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserDocentInfoDetailResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private String writer;
    private String position;
    private String intro;
    private Storage audio;
    private Storage map;
    private NextRecord next;
    private NextRecord prev;

    public GetUserDocentInfoDetailResponse(DocentInfo entity, NextRecord previousRecord, NextRecord nextRecord) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.title = entity.getTitle();
        this.writer = entity.getWriter();
        this.position = entity.getPosition();
        this.intro = entity.getIntro();
        this.audio = entity.getAudio();
        this.map = entity.getMap();
        this.next = nextRecord;
        this.prev = previousRecord;
    }
}
