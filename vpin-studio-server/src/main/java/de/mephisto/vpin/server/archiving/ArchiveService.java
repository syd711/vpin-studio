package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.BackupDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapter;
import de.mephisto.vpin.server.archiving.adapters.TableBackupAdapterFactory;
import de.mephisto.vpin.server.archiving.adapters.vpa.ArchiveSourceAdapterFolder;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaArchiveSource;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchiveService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private ArchiveSourceRepository archiveSourceRepository;

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

  private ArchiveSourceAdapter defaultArchiveSourceAdapter;

  private final Map<Long, ArchiveSourceAdapter> adapterCache = new LinkedHashMap<>();

  public List<ArchiveDescriptor> getArchiveDescriptorForGame(int gameId) {
    Game game = gameService.getGame(gameId);

    return getArchiveDescriptors().stream().filter(ArchiveDescriptor -> {
      TableDetails manifest = ArchiveDescriptor.getTableDetails();
      if (manifest == null) {
        return false;
      }
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  @NonNull
  public List<ArchiveDescriptor> getArchiveDescriptors() {
    List<ArchiveDescriptor> result = new ArrayList<>();
    for (ArchiveSourceAdapter adapter : adapterCache.values()) {
      if (adapter.getArchiveSource().isEnabled()) {
        List<ArchiveDescriptor> descriptors = adapter.getArchiveDescriptors();
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
  public List<ArchiveDescriptor> getArchiveDescriptors(long sourceId) {
    ArchiveSourceAdapter adapter = getArchiveSourceAdapter(sourceId);
    List<ArchiveDescriptor> archiveDescriptors = adapter.getArchiveDescriptors();
    archiveDescriptors.sort((o1, o2) -> {
      if (o1.getFilename() != null && o2.getFilename() != null) {
        return o1.getFilename().compareTo(o2.getFilename());
      }
      return o1.getTableDetails().getGameDisplayName().compareTo(o2.getTableDetails().getGameDisplayName());
    });

    return archiveDescriptors;
  }

  @Nullable
  public ArchiveDescriptor getArchiveDescriptor(long sourceId, @NonNull String filename) {
    ArchiveSourceAdapter sourceAdapter = adapterCache.get(sourceId);
    List<ArchiveDescriptor> descriptors = sourceAdapter.getArchiveDescriptors();
    for (ArchiveDescriptor descriptor : descriptors) {
      String descriptorFilename = descriptor.getFilename();
      if (filename.equals(descriptorFilename)) {
        return descriptor;
      }
    }
    return null;
  }

  public boolean deleteArchiveDescriptor(long sourceId, @NonNull String filename) {
    List<ArchiveDescriptor> descriptors = adapterCache.get(sourceId).getArchiveDescriptors();
    Optional<ArchiveDescriptor> first = descriptors.stream().filter(ArchiveDescriptor -> ArchiveDescriptor.getFilename().equals(filename)).findFirst();
    if (first.isPresent()) {
      ArchiveDescriptor descriptor = first.get();
      return getArchiveSourceAdapter(sourceId).delete(descriptor);
    }
    return false;
  }

  public boolean deleteArchiveSource(long id) {
    if (adapterCache.containsKey(id)) {
      this.adapterCache.remove(id);
      this.archiveSourceRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public List<ArchiveSource> getArchiveSources() {
    return adapterCache.values().stream().map(ArchiveSourceAdapter::getArchiveSource).collect(Collectors.toList());
  }

  public ArchiveSourceAdapter getDefaultArchiveSourceAdapter() {
    return this.defaultArchiveSourceAdapter;
  }

  public ArchiveSourceAdapter getArchiveSourceAdapter(long sourceId) {
    return adapterCache.get(sourceId);
  }

  public File export(ArchiveDescriptor archiveDescriptor) {
    return getDefaultArchiveSourceAdapter().export(archiveDescriptor);
  }

  public void invalidateCache() {
    Set<Map.Entry<Long, ArchiveSourceAdapter>> entries = adapterCache.entrySet();
    for (Map.Entry<Long, ArchiveSourceAdapter> entry : entries) {
      entry.getValue().invalidate();
    }
  }

  public ArchiveSource save(ArchiveSourceRepresentation representation) {
    Optional<ArchiveSource> byId = archiveSourceRepository.findById(representation.getId());
    ArchiveSource archiveSource = null;
    if (byId.isPresent()) {
      archiveSource = byId.get();
    }
    else {
      archiveSource = new ArchiveSource();
      archiveSource.setCreatedAt(new Date());
      archiveSource.setType(representation.getType());
    }

    archiveSource.setLocation(representation.getLocation());
    archiveSource.setName(representation.getName());
    archiveSource.setAuthenticationType(representation.getAuthenticationType());
    archiveSource.setLogin(representation.getLogin());
    archiveSource.setPassword(representation.getPassword());
    archiveSource.setEnabled(representation.isEnabled());
    archiveSource.setSettings(representation.getSettings());

    ArchiveSource updatedSource = archiveSourceRepository.saveAndFlush(archiveSource);
    adapterCache.remove(updatedSource.getId());

    adapterCache.put(updatedSource.getId(), ArchiveSourceAdapterFactory.create(this, updatedSource, vpaService));
    LOG.info("(Re)created archive source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  public File getTargetFile(ArchiveDescriptor archiveDescriptor) {
    ArchiveType archiveType = ArchiveType.VPA;

    switch (archiveType) {
      case VPA: {
        return new File(VpaArchiveSource.FOLDER, archiveDescriptor.getFilename());
      }
    }
    return null;
  }


  public boolean restoreArchive(@NonNull ArchiveRestoreDescriptor installDescriptor) {
    try {
      ArchiveDescriptor archiveDescriptor = getArchiveDescriptor(installDescriptor.getArchiveSourceId(), installDescriptor.getFilename());
      GameEmulator emulator = emulatorService.getGameEmulator(installDescriptor.getEmulatorId());


      JobDescriptor jobDescriptor = new JobDescriptor(JobType.ARCHIVE_INSTALL);
      jobDescriptor.setTitle("Restoring \"" + archiveDescriptor.getFilename() + "\"");
      ArchiveInstallerJob job = new ArchiveInstallerJob(archiveDescriptor, universalUploadService, gameService, emulator, cardService);
      jobDescriptor.setJob(job);

      jobService.offer(jobDescriptor);
      LOG.info("Offered restore job for \"" + archiveDescriptor.getTableDetails().getGameDisplayName() + "\"");
    }
    catch (Exception e) {
      LOG.error("Import failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public boolean backupTable(@NonNull BackupDescriptor exportDescriptor) {
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

  private boolean backupTable(@NonNull Game game, @NonNull BackupDescriptor exportDescriptor) {
    JobDescriptor descriptor = new JobDescriptor(JobType.TABLE_BACKUP);
    descriptor.setTitle("Backup of \"" + game.getGameDisplayName() + "\"");
    descriptor.setGameId(game.getId());

    TableBackupAdapter adapter = tableBackupAdapterFactory.createAdapter(game);

    ArchiveSourceAdapter sourceAdapter = getDefaultArchiveSourceAdapter();
    descriptor.setJob(new TableBackupJob(frontendService, sourceAdapter, adapter, exportDescriptor, game.getId()));
    jobService.offer(descriptor);
    LOG.info("Offered export job for '" + game.getGameDisplayName() + "'");
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    //VPA files
    if (systemService.getArchiveType().equals(ArchiveType.VPA)) {
      ArchiveSource archiveSource = new VpaArchiveSource();
      this.defaultArchiveSourceAdapter = new ArchiveSourceAdapterFolder(vpaService, archiveSource);
      this.adapterCache.put(archiveSource.getId(), this.defaultArchiveSourceAdapter);
    }

    //EXTERNAL
    List<ArchiveSource> all = archiveSourceRepository.findAll();
    for (ArchiveSource as : all) {
      ArchiveSourceAdapter vpaSourceAdapter = null;
      try {
        vpaSourceAdapter = ArchiveSourceAdapterFactory.create(this, as, vpaService);
      }
      catch (Exception e) {
        LOG.error("Failed to create archive source: {}", e.getMessage());
        continue;
      }
      this.adapterCache.put(as.getId(), vpaSourceAdapter);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
