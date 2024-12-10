package art.heredium.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.core.gson.GsonLocalDateTimeAdapter;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.ncloud.bean.CloudStorage;

@Component
public class Constants {

  public static String PROFILE_ACTIVE;
  public static String S3_URL;
  /** 전시 시작전 {BOOKING_DATE} 일 전부터 예매가능한 날짜로 설정 */
  public static long BOOKING_DATE = 90;

  public static final String COMPANY_PREFIX = "법인회원-";

  @Value("${spring.profiles.active}")
  public void setProfileActive(String value) {
    PROFILE_ACTIVE = value;
  }

  @Value("${ncloud.storage.s3-url}")
  public void sets3Url(String value) {
    S3_URL = value;
  }

  public static LocalDateTime getNow() {
    return LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  }

  public static Gson getGson() {
    Gson gson =
        new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
            .create();
    return gson;
  }

  public static String getIP() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null) {
      ip = request.getRemoteAddr();
    }
    if (ip == null) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  public static List<String> getImageNameFromHtml(String contents) {
    String[] images = StringUtils.substringsBetween(contents, "src=\"" + S3_URL, "\"");
    return images != null ? Arrays.asList(images) : new ArrayList<>();
  }

  public static List<String> getEditorTempFile(String contents, String path) {
    List<String> list =
        getImageNameFromHtml(contents).stream()
            .filter(x -> x.startsWith(FilePathType.EDITOR.getPath()))
            .collect(Collectors.toList());

    return list;
  }

  public static String replaceTempFile(String contents, List<String> tempFiles, String path) {
    for (String tempFile : tempFiles) {
      String moveFilePath = tempFile.replace(FilePathType.EDITOR.getPath(), path);
      contents = contents.replace(tempFile, moveFilePath);
    }
    return contents;
  }

  public static List<String> getRemoveImage(String beforeHtml, String afterHtml) {
    List<String> beforeImage = getImageNameFromHtml(beforeHtml);
    List<String> afterImage = getImageNameFromHtml(afterHtml);
    List<String> removeFiles = new ArrayList<>();
    for (String s : beforeImage) {
      if (afterImage.stream().noneMatch(x -> x.equals(s))) {
        removeFiles.add(s);
      }
    }
    return removeFiles;
  }

  public static void moveFileFromTemp(
      CloudStorage cloudStorage, Storage storage, String fileFolderPath) {
    if (storage != null) {
      String movedFile = moveTempFile(cloudStorage, fileFolderPath, storage.getSavedFileName());
      if (movedFile != null) {
        storage.setSavedFileName(movedFile);
      }
      Storage.ResizeImage resizeImage = storage.getResizeImage();
      if (resizeImage != null) {
        if (resizeImage.getSmall() != null) {
          String movedSmallFile =
              moveTempFile(cloudStorage, fileFolderPath, resizeImage.getSmall());
          if (movedSmallFile != null) {
            resizeImage.setSmall(movedSmallFile);
          }
        }
        if (resizeImage.getMedium() != null) {
          String movedMediumFile =
              moveTempFile(cloudStorage, fileFolderPath, resizeImage.getMedium());
          if (movedMediumFile != null) {
            resizeImage.setMedium(movedMediumFile);
          }
        }
        if (resizeImage.getLarge() != null) {
          String movedLargeFile =
              moveTempFile(cloudStorage, fileFolderPath, resizeImage.getLarge());
          if (movedLargeFile != null) {
            resizeImage.setLarge(movedLargeFile);
          }
        }
      }
    }
  }

  public static String moveImageToNewPlace(
      CloudStorage cloudStorage, String tempOriginalUrl, String newPath) {
    Storage storage = new Storage();
    storage.setSavedFileName(tempOriginalUrl);
    Constants.moveFileFromTemp(cloudStorage, storage, newPath);
    return storage.getSavedFileName();
  }

  private static String moveTempFile(
      CloudStorage cloudStorage, String fileFolderPath, String savedFileName) {
    if (savedFileName.startsWith(FilePathType.TEMP.getPath())) {
      String moveFilePath = savedFileName.replace(FilePathType.TEMP.getPath(), fileFolderPath);
      cloudStorage.move(savedFileName, moveFilePath);
      return moveFilePath;
    }
    return null;
  }

  public static boolean valid(MultipartFile multipartFile, FilePathType type) {
    if (multipartFile == null) return false;

    if (type.getMegabytes() != null
        && multipartFile.getSize() > type.getMegabytes() * 1024 * 1024) {
      throw new ApiException(ErrorCode.MAX_UPLOAD_SIZE, "파일 용랑초과");
    }

    String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
    if (extension == null
        || type.getExtension() != null
            && type.getExtension().stream().noneMatch(extension::equalsIgnoreCase)) {
      throw new ApiException(ErrorCode.BAD_VALID, "파일 확장자 미지원");
    }

    return true;
  }

  /**
   *
   *
   * <pre>
   * a@gmail.com                     = *@gmail.com
   * ab@gmail.com                    = a*@gmail.com
   * abc@gmail.com                   = a*c@gmail.com
   * abcd@gmail.com                  = a*cd@gmail.com
   * abcde@gmail.com                 = a**de@gmail.com
   * abcdef@gmail.com                = ab**ef@gmail.com
   * abcdefg@gmail.com               = ab**efg@gmail.com
   * abcdefghij@gmail.com            = abc***ghij@gmail.com
   * </pre>
   */
  public static String emailMasking(String email) {
    String[] split = email.split("@");
    StringBuilder buf = new StringBuilder(split[0]);
    int len = buf.length(), start = 0, end = 0;
    if (len > 0) {
      start = (len / 3) + ((len < 3) ? 1 : 0);
      end = (int) (start + Math.round(((double) len) / 3));
    }
    return buf.replace(start, end, String.join("", Collections.nCopies(end - start, "*")))
        + (split.length > 1 ? ("@" + split[1]) : "");
  }

  public static String getUUID() {
    return getNow().format(DateTimeFormatter.ofPattern("yyMMdd"))
        + RandomStringUtils.randomAlphanumeric(10);
  }

  public static <T> List<List<T>> separateList(List<T> list, int maxLoop) {
    List<List<T>> result = new ArrayList<>();
    if (list.size() > 0) {
      int max = list.size() / maxLoop;
      int remainder = list.size() % maxLoop;
      for (int i = 1; i <= max; i++) {
        List<T> ii = list.subList(maxLoop * (i - 1), maxLoop * i);
        result.add(ii);
      }
      if (remainder > 0) {
        List<T> ii = list.subList(maxLoop * max, (maxLoop * max) + remainder);
        result.add(ii);
      }
    }
    return result;
  }

  public static String phone(String src) {
    if (src == null) {
      return "";
    }
    if (src.length() == 8) {
      return src.replaceFirst("^([0-9]{4})([0-9]{4})$", "$1-$2");
    } else if (src.length() == 12) {
      return src.replaceFirst("(^[0-9]{4})([0-9]{4})([0-9]{4})$", "$1-$2-$3");
    }
    return src.replaceFirst("(^02|[0-9]{3})([0-9]{3,4})([0-9]{4})$", "$1-$2-$3");
  }
}
