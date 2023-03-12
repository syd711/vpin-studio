package de.mephisto.vpin.server.games.puppack;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpa.JCodec;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class PupPack {
  private final static Logger LOG = LoggerFactory.getLogger(PupPack.class);

  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";

  private final SystemService systemService;
  private final Game game;

  public PupPack(@NonNull SystemService systemService, @NonNull Game game) {
    this.systemService = systemService;
    this.game = game;
  }

  public boolean isAvailable() {
    if (StringUtils.isEmpty(game.getRom())) {
      return false;
    }
    return getScreensPup().exists() && getTriggersPup().exists();
  }

  public File getPupPackFolder() {
    if (game.getRom() == null) {
      return null;
    }
    return new File(new File(systemService.getPinUPSystemFolder(), "PUPVideos"), game.getRom());
  }

  public File getScreensPup() {
    return new File(getPupPackFolder(), SCREENS_PUP);
  }

  public File getTriggersPup() {
    return new File(getPupPackFolder(), TRIGGERS_PUP);
  }

  @Override
  public String toString() {
    return "PUP Pack for \"" + game.getGameDisplayName() + "\"";
  }

  @Nullable
  public File exportDefaultPicture() {
    if (isAvailable()) {
      File mediaFolder = new File(systemService.getB2SImageExtractionFolder(), game.getRom());
      File defaultPicture = new File(mediaFolder, SystemService.DEFAULT_BACKGROUND);
      if (defaultPicture.exists() && defaultPicture.length() > 0) {
        return defaultPicture;
      }

      if (defaultPicture.exists() && defaultPicture.length() == 0) {
        return null;
      }

      if (!mediaFolder.exists()) {
        mediaFolder.mkdirs();
      }

      return resolveDefaultPictureFromPupVideo(defaultPicture);
    }
    return null;
  }

  @Nullable
  private File resolveDefaultPictureFromPupVideo(@NonNull File defaultPicture) {
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
}
