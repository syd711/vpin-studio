package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.backups.BackupSourceRepresentation;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.BackupExportDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.backups.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.backups.adapters.vpa.BackupSourceAdapterFolder;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaBackupSource;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpauthenticators.VPAuthenticationService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackupService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(BackupService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private BackupSourceRepository backupSourceRepository;

  @Autowired
  private JobService jobService;

  @Autowired
  private TableBackupAdapterFactory tableBackupAdapterFactory;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private CardService cardService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private VpaService vpaService;

  @Autowired
  private VPAuthenticationService vpAuthenticationService;

  @Autowired
  private PreferencesService preferencesService;

  private BackupSourceAdapter defaultBackupSourceAdapter;

  private final Map<Long, BackupSourceAdapter> backupSourcesCache = new LinkedHashMap<>();

  public List<BackupDescriptor> getBackupDescriptorForGame(int gameId) {
    Game game = gameService.getGame(gameId);

    return getBackupSourceDescriptors().stream().filter(backupDescriptor -> {
      TableDetails manifest = backupDescriptor.getTableDetails();
      if (manifest == null) {
        return false;
      }
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  @NonNull
  public List<BackupDescriptor> getBackupSourceDescriptors() {
    List<BackupDescriptor> result = new ArrayList<>();
    for (BackupSourceAdapter adapter : backupSourcesCache.values()) {
      if (adapter.getBackupSource().isEnabled()) {
        List<BackupDescriptor> descriptors = adapter.getBackupDescriptors();
        result.addAll(descriptors);
      }
    }
    result.sort((o1, o2) -> {
      if (o1.getFilename() != null && o2.getFilename() != null) {
        return o1.getFilename().compareTo(o2.getFilename());
      }
      return o1.getTableDetails().getGameDisplayName().compareTo(o2.getTableDetails().getGameDisplayName());
    });
    return result;
  }

  @NonNull
  public List<BackupDescriptor> getBackupSourceDescriptors(long sourceId) {
    if (!vpAuthenticationService.isAuthenticated()) {
      return Collections.emptyList();
    }

    BackupSourceAdapter adapter = getBackupSourceAdapter(sourceId);
    List<BackupDescriptor> backupDescriptors = adapter.getBackupDescriptors();
    backupDescriptors.sort((o1, o2) -> {
      if (o1.getFilename() != null && o2.getFilename() != null) {
        return o1.getFilename().compareTo(o2.getFilename());
      }
      return o1.getTableDetails().getGameDisplayName().compareTo(o2.getTableDetails().getGameDisplayName());
    });

    return backupDescriptors;
  }

  @Nullable
  public BackupDescriptor getBackupDescriptors(long sourceId, @NonNull String filename) {
    BackupSourceAdapter sourceAdapter = backupSourcesCache.get(sourceId);
    List<BackupDescriptor> descriptors = sourceAdapter.getBackupDescriptors();
    for (BackupDescriptor descriptor : descriptors) {
      String descriptorFilename = descriptor.getFilename();
      if (filename.equals(descriptorFilename)) {
        return descriptor;
      }
    }
    return null;
  }

  public boolean deleteBackup(long sourceId, @NonNull String filename) {
    List<BackupDescriptor> descriptors = backupSourcesCache.get(sourceId).getBackupDescriptors();
    Optional<BackupDescriptor> first = descriptors.stream().filter(ArchiveDescriptor -> ArchiveDescriptor.getFilename().equals(filename)).findFirst();
    if (first.isPresent()) {
      BackupDescriptor descriptor = first.get();
      return getBackupSourceAdapter(sourceId).delete(descriptor);
    }
    return false;
  }

  public boolean deleteBackupSource(long id) {
    if (backupSourcesCache.containsKey(id)) {
      this.backupSourcesCache.remove(id);
      this.backupSourceRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public List<BackupSource> getBackupSources() {
    return backupSourcesCache.values().stream().map(BackupSourceAdapter::getBackupSource).collect(Collectors.toList());
  }

  public BackupSourceAdapter getDefaultBackupSource() {
    return this.defaultBackupSourceAdapter;
  }

  public BackupSourceAdapter getBackupSourceAdapter(long sourceId) {
    return backupSourcesCache.get(sourceId);
  }

  public File export(BackupDescriptor backupDescriptor) {
    return getDefaultBackupSource().export(backupDescriptor);
  }

  public void invalidateCache() {
    Set<Map.Entry<Long, BackupSourceAdapter>> entries = backupSourcesCache.entrySet();
    for (Map.Entry<Long, BackupSourceAdapter> entry : entries) {
      entry.getValue().invalidate();
    }
  }

  public BackupSource save(BackupSourceRepresentation representation) {
    Optional<BackupSource> byId = backupSourceRepository.findById(representation.getId());
    BackupSource backupSource = null;
    if (byId.isPresent()) {
      backupSource = byId.get();
    }
    else {
      backupSource = new BackupSource();
      backupSource.setCreatedAt(new Date());
      backupSource.setType(representation.getType());
    }

    backupSource.setLocation(representation.getLocation());
    backupSource.setName(representation.getName());
    backupSource.setAuthenticationType(representation.getAuthenticationType());
    backupSource.setLogin(representation.getLogin());
    backupSource.setPassword(representation.getPassword());
    backupSource.setEnabled(representation.isEnabled());
    backupSource.setSettings(representation.getSettings());

    BackupSource updatedSource = backupSourceRepository.saveAndFlush(backupSource);
    backupSourcesCache.remove(updatedSource.getId());

    backupSourcesCache.put(updatedSource.getId(), BackupSourceAdapterFactory.create(this, updatedSource, vpaService));
    LOG.info("(Re)created archive source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  public File getTargetFile(BackupDescriptor backupDescriptor) {
    BackupType backupType = BackupType.VPA;

    switch (backupType) {
      case VPA: {
        return new File(VpaBackupSource.FOLDER, backupDescriptor.getFilename());
      }
    }
    return null;
  }


  public boolean restoreBackup(@NonNull ArchiveRestoreDescriptor installDescriptor) {
    try {
      BackupDescriptor backupDescriptor = getBackupDescriptors(installDescriptor.getArchiveSourceId(), installDescriptor.getFilename());
      GameEmulator emulator = emulatorService.getGameEmulator(installDescriptor.getEmulatorId());


      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_INSTALL);
      jobDescriptor.setTitle("Restoring \"" + backupDescriptor.getFilename() + "\"");
      BackupInstallerJob job = new BackupInstallerJob(backupDescriptor, universalUploadService, gameService, emulator, cardService);
      jobDescriptor.setJob(job);

      jobService.offer(jobDescriptor);
      LOG.info("Offered restore job for \"" + backupDescriptor.getTableDetails().getGameDisplayName() + "\"");
    }
    catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean backupTable(@NonNull BackupExportDescriptor exportDescriptor) {
    List<Integer> gameIds = exportDescriptor.getGameIds();
    boolean result = true;
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null && game.getGameFile().exists()) {
        if (!backupTable(game, exportDescriptor)) {
          result = false;
        }
      }
      else {
        LOG.error("Cancelled backup for id " + game + ", invalid game data.");
        result = false;
      }
    }
    return result;
  }

  private boolean backupTable(@NonNull Game game, @NonNull BackupExportDescriptor exportDescriptor) {
    JobDescriptor descriptor = new JobDescriptor(JobType.TABLE_BACKUP);
    descriptor.setTitle("Backup of \"" + game.getGameDisplayName() + "\"");
    descriptor.setGameId(game.getId());

    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(game);

    BackupSourceAdapter sourceAdapter = getDefaultBackupSource();
    descriptor.setJob(new TableBackupJob(frontendService, sourceAdapter, adapter, exportDescriptor, game.getId()));
    jobService.offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }


  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) {
    if (PreferenceNames.AUTHENTICATION_SETTINGS.equalsIgnoreCase(propertyName)) {
      AuthenticationSettings authenticationSettings = preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class);
      if (authenticationSettings.isAuthenticated() && !StringUtils.isEmpty(authenticationSettings.getToken())) {
        VpaArchiveUtil.setPassword(authenticationSettings.getToken());
      }
    }
  }

  @Override
  public void afterPropertiesSet() {
    preferenceChanged(PreferenceNames.AUTHENTICATION_SETTINGS, null, null);
    preferencesService.addChangeListener(this);

    //VPA files
    if (systemService.getArchiveType().equals(BackupType.VPA)) {
      BackupSource backupSource = new VpaBackupSource();
      this.defaultBackupSourceAdapter = new BackupSourceAdapterFolder(vpaService, backupSource);
      this.backupSourcesCache.put(backupSource.getId(), this.defaultBackupSourceAdapter);
    }

    //EXTERNAL
    List<BackupSource> all = backupSourceRepository.findAll();
    for (BackupSource as : all) {
      BackupSourceAdapter vpaSourceAdapter = null;
      try {
        vpaSourceAdapter = BackupSourceAdapterFactory.create(this, as, vpaService);
      }
      catch (Exception e) {
        LOG.error("Failed to create archive source: {}", e.getMessage());
        continue;
      }
      this.backupSourcesCache.put(as.getId(), vpaSourceAdapter);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
