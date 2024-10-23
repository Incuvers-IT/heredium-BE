package art.heredium.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import art.heredium.core.util.Constants;
import art.heredium.core.util.ImageResize;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import art.heredium.domain.common.type.ResizeImageType;
import art.heredium.ncloud.bean.CloudStorage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/file")
public class FileController {

  private final CloudStorage cloudStorage;

  @PostMapping
  public ResponseEntity uploadFiles(
      @RequestParam("files") List<MultipartFile> files,
      @RequestParam(value = "type", required = false) ResizeImageType type) {
    List<Storage> result = new ArrayList<>();
    files.forEach(
        file -> {
          if (Constants.valid(file, FilePathType.TEMP)) {
            Storage storage = cloudStorage.upload(file, FilePathType.TEMP.getPath());
            if (type != null) {
              Storage.ResizeImage resizeImage = new Storage.ResizeImage();
              if (type.getSmall() != null) {
                Storage small =
                    cloudStorage.upload(
                        ImageResize.resizeImage(file, type.getSmall()[0], type.getSmall()[1]),
                        FilePathType.TEMP.getPath());
                resizeImage.setSmall(small.getSavedFileName());
              }
              if (type.getMedium() != null) {
                Storage medium =
                    cloudStorage.upload(
                        ImageResize.resizeImage(file, type.getMedium()[0], type.getMedium()[1]),
                        FilePathType.TEMP.getPath());
                resizeImage.setMedium(medium.getSavedFileName());
              }
              if (type.getLarge() != null) {
                Storage large =
                    cloudStorage.upload(
                        ImageResize.resizeImage(file, type.getLarge()[0], type.getLarge()[1]),
                        FilePathType.TEMP.getPath());
                resizeImage.setLarge(large.getSavedFileName());
              }
              storage.setResizeImage(resizeImage);
            }
            result.add(storage);
          }
        });
    return ResponseEntity.ok(result.size() == 1 ? result.stream().findFirst() : result);
  }

  @PostMapping("/image")
  public List<String> uploadImage(@RequestParam("files") List<MultipartFile> files) {
    List<String> result = new ArrayList<>();
    files.forEach(
        file -> {
          if (Constants.valid(file, FilePathType.EDITOR)) {
            Storage storage = cloudStorage.upload(file, FilePathType.EDITOR.getPath());
            result.add(cloudStorage.getS3Url() + storage.getSavedFileName());
          }
        });
    return result;
  }

  @GetMapping(path = "/download")
  public ResponseEntity download(
      @RequestParam("path") String path,
      @RequestParam(value = "fileName", required = false) String fileName)
      throws IOException {
    byte[] bytes = cloudStorage.getByte(path);
    String downloadName =
        URLEncoder.encode(fileName != null ? fileName : path, "UTF-8").replaceAll("\\+", "%20");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    httpHeaders.setContentLength(bytes.length);
    httpHeaders.setContentDispositionFormData("attachment", downloadName);

    return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
  }

  @GetMapping("/template/company-membership/download")
  public ResponseEntity<Resource> downloadCompanyTemplate() throws IOException {
    Resource resource = new ClassPathResource("company_template.xlsx");

    HttpHeaders headers = new HttpHeaders();
    headers.add(
        HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=membership_upload_template.xlsx");

    return ResponseEntity.ok()
        .headers(headers)
        .contentLength(resource.contentLength())
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(resource);
  }
}
