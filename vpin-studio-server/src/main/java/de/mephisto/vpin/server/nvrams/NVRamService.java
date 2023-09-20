package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.restclient.NVRamList;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

/**
 * @see de.mephisto.vpin.server.ServerUpdatePreProcessing
 */
@Service
public class NVRamService {

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
}
