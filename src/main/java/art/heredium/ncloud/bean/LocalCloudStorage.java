package art.heredium.ncloud.bean;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;

@Component
@Profile("local")
public class LocalCloudStorage implements S3Storage {

  @Override
  public void move(CloudStorage cloudStorage, String oldPath, String newPath) {
    AmazonS3 s3 = cloudStorage.getSession();

    CopyObjectRequest copyObjectRequest =
        new CopyObjectRequest(cloudStorage.getBucket(), oldPath, cloudStorage.getBucket(), newPath)
            .withCannedAccessControlList(CannedAccessControlList.PublicRead);
    s3.copyObject(copyObjectRequest);

    cloudStorage.delete(oldPath);
  }
}
