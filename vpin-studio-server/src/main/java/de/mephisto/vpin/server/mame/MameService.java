package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.steam.SteamUtil;
import org.jspecify.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

@Service
public class MameService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public boolean play(@NonNull Game game) {
    try {
      File gameFile = game.getGameFile();
      GameEmulator emulator = game.getEmulator();
      File emuFolder = emulator.getInstallationFolder();

      List<String> params = new ArrayList<>();
      params.add("mame.exe");
      params.add(FilenameUtils.getBaseName(gameFile.getName()));

      SystemCommandExecutor executor = new SystemCommandExecutor(params, true);
      executor.setDir(emuFolder);
      executor.executeCommandAsync();

      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (standardErrorFromCommand != null && !StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("MAME command failed:\n{}", standardErrorFromCommand);
        return false;
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Error executing MAME command: {}", e.getMessage(), e);
    }
    return false;
  }

  public File installRom(UploadDescriptor uploadDescriptor, Game game, GameEmulator gameEmulator, File tempFile, UploaderAnalysis analysis) throws IOException {
    File emuFolder = gameEmulator.getInstallationFolder();
    File romsFolder = new File(emuFolder, "roms");
    if (romsFolder.exists()) {
      File out = new File(romsFolder, uploadDescriptor.getOriginalUploadFileName());

      if (out.exists() && !out.delete()) {
        throw new IOException("Failed to delete existing MAME rom " + out.getAbsolutePath());
      }
      org.apache.commons.io.FileUtils.copyFile(tempFile, out);
      LOG.info("Installed MAME asset {}", out.getAbsolutePath());
      return out;
    }
    throw new IOException("Failed to install MAME rom, roms folder not found.");
  }

  public String resolveMAMENameFor(String baseName) {
    File gameList = new File(RESOURCES, "mame-gamelist.txt");
    if (gameList.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(gameList))) {
        String line;
        while ((line = reader.readLine()) != null) {
          int sep = line.indexOf(' ');
          if (sep > 0 && line.substring(0, sep).equalsIgnoreCase(baseName)) {
            String quoted = line.substring(sep).trim();
            int start = quoted.indexOf('"');
            int end = quoted.lastIndexOf('"');
            if (start >= 0 && end > start) {
              return quoted.substring(start + 1, end);
            }
          }
        }
      }
      catch (Exception e) {
        LOG.error("Failed to resolve MAME name for \"{}\": {}", baseName, e.getMessage());
      }
    }
    return baseName;
  }
}
