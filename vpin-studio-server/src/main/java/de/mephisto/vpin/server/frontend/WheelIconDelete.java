package de.mephisto.vpin.server.frontend;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class WheelIconDelete {
  private final static Logger LOG = LoggerFactory.getLogger(WheelIconDelete.class);

  private final File wheelIcon;
  private final File wheelIconThumbnail;
  private final File wheelIconThumbnailSm;

  public WheelIconDelete(File wheelIcon) {
    this.wheelIcon = wheelIcon;

    File pThumbsFolder = new File(wheelIcon.getParentFile(), "pthumbs");

    this.wheelIconThumbnail = new File(pThumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb.png");
    this.wheelIconThumbnailSm = new File(pThumbsFolder, FilenameUtils.getBaseName(wheelIcon.getName()) + "_thumb_sm.png");
  }

  public void delete() {
    if (wheelIconThumbnail.exists() && wheelIconThumbnail.delete()) {
      LOG.info("Deleted wheel thumbnail icon {}", wheelIconThumbnail.getAbsolutePath());
    }
    if (wheelIconThumbnailSm.exists() && wheelIconThumbnailSm.delete()) {
      LOG.info("Deleted wheel thumbnail sm icon {}", wheelIconThumbnailSm.getAbsolutePath());
    }
  }
}
