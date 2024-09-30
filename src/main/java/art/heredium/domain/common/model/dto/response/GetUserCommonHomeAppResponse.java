package art.heredium.domain.common.model.dto.response;

import art.heredium.domain.popup.entity.Popup;
import art.heredium.domain.slide.entity.Slide;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class GetUserCommonHomeAppResponse {
    private List<GetUserCommonHomeResponse.Popup> popups;
    private List<GetUserCommonHomeResponse.Slide> slides;

    public GetUserCommonHomeAppResponse(List<Popup> popups, List<Slide> slides) {
        this.popups = popups.stream()
                .map(GetUserCommonHomeResponse.Popup::new)
                .collect(Collectors.toList());
        this.slides = slides.stream()
                .map(GetUserCommonHomeResponse.Slide::new)
                .collect(Collectors.toList());
    }
}
