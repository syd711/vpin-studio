package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @see de.mephisto.vpin.server.ServerUpdatePreProcessing
 */
@Service
public class NVRamService {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamService.class);

  @Autowired
  private SystemService systemService;

  public NVRamList getResettedNVRams() {
    NVRamList result = new NVRamList();
    File nvRamsFolder = systemService.getResettedNVRamsFolder();
    if (nvRamsFolder.exists()) {
      File[] nvramFiles = nvRamsFolder.listFiles((dir, name) -> name.endsWith(".nv"));
      if (nvramFiles != null) {
        Arrays.stream(nvramFiles).forEach(file -> result.getEntries().add(FilenameUtils.getBaseName(file.getName())));
      }
    }
    return result;
  }

  public boolean copyResettedNvRam(File nvRamFile) {
    try {
      File resettedNvRam = new File(systemService.getResettedNVRamsFolder(), nvRamFile.getName());
      if (resettedNvRam.exists()) {
        FileUtils.copyFile(resettedNvRam, nvRamFile);
        LOG.info("Copied resetted nvram file " + resettedNvRam.getAbsolutePath());
        return true;
      }
    } catch (IOException e) {
      LOG.error("Failed to copy resetted nvram file " + nvRamFile.getAbsolutePath() + ": " + e.getMessage(), e);
      return false;
    }
    return true;
  }
}
