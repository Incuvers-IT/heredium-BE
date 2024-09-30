package art.heredium.domain.exhibition.model.dto.response;

import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.HallType;
import art.heredium.domain.common.type.ProjectStateType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class GetAdminExhibitionResponse {
    private Long id;
    private Storage thumbnail;
    private String title;
    private List<HallType> halls;
    private Boolean isEnabled;
    private ProjectStateType state;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime bookingDate;
    private Integer total;
    private LocalDateTime createdDate;
    private String createdName;
    private LocalDateTime lastModifiedDate;
    private String lastModifiedName;

    @QueryProjection
    public GetAdminExhibitionResponse(Long id, Storage thumbnail, String title, List<HallType> halls, Boolean isEnabled, LocalDateTime startDate, LocalDateTime endDate, LocalDateTime bookingDate, Integer totalBooking, LocalDateTime createdDate, String createdName, LocalDateTime lastModifiedDate, String lastModifiedName) {
        this.id = id;
        this.thumbnail = thumbnail;
        this.title = title;
        this.halls = halls;
        this.isEnabled = isEnabled;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bookingDate = bookingDate;
        this.total = totalBooking;
        this.createdDate = createdDate;
        this.createdName = createdName;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifiedName = lastModifiedName;
        this.state = ProjectStateType.getState(this.getStartDate(), this.getEndDate(), this.getBookingDate());
    }
}
