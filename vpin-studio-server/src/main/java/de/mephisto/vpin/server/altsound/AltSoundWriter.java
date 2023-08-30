package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class AltSoundWriter {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundWriter.class);

  private final File gameAltSoundFolder;

  public AltSoundWriter(@NonNull Game gameAltSoundFolder) {
    this.gameAltSoundFolder = gameAltSoundFolder;
  }

  public void save(@NonNull AltSound altSound) {
    try {
      org.apache.commons.io.FileUtils.writeStringToFile(altSoundCsv, altSound.toCSV(), StandardCharsets.UTF_8);
      LOG.info("Written ALTSound for " + game.getGameDisplayName());
      return null;
    } catch (Exception e) {
      LOG.error("Error writing CSV " + altSoundCsv.getAbsolutePath() + ": " + e.getMessage(), e);
    }
  }
}
