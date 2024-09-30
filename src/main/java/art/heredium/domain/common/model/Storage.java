package art.heredium.domain.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import lombok.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class Storage {

    private Long fileSize;
    private String originalFileName;
    private String savedFileName;
    private String mimeType;
    private ResizeImage resizeImage;

    public List<String> getAllFileName() {
        List<String> list = new ArrayList<>();
        if (savedFileName != null) {
            list.add(savedFileName);
        }
        if (resizeImage != null) {
            list.add(resizeImage.getSmall());
            list.add(resizeImage.getMedium());
            list.add(resizeImage.getLarge());
        }
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class ResizeImage {
        private String large;
        private String medium;
        private String small;
    }

    public Storage(MultipartFile file, String path) {
        String fileName = FilenameUtils.getName(file.getOriginalFilename());
        String objectName = generateUUID(Objects.requireNonNull(fileName));
        this.fileSize = file.getSize();
        this.originalFileName = fileName;

        TikaConfig tikaConfig = null;
        try {
            InputStream bufferedIn = new BufferedInputStream(file.getInputStream());
            tikaConfig = new TikaConfig();
            Detector detector = tikaConfig.getDetector();

            Metadata metadata = new Metadata();
            metadata.add(TikaCoreProperties.RESOURCE_NAME_KEY, this.originalFileName);
            MediaType mediaType = detector.detect(bufferedIn, metadata);
            this.mimeType = mediaType.toString();
            this.savedFileName = path + "/" + objectName;
            bufferedIn.close();
        } catch (TikaException | IOException e) {
            throw new ApiException(ErrorCode.INVALID_FILE);
        }
    }

    private static String generateUUID(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        String extension = dotIndex == -1 ? fileName : fileName.substring(dotIndex);
        UUID uuid = UUID.randomUUID();
        return uuid.toString() + extension;
    }
}
