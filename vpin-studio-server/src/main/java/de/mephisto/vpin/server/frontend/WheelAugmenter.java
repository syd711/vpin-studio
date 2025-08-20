package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.commons.fx.ImageUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WheelAugmenter {
  private final static Logger LOG = LoggerFactory.getLogger(WheelAugmenter.class);

  private final File thumbsFolder;
  private final File vsThumbsFolder;

  private final File wheelIcon;
  private final File wheelIconThumbnail;
  private final File wheelIconThumbnailSm;

  private final File backupWheelIcon;
  private final File backupWheelIconThumbnail;
  private final File backupWheelIconThumbnailSm;

  public WheelAugmenter(File wheelIcon) {
    this.wheelIcon = wheelIcon;

    thumbsFolder = new File(wheelIcon.getParentFile(), "pthumbs");
    vsThumbsFolder = new File(wheelIcon.getParentFile(), "vsthumbs");

    if (!thumbsFolder.exists()) {
      thumbsFolder.mkdirs();
    }

    if (!vsThumbsFolder.exists()) {
      vsThumbsFolder.mkdirs();
    }

    this.wheelIconThumbnail = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb.png");
    this.wheelIconThumbnailSm = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_sm.png");

    this.backupWheelIcon = new File(vsThumbsFolder, wheelIcon.getName());
    this.backupWheelIconThumbnail = new File(vsThumbsFolder, wheelIconThumbnail.getName());
    this.backupWheelIconThumbnailSm = new File(vsThumbsFolder, wheelIconThumbnailSm.getName());
  }


  public boolean isAugmented() {
    return backupWheelIcon.exists() && wheelIconThumbnail.exists();
  }

  public File getBackupWheelIcon() {
    return backupWheelIcon;
  }

  public void augment(File badgeFile) {
    if (!wheelIcon.exists()) {
      LOG.error("Could not augment wheel icon " + wheelIcon.getAbsolutePath() + ", file does not exist.");
      return;
    }

    if (backupWheelIcon.exists()) {
      LOG.info("Skipped wheel augmentation, because back file " + backupWheelIcon.getAbsolutePath() + " already exists.");
      return;
    }

    try {
      //always re-create
      if (backupWheelIcon.exists()) {
        deAugment(backupWheelIcon, wheelIcon);
        backupWheelIcon.delete();
      }
      if (backupWheelIconThumbnail.exists()) {
        deAugment(backupWheelIconThumbnail, wheelIconThumbnail);
        backupWheelIconThumbnail.delete();
      }
      if (backupWheelIconThumbnailSm.exists()) {
        deAugment(backupWheelIconThumbnailSm, wheelIconThumbnailSm);
        backupWheelIconThumbnailSm.delete();
      }

      if (wheelIcon.exists()) {
        FileUtils.copyFile(wheelIcon, backupWheelIcon);
      }
      if (wheelIconThumbnail.exists()) {
        FileUtils.copyFile(wheelIconThumbnail, backupWheelIconThumbnail);
      }
      if (wheelIconThumbnailSm.exists()) {
        FileUtils.copyFile(wheelIconThumbnailSm, backupWheelIconThumbnailSm);
      }

      BufferedImage bufferedWheelImage = ImageUtil.loadImage(wheelIcon);
      BufferedImage badgeIcon = ImageUtil.loadImage(badgeFile);

      int width = bufferedWheelImage.getWidth();
      int targetWidthForBadge = (width * 30 / 100);
      badgeIcon = ImageUtil.resizeImage(badgeIcon, targetWidthForBadge);

      int offset = (width * 5 / 100);
      bufferedWheelImage.getGraphics().drawImage(badgeIcon, width - offset - targetWidthForBadge, offset, null);
      ImageUtil.write(bufferedWheelImage, wheelIcon);


      //write large thumbnail
      BufferedImage thumbnail = ImageUtil.resizeImage(bufferedWheelImage, 225);
      thumbnail = ImageUtil.rotateLeft(thumbnail);
      ImageUtil.write(thumbnail, wheelIconThumbnail);

      //write small thumbnail
      BufferedImage thumbnailSm = ImageUtil.resizeImage(thumbnail, 90);
      ImageUtil.write(thumbnailSm, wheelIconThumbnailSm);

      LOG.info("Augmented " + wheelIconThumbnail.getAbsolutePath());
    } catch (Exception e) {
      LOG.error("Wheel augmentation failed: " + e.getMessage(), e);
    }
    resetThumbs();
  }

  private void resetThumbs() {
    try {
      if (thumbsFolder.exists()) {
        FileUtils.deleteDirectory(thumbsFolder);
      }
    } catch (IOException e) {
      LOG.info("Failed to reset thumbnails: " + e.getMessage(), e);
    }
  }

  public synchronized void deAugment() {
    boolean b1 = deAugment(backupWheelIcon, wheelIcon);
    boolean b2 = deAugment(backupWheelIconThumbnail, wheelIconThumbnail);
    boolean b3 = deAugment(backupWheelIconThumbnailSm, wheelIconThumbnailSm);
    if(b1 || b2 || b3) {
      resetThumbs();
    }
  }

  private boolean deAugment(File backup, File target) {
    if (backup.exists()) {
      if (target.exists() && !target.delete()) {
        LOG.warn("Failed to delete augmented file '" + target.getAbsolutePath() + "'");
        return false;
      }
      else {
        LOG.info("Deleted augmented file '" + target.getAbsolutePath() + "'");
      }

      try {
        FileUtils.copyFile(backup, target);
        LOG.info("Copied un-augmented wheel icon '" + backup.getAbsolutePath() + "' back to '" + target.getAbsolutePath() + "'");
        if (!backup.delete()) {
          LOG.error("Failed to delete backup file " + backup.getAbsolutePath());
        }
        return true;
      } catch (IOException e) {
        LOG.error("Failed to restore original wheel icon '" + target.getAbsolutePath() + "': " + e.getMessage(), e);
      }
    }
    return false;
  }
}
