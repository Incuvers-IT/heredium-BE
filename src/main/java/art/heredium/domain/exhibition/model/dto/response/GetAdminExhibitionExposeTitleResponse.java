package art.heredium.domain.exhibition.model.dto.response;

import art.heredium.domain.exhibition.entity.Exhibition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminExhibitionExposeTitleResponse {
    private Long id;
    private String title;

    public GetAdminExhibitionExposeTitleResponse(Exhibition entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
    }
}