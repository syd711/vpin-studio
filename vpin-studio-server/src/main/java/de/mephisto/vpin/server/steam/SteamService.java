package de.mephisto.vpin.server.steam;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

@Service
public class SteamService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(SteamService.class);

  @Nullable
  public File getGameFolder(@NonNull EmulatorType emulatorType) {
    Map<String, File> gameFolders = SteamUtil.getGameFolders();
    if (!StringUtils.isEmpty(emulatorType.folderName()) && gameFolders.containsKey(emulatorType.folderName())) {
      File file = gameFolders.get(emulatorType.folderName());
      LOG.info("SteamService resolved game folder {}", file.getAbsolutePath());
      return file;
    }
    return null;
  }

  public File getSteamFolder() {
    return SteamUtil.getSteamFolder();
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
