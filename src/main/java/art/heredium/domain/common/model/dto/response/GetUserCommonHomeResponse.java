package art.heredium.domain.common.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.DateState;
import art.heredium.domain.common.type.ProjectStateType;
import art.heredium.domain.exhibition.entity.Exhibition;
import art.heredium.domain.program.entity.Program;
import art.heredium.domain.ticket.type.TicketKindType;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserCommonHomeResponse {
    private List<Popup> popups;
    private List<Slide> slides;
    private List<Project> projects;
    private List<Event> events;
    private List<Notice> notices;

    @Getter
    @Setter
    public static class Popup {
        private Storage pcImage;
        private String pcImageAlt;
        private Storage mobileImage;
        private String mobileImageAlt;
        private Boolean isNewTab;
        private String link;
        private String title;
        private Boolean isHideToday;

        public Popup(art.heredium.domain.popup.entity.Popup entity) {
            this.pcImage = entity.getPcImage();
            this.pcImageAlt = entity.getPcImageAlt();
            this.mobileImage = entity.getMobileImage();
            this.mobileImageAlt = entity.getMobileImageAlt();
            this.isNewTab = entity.getIsNewTab();
            this.link = entity.getLink();
            this.title = entity.getTitle();
            this.isHideToday = entity.getIsHideToday();
        }
    }

    @Getter
    @Setter
    public static class Slide {
        private Storage pcImage;
        private String pcImageAlt;
        private Storage mobileImage;
        private String mobileImageAlt;
        private Boolean isUseButton;
        private Boolean isNewTab;
        private String link;
        private String title;
        private String subtitle;
        private String schedule;
        private LocalDateTime startDate, endDate;

        public Slide(art.heredium.domain.slide.entity.Slide entity) {
            this.pcImage = entity.getPcImage();
            this.pcImageAlt = entity.getPcImageAlt();
            this.mobileImage = entity.getMobileImage();
            this.mobileImageAlt = entity.getMobileImageAlt();
            this.isUseButton = entity.getIsUseButton();
            this.isNewTab = entity.getIsNewTab();
            this.link = entity.getLink();
            this.title = entity.getTitle();
            this.subtitle = entity.getSubtitle();
            this.schedule = entity.getSchedule();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
        }
    }

    @Getter
    @Setter
    private static class Project {
        private Long id;
        private Storage thumbnail;
        private String title;
        private String subtitle;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ProjectStateType state;
        private TicketKindType kind;

        private Project(Exhibition entity) {
            this.id = entity.getId();
            this.thumbnail = entity.getThumbnail();
            this.subtitle = entity.getSubtitle();
            this.title = entity.getTitle();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.state = entity.getState();
            this.kind = TicketKindType.EXHIBITION;
        }

        private Project(Program entity) {
            this.id = entity.getId();
            this.thumbnail = entity.getThumbnail();
            this.subtitle = entity.getSubtitle();
            this.title = entity.getTitle();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.state = entity.getState();
            this.kind = TicketKindType.PROGRAM;
        }
    }

    @Getter
    @Setter
    private static class Event {
        private Long id;
        private Storage thumbnail;
        private String title;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private DateState state;

        private Event(art.heredium.domain.event.entity.Event entity) {
            this.id = entity.getId();
            this.thumbnail = entity.getThumbnail();
            this.title = entity.getTitle();
            this.startDate = entity.getStartDate();
            this.endDate = entity.getEndDate();
            this.state = entity.getState();
        }
    }

    @Getter
    @Setter
    private static class Notice {
        private Long id;
        private String title;
        private String contents;
        private LocalDateTime postDate;
        private Boolean isNotice;

        private Notice(art.heredium.domain.notice.entity.Notice entity) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            String contents = Jsoup.parse(entity.getContents()).text();
            this.contents = contents.substring(0, Math.min(contents.length(), 50));
            this.postDate = entity.getPostDate();
            this.isNotice = entity.getIsNotice();
        }
    }

    public GetUserCommonHomeResponse(List<art.heredium.domain.popup.entity.Popup> popups,
                                     List<art.heredium.domain.slide.entity.Slide> slides,
                                     List<Exhibition> exhibitions,
                                     List<Program> programs,
                                     List<art.heredium.domain.event.entity.Event> events,
                                     List<art.heredium.domain.notice.entity.Notice> notices) {
        this.popups = popups.stream()
                .map(Popup::new)
                .collect(Collectors.toList());
        this.slides = slides.stream()
                .map(Slide::new)
                .collect(Collectors.toList());
        this.events = events.stream()
                .map(Event::new)
                .collect(Collectors.toList());
        this.notices = notices.stream()
                .map(Notice::new)
                .collect(Collectors.toList());

        List<Project> unionProjects = new ArrayList<>();
        unionProjects.addAll(exhibitions.stream().map(Project::new).collect(Collectors.toList()));
        unionProjects.addAll(programs.stream().map(Project::new).collect(Collectors.toList()));
        Comparator<Project> stateComparator = Comparator.comparingInt(x -> x.getState().getCode());
        Comparator<Project> dateComparator = Comparator.comparing(Project::getStartDate);
        unionProjects.sort(stateComparator.thenComparing(dateComparator));
        this.projects = unionProjects;
    }
}
