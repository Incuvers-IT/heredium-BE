package art.heredium.ncloud.bean;

import art.heredium.core.config.error.entity.ApiException;
import art.heredium.core.config.error.entity.ErrorCode;
import art.heredium.domain.common.model.Storage;
import art.heredium.domain.common.type.FilePathType;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class CloudStorage {

  private final long deleteTempFileTimePeriod = 1 * 24 * 60 * 60 * 1000L;

  @Value("${ncloud.storage.end-point}")
  public String END_POINT;

  @Value("${ncloud.storage.region}")
  private String REGION_NAME;

  @Value("${ncloud.credentials.access-key}")
  private String ACCESS_KEY;

  @Value("${ncloud.credentials.secret-key}")
  private String SECRET_KEY;

  @Value("${ncloud.storage.bucket}")
  private String BUCKET;

  @Value("${ncloud.storage.s3-url}")
  private String S3_URL;

  public String getS3Url() {
    return this.S3_URL;
  }

  private AmazonS3 getSession() {
    AmazonS3 s3 =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(END_POINT, REGION_NAME))
            .withCredentials(
                new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
            .withPathStyleAccessEnabled(true)
            .build();
    return s3;
  }

  public List<Storage> upload(List<MultipartFile> files, String path) {
    List<Storage> storages = new ArrayList<>();
    files.forEach(
        file -> {
          Storage storage = upload(file, path);
          if (storage != null) {
            storages.add(storage);
          }
        });
    return storages;
  }

  public Storage upload(MultipartFile file, String path) {
    if (file == null
        || file.isEmpty()
        || file.getOriginalFilename() == null
        || file.getOriginalFilename().trim().length() < 1) {
      throw new ApiException(ErrorCode.INVALID_FILE, "파일 업로드에 실패했습니다.");
    }

    AmazonS3 s3 = getSession();
    Storage storage = new Storage(file, path);
    ObjectMetadata objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(file.getSize());
    objectMetadata.setContentType(file.getContentType());

    try (InputStream inputStream = file.getInputStream()) {
      s3.putObject(
          new PutObjectRequest(BUCKET, storage.getSavedFileName(), inputStream, objectMetadata)
              .withCannedAcl(CannedAccessControlList.PublicRead));
    } catch (IOException e) {
      throw new ApiException(ErrorCode.INVALID_FILE, "파일 업로드에 실패했습니다.");
    }

    return storage;
  }

  // 파일 다운로드, byte[]로 받음.
  public byte[] getByte(String path) throws IOException {
    AmazonS3 s3 = getSession();
    S3Object s3Object = s3.getObject(new GetObjectRequest(BUCKET, path));
    S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
    byte[] bytesArray = IOUtils.toByteArray(s3ObjectInputStream);
    s3ObjectInputStream.close();
    return bytesArray;
  }

  public byte[] zipBytes(Map<String, byte[]> input) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);

    for (Map.Entry<String, byte[]> e : input.entrySet()) {
      byte[] value = e.getValue();
      ZipEntry entry = new ZipEntry(e.getKey());
      entry.setSize(value.length);
      zos.putNextEntry(entry);
      zos.write(value);
    }

    zos.closeEntry();
    zos.close();
    return baos.toByteArray();
  }

  public void delete(List<String> filePaths) {
    if (filePaths == null || filePaths.size() == 0) {
      return;
    }
    AmazonS3 s3 = getSession();
    List<DeleteObjectsRequest.KeyVersion> keys =
        filePaths.stream().map(DeleteObjectsRequest.KeyVersion::new).collect(Collectors.toList());
    DeleteObjectsRequest request = new DeleteObjectsRequest(BUCKET).withKeys(keys);
    try {
      s3.deleteObjects(request);
    } catch (SdkClientException e) {
      // e.printStackTrace();
    }
  }

  public void delete(String path) {
    AmazonS3 s3 = getSession();
    try {
      DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(BUCKET, path);
      s3.deleteObject(deleteObjectRequest);
    } catch (SdkClientException e) {
      // e.printStackTrace();
    }
  }

  public void deleteFolder(String folderPath) {
    AmazonS3 s3 = getSession();
    ObjectListing listObjects = s3.listObjects(BUCKET, folderPath);
    List<S3ObjectSummary> summaries = listObjects.getObjectSummaries();
    List<DeleteObjectsRequest.KeyVersion> keys =
        summaries.stream()
            .map(x -> new DeleteObjectsRequest.KeyVersion(x.getKey()))
            .collect(Collectors.toList());

    DeleteObjectsRequest deleteObjectRequest =
        new DeleteObjectsRequest(BUCKET).withKeys(keys).withQuiet(false);
    try {
      s3.deleteObjects(deleteObjectRequest);
    } catch (SdkClientException e) {
      e.printStackTrace();
    }
  }

  public void move(String oldPath, String newPath) {
    AmazonS3 s3 = getSession();
    // 1) 객체 복사
    s3.copyObject(BUCKET, oldPath, BUCKET, newPath);
    // 2) 퍼블릭 읽기 ACL 강제 설정
    s3.setObjectAcl(BUCKET, newPath, CannedAccessControlList.PublicRead);
    // 3) 원본 삭제
    delete(oldPath);
  }

  public boolean isExistObject(String objectName) {
    AmazonS3 s3 = getSession();
    return s3.doesObjectExist(BUCKET, objectName);
  }

  public void deleteTempFile() {
    AmazonS3 s3 = getSession();
    ObjectListing listObjects = s3.listObjects(BUCKET, FilePathType.TEMP.getPath());
    List<S3ObjectSummary> summaries = listObjects.getObjectSummaries();
    List<DeleteObjectsRequest.KeyVersion> keys =
        summaries.stream()
            .filter(
                x ->
                    new Date().getTime() - x.getLastModified().getTime() > deleteTempFileTimePeriod)
            .map(x -> new DeleteObjectsRequest.KeyVersion(x.getKey()))
            .collect(Collectors.toList());
    if (keys.size() > 0) {
      DeleteObjectsRequest deleteObjectRequest =
          new DeleteObjectsRequest(BUCKET).withKeys(keys).withQuiet(false);
      s3.deleteObjects(deleteObjectRequest);
    }
  }
}
