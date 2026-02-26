package de.mephisto.vpin.server.highscores.parsing.vpreg;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
public class VPRegService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private FolderLookupService folderLookupService;

  public boolean isValid(Game game) {
    VPRegFile reg = folderLookupService.getVPRegFileForGame(game);
    return reg.isValid();
  }

  @Nullable
  public String getVPRegJson(@NonNull Game game) {
    VPRegFile reg = folderLookupService.getVPRegFileForGame(game);
    return reg.toJson();
  }

  @Nullable
  public VPRegFile getVPRegFile(@NonNull Game game) {
    return folderLookupService.getVPRegFileForGame(game);
  }

  public boolean resetHighscores(Game game, long score) {
    VPRegFile vpRegFile = folderLookupService.getVPRegFileForGame(game);
    return vpRegFile.resetHighscores(score);
  }

  public void restore(@NonNull Game game, @NonNull String json) {
    VPRegFile vpRegFile = folderLookupService.getVPRegFileForGame(game);
    vpRegFile.restore(json);
  }
}
