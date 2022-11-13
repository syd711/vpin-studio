package de.mephisto.vpin.server.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadUtil {
  private final static Logger LOG = LoggerFactory.getLogger(UploadUtil.class);

  public static Boolean upload(MultipartFile file, File target) {
    byte[] bytes = new byte[0];
    try {
      bytes = file.getBytes();
      if (target.exists() && !target.delete()) {
        throw new UnsupportedOperationException("Failed to delete existing target file " + target.getAbsolutePath());
      }

      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.write(bytes, fileOutputStream);
      fileOutputStream.close();
      LOG.info("Written uploaded file: " + target.getAbsolutePath() + ", byte size was " + bytes.length);
    } catch (Exception e) {
      LOG.error("Failed to store asset: " + e.getMessage() + ", byte size was " + bytes.length, e);
    }
    return true;
  }

  public static byte[] resizeImageUpload(MultipartFile file, int size) throws IOException {
    String suffix = FilenameUtils.getExtension(file.getOriginalFilename());
    File tempFile = File.createTempFile("vpin-studio-upload", suffix);
    FileOutputStream out = new FileOutputStream(tempFile);
    byte[] orig = file.getBytes();
    IOUtils.write(orig, out);
    out.close();

    BufferedImage image = ImageUtil.loadImage(tempFile);
    if(image.getHeight() > size & image.getWidth() > size) {
      BufferedImage crop = ImageUtil.resizeImage(image, size);
      return ImageUtil.toBytes(crop);
    }

    return orig;
  }
}
