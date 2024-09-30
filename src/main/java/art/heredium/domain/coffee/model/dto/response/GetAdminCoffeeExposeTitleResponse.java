package art.heredium.domain.coffee.model.dto.response;

import art.heredium.domain.coffee.entity.Coffee;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminCoffeeExposeTitleResponse {
    private Long id;
    private String title;

    public GetAdminCoffeeExposeTitleResponse(Coffee entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
    }
}