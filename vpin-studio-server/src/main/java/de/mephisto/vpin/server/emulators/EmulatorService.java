package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.restclient.emulators.EmulatorValidation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.alx.AlxService;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.frontend.FrontendConnector;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.popper.pupgames.PUPGameImporter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameEmulatorValidationService;
import de.mephisto.vpin.server.games.GameMediaService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmulatorService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private GameEmulatorValidationService gameEmulatorValidationService;

  @Autowired
  private VPinMameService vPinMameService;

  @Autowired
  private EmulatorFactory emulatorFactory;

  @Lazy
  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private DOFLinxService dofLinxService;

  @Autowired
  private PreferencesService preferencesService;

  @Lazy
  @Autowired
  private AlxService alxService;

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
    gameEmulators.sort((o1, o2) -> o2.getName().compareTo(o1.getName()));
    return gameEmulators;
  }

  public List<GameEmulator> getValidGameEmulators() {
    List<GameEmulator> gameEmulators = getGameEmulators().stream().filter(GameEmulator::isValid).collect(Collectors.toList());
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
    return getGameEmulators().stream().filter(e -> (e.isVpxEmulator() || e.isZenEmulator()) && e.isValid()).collect(Collectors.toList());
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

  public void reloadEmulators() {
    FrontendConnector frontendConnector = frontendService.getFrontendConnector();
    frontendConnector.reloadCache();
    List<GameEmulator> ems = frontendConnector.getEmulators();
    this.emulators.clear();
    for (GameEmulator emulator : ems) {
      long start = System.currentTimeMillis();
      loadEmulator(emulator);
      LOG.info("Loading of emulator {} took {}ms.", emulator.getName(), (System.currentTimeMillis() - start));
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
        // mind that popper may set a specific romDirectory
        if (StringUtils.isEmpty(emulator.getRomDirectory())) {
          File romFolder = vPinMameService.getRomsFolder();
          if (romFolder != null && romFolder.exists()) {
            emulator.setRomDirectory(romFolder.getAbsolutePath());
          }
          else {
            emulator.setRomDirectory(new File(mameFolder, "roms").getAbsolutePath());
          }
        }
      }

      if (emulator.isZenEmulator() && dofLinxService.isValid() && dofLinxService.getBackglassesFolder(emulator) != null) {
        File backglassesFolder = dofLinxService.getBackglassesFolder(emulator);
        if (backglassesFolder != null) {
          emulator.setBackglassDirectory(backglassesFolder.getAbsolutePath());
        }
      }

      emulators.put(emulator.getId(), emulator);

      LOG.info("Loaded Emulator: {}", emulator);
    }
    catch (Exception e) {
      LOG.error("Emulator initialization failed: {}", e.getMessage(), e);
    }
  }

  public EmulatorValidation validate(EmulatorType emulatorType) {
    return emulatorFactory.create(emulatorType);
  }


  @EventListener(ApplicationReadyEvent.class)
  public void synchronizeEmulators() {
    for (GameEmulator emulator : this.emulators.values()) {
      synchronizeEmulator(emulator);
    }
  }

  /**
   * Used to synchronize emulators with .pupgames files to the latest lists.
   */
  private void synchronizeEmulator(GameEmulator emulator) {
    if (!emulator.isEnabled()) {
      return;
    }

    int count = 0;
    if (emulator.isPupGameImportSupported()) {
      List<Game> gamesByEmulator = frontendService.getGamesByEmulator(emulator.getId());
      List<TableDetails> fullGameList = PUPGameImporter.read(emulator.getType(), emulator.getId());
      if (gamesByEmulator.isEmpty()) {
        for (TableDetails tableDetails : fullGameList) {
          int gameId = frontendService.importGame(tableDetails);
          if (gameId > 0) {
            gameMediaService.autoMatch(gameId, false);
            count++;
          }
        }
        LOG.info("\"{}\" emulator synchronization finished, added {} games.", emulator.getName(), count);
      }
      else if (gamesByEmulator.size() != fullGameList.size()) {
        for (TableDetails tableDetails : fullGameList) {
          if (!containsGame(gamesByEmulator, tableDetails)) {
            LOG.info("Found missing game for {}: {}", emulator.getName(), tableDetails.getGameDisplayName());
//            LOG.info("Importing missing game for {}: {}", emulator.getName(), tableDetails.getGameDisplayName());
//            int gameId = frontendService.importGame(tableDetails);
//            if (gameId > 0) {
//              gameMediaService.autoMatch(gameId, false);
//              count++;
//            }
          }
        }
      }

      cleanUpNonVPXDuplicates(emulator, gamesByEmulator);
    }
  }

  private void cleanUpNonVPXDuplicates(GameEmulator emulator, List<Game> gamesByEmulator) {
    Map<String, List<Game>> byBaseName = gamesByEmulator.stream()
        .collect(Collectors.groupingBy(g -> FilenameUtils.getBaseName(g.getGameFileName()).toLowerCase()));

    for (Map.Entry<String, List<Game>> entry : byBaseName.entrySet()) {
      List<Game> duplicates = entry.getValue();
      if (duplicates.size() > 1) {
        LOG.warn("Found {} duplicate entries for base name \"{}\" in emulator {}", duplicates.size(), entry.getKey(), emulator.getName());
        // Keep the game with the most playtime; fall back to first entry if all are zero
        Game keepGame = duplicates.stream()
            .max(Comparator.comparingInt(g -> alxService.getAlxSummary(g.getId()).getEntries().stream()
                .mapToInt(TableAlxEntry::getTimePlayedSecs).sum()))
            .orElse(duplicates.get(0));
        for (Game duplicate : duplicates) {
          if (duplicate != keepGame) {
            LOG.info("Removing duplicate with less playtime: [{}] {}", duplicate.getId(), duplicate.getGameFileName());
            gamesByEmulator.remove(duplicate);
            frontendService.deleteGame(duplicate.getId());
            LOG.info("Removed non-VPX from frontend database: [{}] {}", duplicate.getId(), duplicate.getGameFileName());
          }
        }
      }
    }
  }

  private boolean containsGame(List<Game> gamesByEmulator, TableDetails tableDetails) {
    for (Game game : gamesByEmulator) {
      if (game.getGameFileName().equals(tableDetails.getGameFileName())) {
        return true;
      }
      if (game.getGameName().equals(tableDetails.getGameName())) {
        return true;
      }
      if (game.getGameDisplayName().equals(tableDetails.getGameDisplayName())) {
        return true;
      }
    }
    return false;
  }

  public boolean clearCache() {
    reloadEmulators();
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

  public void removeEmulatorChangeListener(EmulatorChangeListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.DOFLINX_SETTINGS.equals(propertyName)) {
      reloadEmulators();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
