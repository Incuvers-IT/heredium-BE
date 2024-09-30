package art.heredium.domain.docent.model.dto.request;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.HallType;
import art.heredium.ncloud.bean.CloudStorage;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PostAdminDocentRequest {
    private Storage thumbnail;
    @NotBlank
    @Length(max = 100)
    private String title;
    @NotNull
    @Length(max = 100)
    private String subtitle;
    @NotNull
    private List<HallType> halls = new ArrayList<>();
    @NotNull
    private Boolean isEnabled;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;
    @NotNull
    private List<PostAdminDocentRequest.@Valid Info> infos = new ArrayList<>();

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private Storage thumbnail;
        @Length(max = 100)
        private String title;
        @NotNull
        @Length(max = 30)
        private String writer;
        @NotNull
        @Length(max = 100)
        private String position;
        @NotNull
        @Length(max = 3000)
        private String intro;
        private Storage audio;
        private Storage map;
    }

    public void validate(CloudStorage cloudStorage) {
        validateImage(cloudStorage, this.thumbnail);
        this.getInfos().forEach(info -> {
            validateImage(cloudStorage, info.getThumbnail());
            validateImage(cloudStorage, info.getAudio());
            validateImage(cloudStorage, info.getMap());
        });
    }

    private void validateImage(CloudStorage cloudStorage, Storage image) {
        if (image != null && !cloudStorage.isExistObject(image.getSavedFileName())) {
            throw new ApiException(ErrorCode.S3_NOT_FOUND, image);
        }
    }
}