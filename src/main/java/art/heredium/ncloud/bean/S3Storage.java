package art.heredium.ncloud.bean;

public interface S3Storage {
  void move(CloudStorage cloudStorage, String oldPath, String newPath);
}
