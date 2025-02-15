package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameEmulatorValidationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmulatorService {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorService.class);

  @Autowired
  private GameEmulatorValidationService gameEmulatorValidationService;

  private final Map<Integer, GameEmulator> emulators = new LinkedHashMap<>();

  private FrontendService frontendService;

  public GameEmulator getGameEmulator(int emulatorId) {
    return this.emulators.get(emulatorId);
  }

  public List<String> getAltExeNames(int emulatorId) {
    GameEmulator gameEmulator = getGameEmulator(emulatorId);
    return getAltExeNames(gameEmulator);
  }

  public List<String> getAltExeNames(GameEmulator emulator) {
    if (emulator.isVpxEmulator() && emulator.isValid() && emulator.getInstallationFolder().exists()) {
      File installationFolder = emulator.getInstallationFolder();
      String[] exeFiles = installationFolder.list((dir, name) -> name.endsWith(".exe") && name.toLowerCase().contains("vpin"));
      if (exeFiles == null) {
        exeFiles = new String[]{};
      }
      return Arrays.asList(exeFiles);
    }

    if (emulator.getExe() != null) {
      return List.of(emulator.getExe().getName());
    }
    return Collections.emptyList();
  }

  public List<GameEmulator> getValidatedGameEmulators() {
    List<GameEmulator> gameEmulators = new ArrayList<>(this.emulators.values());
    for (GameEmulator gameEmulator : gameEmulators) {
      List<ValidationState> validate = gameEmulatorValidationService.validate(gameEmulator, true);
      gameEmulator.setValidationStates(validate);
    }
    Collections.sort(gameEmulators, (o1, o2) -> o2.getName().compareTo(o1.getName()));
    return gameEmulators;
  }

  public List<GameEmulator> getValidGameEmulators() {
    List<GameEmulator> gameEmulators = new ArrayList<>(this.emulators.values()).stream().filter(e -> e.isValid()).collect(Collectors.toList());
    Collections.sort(gameEmulators, (o1, o2) -> o2.getName().compareTo(o1.getName()));
    return gameEmulators;
  }

  public List<GameEmulator> getVpxGameEmulators() {
    return this.emulators.values().stream().filter(e -> e.isVpxEmulator() && e.isValid()).collect(Collectors.toList());
  }

  public List<GameEmulator> getBackglassGameEmulators() {
    List<GameEmulator> gameEmulators = new ArrayList<>(this.emulators.values());
    return gameEmulators.stream().filter(GameEmulator::isValid).filter(e -> {
      return e.isVpxEmulator() || e.isFpEmulator();
    }).collect(Collectors.toList());
  }

  public GameEmulator getDefaultGameEmulator() {
    Collection<GameEmulator> emulators = this.emulators.values();

    // when there is only one VPX emulator, it is forcibly the default one
    if (emulators.size() == 1) {
      GameEmulator value = emulators.iterator().next();
      return value.isVpxEmulator() ? value : null;
    }

    for (GameEmulator emulator : emulators) {
      if (emulator.isValid() && emulator.getDescription() != null && emulator.isVpxEmulator() && emulator.getDescription().contains("default")) {
        return emulator;
      }
    }

    for (GameEmulator value : emulators) {
      if (value.isValid() && value.isVpxEmulator() && value.getNvramFolder().exists()) {
        return value;
      }
      else {
        LOG.error(value + " has no nvram folder \"" + value.getNvramFolder().getAbsolutePath() + "\"");
      }
    }
    LOG.error("Failed to determine emulator for highscores, no VPinMAME/nvram folder could be resolved (" + this.emulators.size() + " VPX emulators found).");
    return null;
  }

  public boolean isValidVPXEmulator(GameEmulator emulator) {
    if (!emulator.getType().isVpxEmulator()) {
      return false;
    }

    if (StringUtils.isEmpty(emulator.getGamesDirectory())) {
      LOG.warn("Ignoring " + emulator + ", because \"Games Folder\" is not set.");
      return false;
    }

    if (frontendService.getFrontendConnector().getMediaAccessStrategy() != null && StringUtils.isEmpty(emulator.getMediaDirectory())) {
      LOG.warn("Ignoring " + emulator + ", because \"Media Dir\" is not set.");
      return false;
    }

    return true;
  }

  public void setFrontendService(FrontendService frontendService) {
    this.frontendService = frontendService;
  }

  public GameEmulator save(GameEmulator emulator) {
    return frontendService.saveEmulator(emulator);
  }

  public boolean delete(int emulatorId) {
    return frontendService.deleteEmulator(emulatorId);
  }

  public void loadEmulators() {
    FrontendConnector frontendConnector = frontendService.getFrontendConnector();
    frontendConnector.reloadCache();
    List<GameEmulator> ems = frontendConnector.getEmulators();
    this.emulators.clear();
    for (GameEmulator emulator : ems) {
      try {
        if (emulator.getType().isVpxEmulator() && !isValidVPXEmulator(emulator)) {
          continue;
        }

        emulators.put(emulator.getId(), emulator);

        LOG.info("Loaded Emulator: " + emulator);
      }
      catch (Exception e) {
        LOG.error("Emulator initialization failed: " + e.getMessage(), e);
      }
    }

    if (this.emulators.isEmpty()) {
      LOG.error("*****************************************************************************************");
      LOG.error("No valid game emulators folder, fill all(!) emulator directory settings in your frontend.");
      LOG.error("*****************************************************************************************");
    }
  }
}
