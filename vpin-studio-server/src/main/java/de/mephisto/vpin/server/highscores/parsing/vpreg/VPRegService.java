package de.mephisto.vpin.server.highscores.parsing.vpreg;

import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VPRegService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FolderLookupService folderLookupService;

  private final Map<Integer, List<String>> vpRegEntries = new HashMap<>();

  public boolean isValid(Game game) {
    VPRegFile reg = folderLookupService.getVPRegFileForGame(game);
    if (reg != null) {
      return reg.isValid();
    }
    return false;
  }

  @Nullable
  public String getVPRegJson(@NonNull Game game) {
    VPRegFile reg = folderLookupService.getVPRegFileForGame(game);
    if (reg != null) {
      return reg.toJson();
    }
    return null;
  }

  @Nullable
  public VPRegFile getVPRegFile(@NonNull Game game) {
    return folderLookupService.getVPRegFileForGame(game);
  }

  public boolean resetHighscores(Game game, long score) {
    VPRegFile vpRegFile = folderLookupService.getVPRegFileForGame(game);
    if (vpRegFile != null) {
      return vpRegFile.resetHighscores(score);
    }
    return false;
  }

  public void restore(@NonNull Game game, @NonNull String json) {
    VPRegFile vpRegFile = folderLookupService.getVPRegFileForGame(game);
    if (vpRegFile != null) {
      vpRegFile.restore(json);
    }
  }

  public void refreshVPRegEntries() {
    try {
      vpRegEntries.clear();
      List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        File vpRegFile = gameEmulator.getVPRegFile();
        VPRegFile regFile = new VPRegFile(vpRegFile, null, null);
        vpRegEntries.put(gameEmulator.getId(), regFile.getEntries());
      }
      LOG.info("VPRegService read " + vpRegEntries.size() + " VPReg.stg entries for emulators");
    }
    catch (Exception e) {
      LOG.error("Failed to refresh emulator VPReg entries: " + e.getMessage(), e);
    }
  }
}
