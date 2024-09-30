package art.heredium.domain.slide.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.slide.entity.Slide;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAdminSlideDetailResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String schedule;
    private Boolean isEnabled;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Storage pcImage;
    private String pcImageAlt;
    private Storage mobileImage;
    private String mobileImageAlt;
    private Boolean isUseButton;
    private Boolean isNewTab;
    private String link;
    private String lastModifiedName;
    private LocalDateTime lastModifiedDate;
    private String createdName;
    private LocalDateTime createdDate;


    public GetAdminSlideDetailResponse(Slide entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.subtitle = entity.getSubtitle();
        this.schedule = entity.getSchedule();
        this.isEnabled = entity.getIsEnabled();
        this.startDate = entity.getStartDate();
        this.endDate = entity.getEndDate();
        this.pcImage = entity.getPcImage();
        this.pcImageAlt = entity.getPcImageAlt();
        this.mobileImage = entity.getMobileImage();
        this.mobileImageAlt = entity.getMobileImageAlt();
        this.isUseButton = entity.getIsUseButton();
        this.isNewTab = entity.getIsNewTab();
        this.link = entity.getLink();
        this.lastModifiedName = entity.getLastModifiedName();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.createdName = entity.getCreatedName();
        this.createdDate = entity.getCreatedDate();
    }
}
