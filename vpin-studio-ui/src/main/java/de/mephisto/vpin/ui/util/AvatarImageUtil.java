package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.CommonImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.invoke.MethodHandles;

public class AvatarImageUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static File createAvatar(File avatarFile) throws Exception {
    BufferedImage bufferedImage = CommonImageUtil.loadBackground(avatarFile);
    BufferedImage crop = crop(bufferedImage, 1, 1);
    BufferedImage scaled = CommonImageUtil.resizeImage(crop, 100, 100);
    File tempFile = File.createTempFile("avatar", ".png");
    CommonImageUtil.write(scaled, tempFile);
    return tempFile;
  }

  private static BufferedImage crop(BufferedImage image, int xRatio, int yRatio) {
    int width = image.getWidth();
    int height = image.getHeight();

    int targetWidth = width;
    int targetHeight = width / xRatio * yRatio;
    if (targetHeight > height) {
      targetWidth = image.getHeight() / yRatio * xRatio;
      targetHeight = height;
    }

    int x = 0;
    int y = 0;
    if (targetWidth < width) {
      x = (width / 2) - (targetWidth / 2);
    }

    LOG.info("Cropping image from " + width + "x" + height + " to " + targetWidth + "x" + targetHeight);
    return image.getSubimage(x, y, targetWidth, targetHeight);
  }
}
