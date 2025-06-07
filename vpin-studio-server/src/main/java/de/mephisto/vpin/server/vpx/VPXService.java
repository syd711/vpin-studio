package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.commons.utils.VPXKeyManager;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.vpx.TableInfo;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.INIConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
public class VPXService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPXService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private VPXCommandLineService vpxCommandLineService;

  /**
   * The cached information in vPïnballX.ini file
   */
  private INIConfiguration iniConfiguration;

  private VPXKeyManager keyManager;


  public File getVPXFile() {
    String userhome = System.getProperty("user.home");
    return new File(userhome, "AppData/Roaming/VPinballX/VPinballX.ini");
  }

  private void loadIni() {
    File vpxInFile = getVPXFile();
    if (vpxInFile.exists()) {
      try (FileReader fileReader = new FileReader(vpxInFile)) {
        this.iniConfiguration = new INIConfiguration();
        iniConfiguration.setCommentLeadingCharsUsedInInput(";");
        iniConfiguration.setSeparatorUsedInOutput("=");
        iniConfiguration.setSeparatorUsedInInput("=");
        iniConfiguration.read(fileReader);
        LOG.info("loaded VPX ini file {}", vpxInFile.getAbsolutePath());

        this.keyManager = new VPXKeyManager(getPlayerConfiguration(false));
      }
      catch (Exception e) {
        LOG.error("Failed to read VPX ini file: " + e.getMessage(), e);
      }
    }
  }

  public @Nullable Configuration getPlayerConfiguration(boolean forceReload) {
    if (forceReload) {
      loadIni();
    }
    return iniConfiguration != null ? iniConfiguration.getSection("Player") : null;
  }

  public VPXKeyManager getKeyManager() {
    return keyManager;
  }

  //---------------------------------------------------- POV Management ---

  public POV getPOV(Game game) {
    try {
      if (game != null) {
        File povFile = game.getPOVFile();
        if (povFile.exists()) {
          return POVParser.parse(povFile, game.getId());
        }
      }
      return null;
    }
    catch (VPinStudioException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public boolean savePOVPreference(Game game, Map<String, Object> values) {
    POV pov = getPOV(game);
    if (pov != null) {
      BeanWrapper bean = new BeanWrapperImpl(pov);
      String property = (String) values.get("property");
      Object value = values.get("value");
      bean.setPropertyValue(property, value);
      save(game, pov);
      return true;
    }
    return false;
  }

  @Nullable
  public POV create(Game game) {
    if (game != null) {
      if (game.getPOVFile().exists()) {
        if (!game.getPOVFile().delete()) {
          throw new UnsupportedOperationException("Failed to delete " + game.getPOVFile().getAbsolutePath());
        }
      }

      if (!game.getPOVFile().exists()) {
        createPOV(game);
        return getPOV(game);
      }
    }
    return null;
  }

  public POV save(Game game, POV pov) {
    try {
      if (game != null) {
        if (!game.getPOVFile().exists()) {
          createPOV(game);
          return getPOV(game);
        }

        POVSerializer.serialize(pov, game);
      }
      return pov;
    }
    catch (VPinStudioException e) {
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public boolean delete(Game game) {
    if (game != null) {
      File povFile = game.getPOVFile();
      if (povFile.exists()) {
        LOG.info("Deleting " + povFile.getAbsolutePath());
        return povFile.delete();
      }
      else {
        LOG.info("POV file " + povFile.getAbsolutePath() + " does not exist for deletion");
      }
    }
    else {
      LOG.error("No game found for pov deletion");
    }
    return false;
  }

  public POV createPOV(Game game) {
    if (game != null) {
      try {
        File target = vpxCommandLineService.export(game, "-Pov", "pov");
        if (target.exists()) {
          return getPOV(game);
        }
      }
      catch (Exception e) {
        LOG.error("Error executing shutdown: " + e.getMessage(), e);
      }
    }
    else {
      LOG.error("No game found for pov creation");
    }
    return null;
  }

  //---------------------------------------------------- GAME Management ---

  public String getScript(Game game) {
    if (game != null) {
      File target = vpxCommandLineService.export(game, "-ExtractVBS", "vbs");
      if (target.exists()) {
        try {
          LOG.info("Reading vbs file " + target.getAbsolutePath() + " (" + FileUtils.readableFileSize(target.length()) + ")");
          Path filePath = Path.of(target.toURI());
          return Files.readString(filePath);
        }
        catch (IOException e) {
          LOG.error("Failed to read " + target.getAbsolutePath() + ": " + e.getMessage(), e);
        }
        finally {
          if (!target.delete()) {
            LOG.error("Failed to clean up vbs file " + target.getAbsolutePath());
          }
        }
      }
    }
    else {
      LOG.error("No game found for script extraction");
    }
    return null;
  }

  @Nullable
  public TableInfo getTableInfo(Game game) {
    if (game != null) {
      File gameFile = game.getGameFile();
      if (gameFile.exists()) {
        try {
          Map<String, Object> values = VPXUtil.readTableInfo(gameFile);
          if (values != null) {
            return new TableInfo(values);
          }
        }
        catch (Exception e) {
          LOG.error("Failed to read table info: " + e.getMessage());
        }
      }
    }
    return null;
  }

  public String getSources(Game game) {
    if (game != null) {
      File gameFile = game.getGameFile();
      if (gameFile.exists()) {
        String sources = VPXUtil.readScript(gameFile);
        return Base64.getEncoder().encodeToString(sources.getBytes());
      }
    }
    else {
      LOG.error("No game found for table sources extraction");
    }
    return null;
  }

  public boolean importVBS(Game game, String vbs, boolean useTempFile) {
    if (game != null) {
      File gameFile = game.getGameFile();
      if (gameFile.exists()) {
        try {
          VPXUtil.importVBS(gameFile, vbs, useTempFile);
          LOG.info("Written table sources " + gameFile.getAbsolutePath());
          return true;
        }
        catch (IOException e) {
          //already logged
        }
      }
    }
    return false;
  }

  public boolean play(@Nullable Game game, @Nullable String altExe, @Nullable String option) {
    if (game != null) {
      if ("cameraMode".equals(option)) {
        return vpxCommandLineService.execute(game, altExe, "-Minimized", "-PovEdit");
      }
      else if ("primary".equals(option)) {
        return vpxCommandLineService.execute(game, altExe, "-Minimized", "-Primary", "-Play");
      }
      else {
        return vpxCommandLineService.execute(game, altExe, "-Minimized", "-Play");
      }

    }
    return false;
  }

  public boolean waitForPlayer() {
    return systemService.waitForWindow("Visual Pinball Player", 60, 2000);
  }

  public String getChecksum(Game game) {
    if (game != null) {
      File gameFile = game.getGameFile();
      if (gameFile.exists()) {
        return VPXUtil.getChecksum(gameFile);
      }
      else {
        LOG.info("Game file " + gameFile.getAbsolutePath() + " does not exist for reading the checksum.");
      }
    }
    else {
      LOG.error("No game found reading checksum");
    }
    return null;
  }

  //------------------------------------------

  @Override
  public void afterPropertiesSet() throws Exception {
    loadIni();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
