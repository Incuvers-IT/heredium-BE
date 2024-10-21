package art.heredium.ncloud.bean;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;

@Component
@Profile("!local")
public class ProductCloudStorage implements S3Storage {

  @Override
  public void move(CloudStorage cloudStorage, String oldPath, String newPath) {
    AmazonS3 s3 = cloudStorage.getSession();
    s3.copyObject(cloudStorage.getBucket(), oldPath, cloudStorage.getBucket(), newPath);
    s3.setObjectAcl(cloudStorage.getBucket(), newPath, CannedAccessControlList.PublicRead);
    cloudStorage.delete(oldPath);
  }
}
