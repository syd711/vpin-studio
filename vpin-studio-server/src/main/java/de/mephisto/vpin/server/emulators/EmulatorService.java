package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.emulators.EmulatorValidation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.popper.pupgames.PUPGameImporter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameEmulatorValidationService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.mame.MameService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmulatorService {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorService.class);

  @Autowired
  private GameEmulatorValidationService gameEmulatorValidationService;

  @Autowired
  private MameService mameService;

  @Autowired
  private EmulatorFactory emulatorFactory;

  @Lazy
  @Autowired
  private GameMediaService gameMediaService;

  private final List<EmulatorChangeListener> listeners = new ArrayList<>();

  private final Map<Integer, GameEmulator> emulators = new LinkedHashMap<>();

  private FrontendService frontendService;

  public GameEmulator getGameEmulator(int emulatorId) {
    return this.emulators.get(emulatorId);
  }

  public Collection<GameEmulator> getGameEmulators() {
    return this.emulators.values();
  }

  public List<String> getAltExeNames(int emulatorId) {
    GameEmulator gameEmulator = getGameEmulator(emulatorId);
    return getAltExeNames(gameEmulator);
  }

  public List<String> getAltExeNames(GameEmulator emulator) {
    if (emulator.isVpxEmulator() && emulator.isValid() && emulator.getInstallationFolder().exists()) {
      File installationFolder = emulator.getInstallationFolder();
      String[] exeFiles = installationFolder.list((dir, name) -> name.endsWith(".exe") && !name.toLowerCase().contains("install"));
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
    List<GameEmulator> gameEmulators = new ArrayList<>(getGameEmulators());
    for (GameEmulator gameEmulator : gameEmulators) {
      List<ValidationState> validate = gameEmulatorValidationService.validate(frontendService.getFrontendType(), gameEmulator, true);
      gameEmulator.setValidationStates(validate);
    }
    Collections.sort(gameEmulators, (o1, o2) -> o2.getName().compareTo(o1.getName()));
    return gameEmulators;
  }

  public List<GameEmulator> getValidGameEmulators() {
    List<GameEmulator> gameEmulators = getGameEmulators().stream().filter(e -> e.isValid()).collect(Collectors.toList());
    Collections.sort(gameEmulators, (o1, o2) -> o2.getName().compareTo(o1.getName()));
    return gameEmulators;
  }

  public List<GameEmulator> getVpxGameEmulators() {
    return getGameEmulators().stream().filter(e -> e.isVpxEmulator() && e.isValid()).collect(Collectors.toList());
  }

  public List<GameEmulator> getZenGameEmulators() {
    return getGameEmulators().stream().filter(e -> e.isZenEmulator() && e.isValid()).collect(Collectors.toList());
  }

  public List<GameEmulator> getBackglassGameEmulators() {
    return getGameEmulators().stream().filter(GameEmulator::isValid).filter(e -> e.isVpxEmulator()).collect(Collectors.toList());
  }

  public void setFrontendService(FrontendService frontendService) {
    this.frontendService = frontendService;
  }

  public GameEmulator save(GameEmulator emulator) {
    GameEmulator saved = frontendService.saveEmulator(emulator);
    this.emulators.remove(saved.getId());
    loadEmulator(saved);
    synchronizeEmulator(saved);
    notifyEmulatorChange(saved.getId());
    return saved;
  }

  public boolean delete(int emulatorId) {
    frontendService.deleteEmulator(emulatorId);
    this.emulators.remove(emulatorId);
    notifyEmulatorChange(emulatorId);
    return true;
  }

  public void loadEmulators() {
    FrontendConnector frontendConnector = frontendService.getFrontendConnector();
    frontendConnector.reloadCache();
    List<GameEmulator> ems = frontendConnector.getEmulators();
    this.emulators.clear();
    for (GameEmulator emulator : ems) {
      loadEmulator(emulator);
    }

    for (GameEmulator emulator : ems) {
      synchronizeEmulator(emulator);
    }

    if (this.emulators.isEmpty()) {
      LOG.error("*****************************************************************************************");
      LOG.error("No valid game emulators folder, fill all(!) emulator directory settings in your frontend.");
      LOG.error("*****************************************************************************************");
    }
  }

  private void loadEmulator(GameEmulator emulator) {
    try {
      File mameFolder = new File(emulator.getInstallationDirectory(), "VPinMAME");
      if (mameFolder.exists()) {
        emulator.setMameDirectory(mameFolder.getAbsolutePath());
      }

      if (emulator.isVpxEmulator()) {
        File registryFolder = mameService.getNvRamFolder();
        if (registryFolder != null && registryFolder.exists()) {
          emulator.setNvramDirectory(registryFolder.getAbsolutePath());
        }
        else {
          emulator.setNvramDirectory(new File(mameFolder, "nvram").getAbsolutePath());
        }

        File cfgFolder = mameService.getCfgFolder();
        if (cfgFolder != null && cfgFolder.exists()) {
          emulator.setCfgDirectory(cfgFolder.getAbsolutePath());
        }
        else {
          emulator.setCfgDirectory(new File(mameFolder, "nvram").getAbsolutePath());
        }

        // mind that popper may set a specific romDirectory
        if (StringUtils.isEmpty(emulator.getRomDirectory())) {
          File romFolder = mameService.getRomsFolder();
          if (romFolder != null && romFolder.exists()) {
            emulator.setRomDirectory(romFolder.getAbsolutePath());
          }
          else {
            emulator.setRomDirectory(new File(mameFolder, "roms").getAbsolutePath());
          }
        }
      }
      emulators.put(emulator.getId(), emulator);

      LOG.info("Loaded Emulator: " + emulator);
    }
    catch (Exception e) {
      LOG.error("Emulator initialization failed: " + e.getMessage(), e);
    }
  }

  public EmulatorValidation validate(EmulatorType emulatorType) {
    return emulatorFactory.create(emulatorType);
  }

  private void synchronizeEmulator(GameEmulator emulator) {
    if (!emulator.isEnabled()) {
      return;
    }

    int count = 0;
    if (emulator.isPupGameImportSupported()) {
      List<Game> gamesByEmulator = frontendService.getGamesByEmulator(emulator.getId());
      if (gamesByEmulator.isEmpty()) {
        List<TableDetails> tableDetailList = PUPGameImporter.read(emulator.getType(), emulator.getId());
        for (TableDetails tableDetails : tableDetailList) {
          int gameId = frontendService.importGame(tableDetails);
          if (gameId > 0) {
            gameMediaService.autoMatch(gameId, false);
            count++;
          }
        }
        LOG.info("\"{}\" emulator synchronization finished, added {} games.", emulator.getName(), count);
      }
    }
  }

  public boolean clearCache() {
    loadEmulators();
    return true;
  }

  private void notifyEmulatorChange(int emulatorId) {
    for (EmulatorChangeListener listener : listeners) {
      listener.emulatorChanged(emulatorId);
    }
  }

  public void addEmulatorChangeListener(EmulatorChangeListener listener) {
    this.listeners.add(listener);
  }
}
