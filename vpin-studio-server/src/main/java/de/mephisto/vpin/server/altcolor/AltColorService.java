package de.mephisto.vpin.server.altcolor;

import de.mephisto.vpin.restclient.AltColor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@Service
public class AltColorService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorService.class);

  @Autowired
  private SystemService systemService;

  private final Map<String, AltColor> altColors = new ConcurrentHashMap<>();

  public boolean isAltColorAvailable(@NonNull Game game) {
    return getAltColor(game) != null;
  }

  public boolean delete(@NonNull Game game) {
    try {
      AltColor altColor = getAltColor(game);
      if (altColor != null) {
        File dir = new File(systemService.getAltColorFolder(), altColor.getName());
        if (dir.exists()) {
          FileUtils.deleteDirectory(dir);
          return true;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to delete altcolor directory for " + game + ": " + e.getMessage(), e);
    }
    return false;
  }

  @Nullable
  public AltColor getAltColor(@NonNull Game game) {
    String rom = game.getRom();
    String tableName = game.getTableName();
    if (!StringUtils.isEmpty(rom) && altColors.containsKey(rom)) {
      return altColors.get(rom);
    }

    if (!StringUtils.isEmpty(tableName) && altColors.containsKey(tableName)) {
      return altColors.get(tableName);
    }
    return null;
  }

  public boolean setAltColorEnabled(@NonNull Game game, boolean b) {
    String rom = game.getRom();
    if (!StringUtils.isEmpty(rom)) {
      if (b) {
//        systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, SOUND_MODE, 1);
      }
      else {
//        systemService.writeRegistry(SystemService.MAME_REG_KEY + rom, SOUND_MODE, 0);
      }
    }
    return b;
  }

  public boolean isAltColorEnabled(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom())) {
//      String sound_mode = systemService.getMameRegistryValue(game.getRom(), SOUND_MODE);
//      return String.valueOf(sound_mode).equals("0x1") || String.valueOf(sound_mode).equals("1");
    }
    return false;
  }

  public boolean clearCache() {
    this.altColors.clear();
    long start = System.currentTimeMillis();
    File altColorsFolder = systemService.getAltColorFolder();
    if (altColorsFolder.exists()) {
      File[] altColorFolders = altColorsFolder.listFiles((dir, name) -> new File(dir, name).isDirectory());
      if (altColorFolders != null) {
        for (File altColorFolder : altColorFolders) {
          File[] altColorFiles = altColorFolder.listFiles((dir, name) -> new File(dir, name).isFile());
          if (altColorFiles != null && altColorFiles.length > 0) {
            AltColor altColor = new AltColor();
            altColor.setName(altColorFolder.getName());
            altColor.setModificationDate(new Date(altColorFolder.lastModified()));
            this.altColors.put(altColorFolder.getName(), altColor);
          }
        }
      }
    }
    else {
      LOG.error("altcolor folder " + altColorsFolder.getAbsolutePath() + " does not exist.");
    }
    long end = System.currentTimeMillis();
    LOG.info("Finished altcolor scan, found " + altColors.size() + " colorizations (" + (end - start) + "ms)");
    return true;
  }

  public JobExecutionResult installAltColor(Game game, File out) {
    File folder = game.getAltColorFolder();
    if (folder != null) {
      LOG.info("Extracting archive to " + folder.getAbsolutePath());
      if (!folder.exists()) {
        if (!folder.mkdirs()) {
          return JobExecutionResultFactory.error("Failed to create ALT color directory " + folder.getAbsolutePath());
        }
      }

      AltColorUtil.unzip(out, folder);
      if (!out.delete()) {
        return JobExecutionResultFactory.error("Failed to delete temporary file.");
      }
      clearCache();
      setAltColorEnabled(game, true);
    }
    return JobExecutionResultFactory.empty();
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      clearCache();
    }).start();
  }
}
