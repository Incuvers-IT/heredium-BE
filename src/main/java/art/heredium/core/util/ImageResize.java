package art.heredium.core.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.io.FilenameUtils;

public final class ImageResize {

  public static MultipartFile resizeImage(MultipartFile file, Integer width, Integer height) {
    try {
      BufferedImage originalImage = ImageIO.read(file.getInputStream());

      int originWidth = originalImage.getWidth(null);
      int originHeight = originalImage.getHeight(null);
      double originWidthRatio = (double) originWidth / originHeight;
      if (width < originWidth && height < originHeight) {
        if (originWidthRatio > 1) {
          width = originWidth * height / originHeight;
        } else {
          height = originHeight * width / originWidth;
        }
      } else {
        width = originWidth;
        height = originHeight;
      }
      return resizeImage(file, originalImage, width, height);
    } catch (Exception e) {
      return file;
    }
  }

  private static MultipartImage resizeImage(
      MultipartFile file, BufferedImage originalImage, int width, int height) throws IOException {
    Image resizeImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    int imgType =
        (originalImage.getTransparency() == Transparency.OPAQUE)
            ? BufferedImage.TYPE_INT_RGB
            : BufferedImage.TYPE_INT_ARGB;
    BufferedImage newImage = new BufferedImage(width, height, imgType);
    Graphics graphics = newImage.getGraphics();
    graphics.drawImage(resizeImage, 0, 0, null);
    graphics.dispose();

    String formatName = FilenameUtils.getExtension(file.getOriginalFilename());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(newImage, formatName, baos);

    return new MultipartImage(
        baos.toByteArray(),
        file.getName(),
        file.getOriginalFilename(),
        file.getContentType(),
        baos.toByteArray().length);
  }
}
