package de.mephisto.vpin.server.games.puppack;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.JCodec;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PupPack {
  private final static Logger LOG = LoggerFactory.getLogger(PupPack.class);

  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";

  private final String name;
  private final File screensPup;
  private final File triggersPup;
  private final File packFolder;

  private long size;

  public PupPack(@NonNull File packFolder) {
    this.name = packFolder.getName();
    screensPup = new File(packFolder, SCREENS_PUP);
    triggersPup = new File(packFolder, TRIGGERS_PUP);
    this.packFolder = packFolder;

    this.size = org.apache.commons.io.FileUtils.sizeOfDirectory(packFolder);
  }

  public long getSize() {
    return size;
  }

  public File getScreensPup() {
    return screensPup;
  }

  public File getTriggersPup() {
    return triggersPup;
  }

  public boolean delete() {
    if (packFolder.exists()) {
      return FileUtils.deleteFolder(packFolder);
    }
    return true;
  }

  public File getPupPackFolder() {
    return this.packFolder;
  }

  @Nullable
  public File exportDefaultPicture(@NonNull Game game, @NonNull File target) {
    File defaultPicture = new File(target, SystemService.DEFAULT_BACKGROUND);
    if (defaultPicture.exists() && defaultPicture.length() > 0) {
      return defaultPicture;
    }

    if (defaultPicture.exists() && defaultPicture.length() == 0) {
      return null;
    }

    if (!target.exists()) {
      target.mkdirs();
    }

    return resolveDefaultPictureFromPupVideo(game, defaultPicture);
  }

  @Nullable
  private File resolveDefaultPictureFromPupVideo(@NonNull Game game, @NonNull File defaultPicture) {
    PupDefaultVideoResolver resolver = new PupDefaultVideoResolver(this);
    File defaultVideo = resolver.findDefaultVideo();
    if (defaultVideo != null && defaultVideo.exists()) {
      boolean success = JCodec.export(defaultVideo, defaultPicture);
      if (success) {
        LOG.info("Successfully extracted default background image " + defaultPicture.getAbsolutePath());
        return defaultPicture;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "PUP Pack for \"" + name + "\"";
  }
}
