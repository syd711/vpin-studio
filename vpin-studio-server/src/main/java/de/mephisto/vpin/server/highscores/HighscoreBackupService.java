package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class HighscoreBackupService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreBackupService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private SystemService systemService;

  public boolean delete(@NonNull String rom, String filename) {
    File folder = new File(systemService.getBackupFolder(), rom);
    File archive = new File(folder, filename);
    if (archive.exists() && !archive.delete()) {
      throw new UnsupportedOperationException("Failed to delete " + archive.getAbsolutePath());
    }
    LOG.info("Deleted " + archive.getAbsolutePath());
    return true;
  }

  public List<HighscoreBackup> getBackups(@NonNull String rom) {
    File folder = new File(systemService.getBackupFolder(), rom);
    List<HighscoreBackup> result = new ArrayList<>();
    if (folder.exists()) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith("." + HighscoreBackupUtil.FILE_SUFFIX));
      if (files != null) {
        for (File file : files) {
          HighscoreBackup highscoreBackup = HighscoreBackupUtil.readBackupFile(file);
          if (highscoreBackup != null) {
            result.add(highscoreBackup);
          }
        }
      }
    }

    result.sort(Comparator.comparing(HighscoreBackup::getCreationDate));
    Collections.reverse(result);
    return result;
  }

  public boolean backup(Game game) {
    String rom = game.getRom();
    if (StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }
    File folder = new File(systemService.getBackupFolder(), rom);
    return HighscoreBackupUtil.writeBackupFile(highscoreService, systemService, game, folder);
  }

  public boolean restore(@NonNull Game game, @NonNull List<Game> games, @NonNull String filename) {
    String rom = game.getRom();
    if(StringUtils.isEmpty(rom)) {
      rom = game.getTableName();
    }

    File backupRomFolder = new File(systemService.getBackupFolder(), rom);
    boolean result = HighscoreBackupUtil.restoreBackupFile(game.getEmulator(), backupRomFolder, filename);
    if (result) {
      highscoreService.setPauseHighscoreEvents(true);
      for (Game allRomGames : games) {
        highscoreService.scanScore(allRomGames);
      }
      highscoreService.setPauseHighscoreEvents(false);
    }
    return result;
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
