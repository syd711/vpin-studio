package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.util.ImageUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;

public class WheelAugmenter {
  private final static Logger LOG = LoggerFactory.getLogger(WheelAugmenter.class);
  private final File thumbsFolder;

  private File wheelIcon;
  private File wheelIconThumbnail;
  private File wheelIconThumbnailSm;

  private File backupWheelIcon;
  private File backupWheelIconThumbnail;
  private File backupWheelIconThumbnailSm;

  public WheelAugmenter(File wheelIcon) {
    this.wheelIcon = wheelIcon;

    thumbsFolder = new File(wheelIcon.getParentFile(), "pthumbs");
    this.wheelIconThumbnail = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb.png");
    this.wheelIconThumbnailSm = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_sm.png");

    this.backupWheelIcon = new File(wheelIcon.getParentFile(), FilenameUtils.getBaseName(wheelIcon.getName()) + "_orig.png");
    this.backupWheelIconThumbnail = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_orig.png");
    this.backupWheelIconThumbnailSm = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_sm_orig.png");
  }

  public void augment(File badgeFile) {
    if (!wheelIcon.exists()) {
      LOG.error("Could not augment wheel icon " + wheelIcon.getAbsolutePath() + ", file does not exist.");
    }

    if (!thumbsFolder.exists()) {
      thumbsFolder.mkdirs();
    }

    try {
      //always re-create
      if (backupWheelIcon.exists()) {
        backupWheelIcon.delete();
      }
      if (backupWheelIconThumbnail.exists()) {
        backupWheelIconThumbnail.delete();
      }
      if (backupWheelIconThumbnailSm.exists()) {
        backupWheelIconThumbnailSm.delete();
      }

      if(wheelIcon.exists()) {
        FileUtils.copyFile(wheelIcon, backupWheelIcon);
      }
      if(wheelIconThumbnail.exists()) {
        FileUtils.copyFile(wheelIconThumbnail, backupWheelIconThumbnail);
      }
      if(wheelIconThumbnailSm.exists()) {
        FileUtils.copyFile(wheelIconThumbnailSm, backupWheelIconThumbnailSm);
      }

      BufferedImage bufferedImage = ImageUtil.loadImage(wheelIcon);
      BufferedImage badgeIcon = ImageUtil.loadImage(badgeFile);

      int width = bufferedImage.getWidth();
      badgeIcon = ImageUtil.resizeImage(badgeIcon, width / 2);

      bufferedImage.getGraphics().drawImage(badgeIcon, width / 2, 0, null);
      ImageUtil.write(bufferedImage, wheelIcon);


      //write large thumbnail
      BufferedImage thumbnail = ImageUtil.resizeImage(bufferedImage, 225);
      thumbnail = ImageUtil.rotateLeft(thumbnail);
      ImageUtil.write(thumbnail, wheelIconThumbnail);

      //write small thumbnail
      BufferedImage thumbnailSm = ImageUtil.resizeImage(thumbnail, 90);
      ImageUtil.write(thumbnailSm, wheelIconThumbnailSm);

    } catch (Exception e) {
      LOG.error("Wheel augmentation failed: " + e.getMessage(), e);
    }
  }

  public void deAugment() {
    if (backupWheelIcon.exists()) {
      wheelIcon.delete();
      boolean result = backupWheelIcon.renameTo(wheelIcon);
      if (result) {
        LOG.info("Reverted augmented wheel icon " + wheelIcon.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to reverted augmented wheel icon " + wheelIcon.getAbsolutePath());
      }
    }

    if (backupWheelIconThumbnail.exists()) {
      wheelIconThumbnail.delete();
      boolean result = backupWheelIconThumbnail.renameTo(wheelIconThumbnail);
      if (result) {
        LOG.info("Reverted augmented wheel icon " + wheelIconThumbnail.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to reverted augmented wheel icon " + wheelIconThumbnail.getAbsolutePath());
      }
    }

    if (backupWheelIconThumbnailSm.exists()) {
      wheelIconThumbnailSm.delete();
      boolean result = backupWheelIconThumbnailSm.renameTo(wheelIconThumbnailSm);
      if (result) {
        LOG.info("Reverted augmented wheel icon " + wheelIconThumbnailSm.getAbsolutePath());
      }
      else {
        LOG.warn("Failed to reverted augmented wheel icon " + wheelIconThumbnailSm.getAbsolutePath());
      }
    }
  }
}
