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

  private File wheelIcon;

  public WheelAugmenter(File wheelIcon) {
    this.wheelIcon = wheelIcon;
  }

  public void augment(File badgeFile) {
    if (!wheelIcon.exists()) {
      LOG.error("Could not augment wheel icon " + wheelIcon.getAbsolutePath() + ", file does not exist.");
    }

    File original = new File(wheelIcon.getParentFile(), wheelIcon.getName() + ".orig");

    try {
      //always re-cretaed
      if (original.exists()) {
        original.delete();
      }

      FileUtils.copyFile(wheelIcon, original);

      BufferedImage bufferedImage = ImageUtil.loadImage(wheelIcon);
      BufferedImage badgeIcon = ImageUtil.loadImage(badgeFile);

      int width = bufferedImage.getWidth();
      badgeIcon = ImageUtil.resizeImage(badgeIcon, width / 2);

      bufferedImage.getGraphics().drawImage(badgeIcon, width / 2, 0, null);
      ImageUtil.write(bufferedImage, wheelIcon);

      clearThumbs();
    } catch (Exception e) {
      LOG.error("Wheel augmentation failed: " + e.getMessage(), e);
    }
  }

  public void deAugment() {
    File original = new File(wheelIcon.getParentFile(), wheelIcon.getName() + ".orig");
    if (original.exists()) {
      wheelIcon.delete();
      boolean result = original.renameTo(wheelIcon);
      if (result) {
        clearThumbs();
        LOG.info("Reverted augmented wheelicon " + wheelIcon.getAbsolutePath());
      }
    }
  }

  private void clearThumbs() {
    File thumbsFolder = new File(wheelIcon.getParentFile(), "pthumbs");
    File thumb = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb." + FilenameUtils.getExtension(wheelIcon.getName()));
    if (thumb.exists()) {
      LOG.info("Deleted thumb " + thumb.getAbsolutePath());
      thumb.delete();
    }
    else {
      LOG.info("No thumb image found " + thumb.getAbsolutePath());
    }

    File thumbSm = new File(thumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_sm." + FilenameUtils.getExtension(wheelIcon.getName()));
    if (thumbSm.exists()) {
      LOG.info("Deleted thumb " + thumbSm.getAbsolutePath());
      thumbSm.delete();
    }
    else {
      LOG.info("No thumb sm image found " + thumb.getAbsolutePath());
    }
  }
}
