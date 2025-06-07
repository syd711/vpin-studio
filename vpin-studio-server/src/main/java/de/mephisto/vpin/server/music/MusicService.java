package de.mephisto.vpin.server.music;


import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.vpx.MusicInstallationUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@Service
public class MusicService {
  private final static Logger LOG = LoggerFactory.getLogger(MusicService.class);

  @Autowired
  private EmulatorService emulatorService;


  @Nullable
  public File getMusicFolder(Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
      GameEmulator emulator = emulatorService.getGameEmulator(game.getEmulatorId()); 
      return new File(emulator.getMusicFolder(), game.getRom());
    }
    return null;
  }

  public Boolean installMusic(@NonNull File out, int emulatorId, @NonNull UploaderAnalysis analysis, @Nullable String rom, boolean acceptAllAudio) throws IOException {
    GameEmulator gameEmulator = emulatorService.getGameEmulator(emulatorId);
    if (gameEmulator != null) {
      File musicFolder = gameEmulator.getMusicFolder();
      if (musicFolder.exists()) {
        MusicInstallationUtil.unpack(out, musicFolder, analysis, rom, analysis.getRelativeMusicPath(acceptAllAudio));
        return true;
      }
      else {
        LOG.warn("Skipped installation of music bundle, no music folder {} found.", musicFolder.getAbsolutePath());
      }
    }
    return false;
  }

  public boolean delete(Game game) {
    File musicFolder = getMusicFolder(game);
    if (FileUtils.deleteFolder(musicFolder)) {
      return true;
    }
    return false;
  }

}
