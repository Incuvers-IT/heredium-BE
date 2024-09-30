package art.heredium.domain.docent.model.dto.response;

import art.heredium.domain.docent.entity.Docent;
import art.heredium.domain.docent.entity.DocentInfo;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.HallType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetAdminDocentDetailResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private String subtitle;
    private List<HallType> halls;
    private Boolean isEnabled;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Info> infos;
    private String createdName;
    private LocalDateTime createdDate;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private Storage thumbnail;
        private String title;
        private String writer;
        private String position;
        private String intro;
        private Storage audio;
        private Storage map;

        public Info(DocentInfo entity) {
            this.id = entity.getId();
            this.thumbnail = entity.getThumbnail();
            this.title = entity.getTitle();
            this.writer = entity.getWriter();
            this.position = entity.getPosition();
            this.intro = entity.getIntro();
            this.audio = entity.getAudio();
            this.map = entity.getMap();
        }
    }

    public GetAdminDocentDetailResponse(Docent entity) {
        this.id = entity.getId();
        this.thumbnail = entity.getThumbnail();
        this.title = entity.getTitle();
        this.subtitle = entity.getSubtitle();
        this.halls = entity.getHalls();
        this.isEnabled = entity.getIsEnabled();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.infos = entity.getInfos().stream().map(Info::new).collect(Collectors.toList());
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }
}
