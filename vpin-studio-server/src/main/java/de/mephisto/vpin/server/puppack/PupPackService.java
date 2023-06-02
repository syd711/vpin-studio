package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.puppack.PupPack;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class PupPackService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackService.class);

  @Autowired
  private SystemService systemService;

  private Map<String, PupPack> pupPackFolders = new HashMap<>();

  @Nullable
  public PupPack getPupPack(@NonNull Game game) {
    if (!StringUtils.isEmpty(game.getRom()) && pupPackFolders.containsKey(game.getRom())) {
      return pupPackFolders.get(game.getRom());
    }
    if (!StringUtils.isEmpty(game.getTableName()) && pupPackFolders.containsKey(game.getTableName())) {
      return pupPackFolders.get(game.getTableName());
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      Thread.currentThread().setName("PUP Pack Scanner");
      long start = System.currentTimeMillis();
      File pupPackFolder = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
      if (pupPackFolder.exists()) {
        File[] pupPacks = pupPackFolder.listFiles((dir, name) -> new File(dir, name).isDirectory());
        if (pupPacks != null) {
          for (File packFolder : pupPacks) {
            Collection<File> files = FileUtils.listFiles(packFolder, new String[]{"mp4"}, true);
            if (!files.isEmpty()) {
              pupPackFolders.put(packFolder.getName(), new PupPack(packFolder));
            }
          }
        }
      }
      else {
        LOG.error("PUP pack folder " + pupPackFolder.getAbsolutePath() + " does not exist.");
      }
      long end = System.currentTimeMillis();
      LOG.info("Finished PUP pack scan, found " + pupPackFolders.size() + " packs (" + (end - start) + "ms)");
    }).start();
  }
}
