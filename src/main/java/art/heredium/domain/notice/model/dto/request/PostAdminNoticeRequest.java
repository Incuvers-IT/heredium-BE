package art.heredium.domain.notice.model.dto.request;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Length;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.common.model.Storage;
import art.heredium.ncloud.bean.CloudStorage;

@Getter
@Setter
public class PostAdminNoticeRequest {
  @NotBlank
  @Length(max = 100)
  private String title;

  @NotNull private Boolean isEnabled;
  @NotNull private LocalDateTime postDate;
  @NotNull private Boolean isNotice;
  @NotBlank private String contents;

  @NotNull
  @Size(max = 3)
  private List<Storage> files = new ArrayList<>();

  public void validate(CloudStorage cloudStorage) {
    this.setFiles(this.getFiles().stream().filter(Objects::nonNull).collect(Collectors.toList()));

    this.getFiles()
        .forEach(
            file -> {
              validateImage(cloudStorage, file);
            });
  }

  private void validateImage(CloudStorage cloudStorage, Storage image) {
    if (image != null && !cloudStorage.isExistObject(image.getSavedFileName())) {
      throw new ApiException(ErrorCode.S3_NOT_FOUND, image);
    }
  }
}
