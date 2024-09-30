package art.heredium.domain.slide.model.dto.request;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.common.model.Storage;
import art.heredium.ncloud.bean.CloudStorage;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class PostAdminSlideRequest {
    @NotBlank
    @Length(max = 100)
    private String title;
    @NotNull
    @Length(max = 100)
    private String subtitle;
    @NotNull
    @Length(max = 100)
    private String schedule;
    @NotNull
    private Boolean isEnabled;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;
    @NotNull
    private Storage pcImage;
    @NotNull
    @Length(max = 100)
    private String pcImageAlt;
    @NotNull
    private Storage mobileImage;
    @NotNull
    @Length(max = 100)
    private String mobileImageAlt;
    @NotNull
    private Boolean isUseButton;
    @NotNull
    private Boolean isNewTab;
    @NotNull
    @Length(max = 2048)
    private String link;

    public void validate(CloudStorage cloudStorage) {
        if (!StringUtils.isBlank(this.getLink()) && !this.link.startsWith("http://") && !this.link.startsWith("https://")) {
            throw new ApiException(ErrorCode.BAD_VALID, "link");
        }
        validateImage(cloudStorage, this.getPcImage());
        validateImage(cloudStorage, this.getMobileImage());
    }

    private void validateImage(CloudStorage cloudStorage, Storage image) {
        if (image == null || !cloudStorage.isExistObject(image.getSavedFileName())) {
            throw new ApiException(ErrorCode.S3_NOT_FOUND, image);
        }
    }
}