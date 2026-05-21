package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.fx.apng.ApngDecodeResult;
import de.mephisto.vpin.commons.fx.apng.ApngUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static de.mephisto.vpin.commons.fx.apng.ApngUtil.decodeFrames;

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
    return backupWheelIcon.exists();
  }

  public File getBackupWheelIcon() {
    return backupWheelIcon;
  }

  public void augment(File badgeFile, boolean rotate) {
    if (!wheelIcon.exists()) {
      LOG.error("Could not augment wheel icon {}, file does not exist.", wheelIcon.getAbsolutePath());
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

      resetThumbs();
      wheelIconThumbnail.getParentFile().mkdirs();

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
      if (rotate) {
        thumbnail = ImageUtil.rotateLeft(thumbnail);
      }
      ImageUtil.write(thumbnail, wheelIconThumbnail);

      //write small thumbnail
      BufferedImage thumbnailSm = ImageUtil.resizeImage(thumbnail, 90);
      ImageUtil.write(thumbnailSm, wheelIconThumbnailSm);

      LOG.info("Augmented {}", wheelIconThumbnail.getAbsolutePath());

      resetThumbs();
    }
    catch (Exception e) {
      LOG.error("Wheel augmentation failed: {}", e.getMessage(), e);
    }
  }

  private void resetThumbs() {
    try {
      if (thumbsFolder.exists()) {
        FileUtils.deleteDirectory(thumbsFolder);
      }
    }
    catch (IOException e) {
      LOG.info("Failed to reset thumbnails: {}", e.getMessage(), e);
    }
  }

  public synchronized void deAugment() {
    boolean b1 = deAugment(backupWheelIcon, wheelIcon);
    boolean b2 = deAugment(backupWheelIconThumbnail, wheelIconThumbnail);
    boolean b3 = deAugment(backupWheelIconThumbnailSm, wheelIconThumbnailSm);
    if (b1 || b2 || b3) {
      resetThumbs();
    }
  }

  private boolean deAugment(File backup, File target) {
    if (backup.exists()) {
      if (target.exists()) {
        if (!target.delete()) {
          LOG.warn("Failed to delete augmented file '{}'", target.getAbsolutePath());
          return false;
        }
        LOG.info("Deleted augmented file '{}'", target.getAbsolutePath());
      }

      try {
        FileUtils.copyFile(backup, target);
        LOG.info("Copied un-augmented wheel icon '{}' back to '{}'", backup.getAbsolutePath(), target.getAbsolutePath());
        if (!backup.delete()) {
          LOG.error("Failed to delete backup file {}", backup.getAbsolutePath());
        }
        return true;
      }
      catch (IOException e) {
        LOG.error("Failed to restore original wheel icon '{}': {}", target.getAbsolutePath(), e.getMessage(), e);
      }
    }
    return false;
  }

    /**
     * Overlays {@code badgeFile} onto every frame of the APNG wheel icon,
     * re-encodes the result in place, and writes static PNG thumbnails.
     *
     * <p>If the icon has been augmented before it is first restored from the
     * backup, so calling {@code augment} multiple times is safe.</p>
     *
     * @param badgeFile the badge image to composite (any format readable by {@link ImageUtil})
     * @param rotate    when {@code true} the large thumbnail is rotated 90° left
     */
    public void augmentApng(File badgeFile, boolean rotate) {

        if (!wheelIcon.exists()) {
            LOG.error("Could not augment wheel icon {}, file does not exist.", wheelIcon.getAbsolutePath());
            return;
        }

        try {
            // Always re-create: if a prior augmentation exists, restore the originals first.
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

            // Snapshot current files before modifying anything.
            if (wheelIcon.exists()) FileUtils.copyFile(wheelIcon, backupWheelIcon);
            if (wheelIconThumbnail.exists()) FileUtils.copyFile(wheelIconThumbnail, backupWheelIconThumbnail);
            if (wheelIconThumbnailSm.exists()) FileUtils.copyFile(wheelIconThumbnailSm, backupWheelIconThumbnailSm);

            resetThumbs();
            wheelIconThumbnail.getParentFile().mkdirs();
            ApngDecodeResult result = decodeFrames(new FileInputStream(wheelIcon));

            List<BufferedImage> frames = result.getFrames();
            List<Integer> delaysMs = result.getDelaysMs();
            int numPlays = result.getNumPlays();


            // --- 2. Prepare badge: 30 % of canvas width, positioned top-right at 5 % inset ---
            int canvasWidth  = frames.getFirst().getWidth();
            int badgeWidth   = canvasWidth * 30 / 100;
            int inset        = canvasWidth * 5  / 100;
            int badgeX       = canvasWidth - inset - badgeWidth;


            BufferedImage badge = ImageUtil.loadImage(badgeFile);
            badge = ImageUtil.resizeImage(badge, badgeWidth);

            // --- 3. Composite badge onto every frame ---
            for (BufferedImage frame : frames) {
                Graphics2D g2d = frame.createGraphics();
                g2d.drawImage(badge, badgeX, inset, null);
                g2d.dispose();
            }

            // --- 4. Re-encode as APNG and overwrite the wheel icon ---
            byte[] apngBytes = ApngUtil.encodeApng(frames, delaysMs, numPlays);
            try (FileOutputStream fos = new FileOutputStream(wheelIcon)) {
                fos.write(apngBytes);
            }

            // --- 5. Write static PNG thumbnails from the first frame ---
            BufferedImage thumbnail = ImageUtil.resizeImage(frames.get(0), 225);
            if (rotate) {
                thumbnail = ImageUtil.rotateLeft(thumbnail);
            }
            ImageUtil.write(thumbnail, wheelIconThumbnail);

            BufferedImage thumbnailSm = ImageUtil.resizeImage(thumbnail, 90);
            ImageUtil.write(thumbnailSm, wheelIconThumbnailSm);

            LOG.info("Augmented APNG {}", wheelIcon.getAbsolutePath());
            resetThumbs();

        } catch (Exception e) {
            LOG.error("APNG wheel augmentation failed: {}", e.getMessage(), e);
        }
    }
}
