package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.VPXZExportDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;
import de.mephisto.vpin.restclient.vpxz.VPXZType;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpauthenticators.VPAuthenticationService;
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
public class VPXZService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZService.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private VPXZSourceRepository vpxzSourceRepository;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private JobService jobService;

  @Autowired
  private VPXZFileService vpxzFileService;

  @Autowired
  private VPAuthenticationService vpAuthenticationService;

  @Autowired
  private PreferencesService preferencesService;

  private final Map<Long, VPXZSourceAdapter> vpxMobileSourcesCache = new LinkedHashMap<>();

  public List<VPXZDescriptor> getVPXZDescriptorForGame(int gameId) {
    Game game = gameService.getGame(gameId);

    return getVPXZDescriptors().stream().filter(vpxMobileDescriptor -> {
      TableDetails manifest = vpxMobileDescriptor.getTableDetails();
      if (manifest == null) {
        return false;
      }
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  @NonNull
  public List<VPXZDescriptor> getVPXZDescriptors() {
    List<VPXZDescriptor> result = new ArrayList<>();
    for (VPXZSourceAdapter adapter : vpxMobileSourcesCache.values()) {
      if (adapter.getVPXZSource().isEnabled()) {
        Collection<VPXZDescriptor> descriptors = adapter.getVPXZDescriptors();
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
  public List<VPXZDescriptor> getVPXZDescriptors(long sourceId) {
    if (!vpAuthenticationService.isAuthenticated()) {
      return Collections.emptyList();
    }

    VPXZSourceAdapter adapter = getVPXMobileSourceAdapter(sourceId);
    List<VPXZDescriptor> VPXZDescriptors = new ArrayList<>(adapter.getVPXZDescriptors());
    VPXZDescriptors.sort((o1, o2) -> {
      if (o1.getFilename() != null && o2.getFilename() != null) {
        return o1.getFilename().compareTo(o2.getFilename());
      }
      return o1.getTableDetails().getGameDisplayName().compareTo(o2.getTableDetails().getGameDisplayName());
    });

    return VPXZDescriptors;
  }

  @Nullable
  public VPXZDescriptor getVPXZDescriptors(long sourceId, @NonNull String filename) {
    VPXZSourceAdapter sourceAdapter = getVPXMobileSourceAdapter(sourceId);
    Collection<VPXZDescriptor> descriptors = sourceAdapter.getVPXZDescriptors();
    for (VPXZDescriptor descriptor : descriptors) {
      String descriptorFilename = descriptor.getFilename();
      if (filename.equals(descriptorFilename)) {
        return descriptor;
      }
    }
    return null;
  }

  public boolean deleteVPXZ(long sourceId, @NonNull String filename) {
    Collection<VPXZDescriptor> descriptors = vpxMobileSourcesCache.get(sourceId).getVPXZDescriptors();
    Optional<VPXZDescriptor> first = descriptors.stream().filter(ArchiveDescriptor -> ArchiveDescriptor.getFilename().equals(filename)).findFirst();
    if (first.isPresent()) {
      VPXZDescriptor descriptor = first.get();
      return getVPXMobileSourceAdapter(sourceId).delete(descriptor);
    }
    return false;
  }

  public boolean deleteVPXMobileSource(long id) {
    if (vpxMobileSourcesCache.containsKey(id)) {
      this.vpxMobileSourcesCache.remove(id);
      this.vpxzSourceRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public List<VPXZSource> getVPXMobileSources() {
    return vpxMobileSourcesCache.values().stream().map(VPXZSourceAdapter::getVPXZSource).collect(Collectors.toList());
  }

  public VPXZSourceAdapter getVPXMobileSourceAdapter(long sourceId) {
    return vpxMobileSourcesCache.get(sourceId);
  }

  public void invalidateCache() {
    Set<Map.Entry<Long, VPXZSourceAdapter>> entries = vpxMobileSourcesCache.entrySet();
    for (Map.Entry<Long, VPXZSourceAdapter> entry : entries) {
      entry.getValue().invalidate();
    }
  }

  public VPXZSource save(VPXZSourceRepresentation representation) {
    Optional<VPXZSource> byId = vpxzSourceRepository.findById(representation.getId());
    VPXZSource vpxzSource = null;
    if (byId.isPresent()) {
      vpxzSource = byId.get();
    }
    else {
      vpxzSource = new VPXZSource();
      vpxzSource.setCreatedAt(new Date());
      vpxzSource.setType(representation.getType());
    }

    vpxzSource.setLocation(representation.getLocation());
    vpxzSource.setName(representation.getName());
    vpxzSource.setAuthenticationType(representation.getAuthenticationType());
    vpxzSource.setLogin(representation.getLogin());
    vpxzSource.setPassword(representation.getPassword());
    vpxzSource.setEnabled(representation.isEnabled());
    vpxzSource.setSettings(representation.getSettings());

    VPXZSource updatedSource = vpxzSourceRepository.saveAndFlush(vpxzSource);
    vpxMobileSourcesCache.remove(updatedSource.getId());

    VPXZSourceAdapter VPXZSourceAdapter = VPXZSourceAdapterFactory.create(this, updatedSource, vpxzFileService);
    vpxMobileSourcesCache.put(updatedSource.getId(), VPXZSourceAdapter);
    LOG.info("(Re)created vpxz source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  public boolean createVpxz(@NonNull VPXZExportDescriptor vpxzDescriptor) {
    boolean result = true;
    List<Integer> gameIds = vpxzDescriptor.getGameIds();
    for (Integer gameId : gameIds) {
      Game game = gameService.getGame(gameId);
      if (game != null && game.getGameFile().exists()) {
        if (!createVpxz(game, vpxzDescriptor)) {
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

  private boolean createVpxz(@NonNull Game game, @NonNull VPXZExportDescriptor vpxzDescriptor) {
    JobDescriptor descriptor = new JobDescriptor(JobType.VPXZ_EXPORT);
    descriptor.setCancelable(true);
    descriptor.setTitle("Creating .vpxz for \"" + game.getGameDisplayName() + "\"");
    descriptor.setGameId(game.getId());

    Optional<VPXZSource> source = vpxzSourceRepository.findById(vpxzDescriptor.getSourceId());
    if (source.isPresent()) {
      TableDetails tableDetails = frontendService.getTableDetails(game.getId());
      VPXZSettings vpxzSettings = preferencesService.getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);

      descriptor.setJob(new VPXZCreationJob(vpxzFileService, source.get(), game, tableDetails, vpxzSettings));
      jobService.offer(descriptor);
      LOG.info("Offered vpxz export job for '" + game.getGameDisplayName() + "'");
      return true;
    }
    else {
      LOG.error("No matching vps source found for {}", vpxzDescriptor.getSourceId());
    }

    return false;
  }

  @Override
  public void afterPropertiesSet() {
    List<VPXZSource> all = vpxzSourceRepository.findAll();
    if (all.isEmpty()) {
      VPXZSource source = new VPXZSource();
      source.setCreatedAt(new Date());
      source.setName("Default VPXZ Folder");
      source.setType(VPXZSourceType.Folder.name());
      source.setLocation(VPXZSourceImpl.FOLDER.getAbsolutePath());
      source.setEnabled(true);
      vpxzSourceRepository.saveAndFlush(source);
    }

    all = vpxzSourceRepository.findAll();
    for (VPXZSource as : all) {
      VPXZSourceAdapter vpxzSourceAdapter = null;
      try {
        vpxzSourceAdapter = VPXZSourceAdapterFactory.create(this, as, vpxzFileService);
      }
      catch (Exception e) {
        LOG.error("Failed to create VPXZ source: {}", e.getMessage(), e);
        continue;
      }
      this.vpxMobileSourcesCache.put(as.getId(), vpxzSourceAdapter);
    }

    new Thread(() -> {
      Thread.currentThread().setName("VPXZ Service Initializer");
      Collection<VPXZSourceAdapter> values = vpxMobileSourcesCache.values();
      int count = 0;
      for (VPXZSourceAdapter value : values) {
        count += value.getVPXZDescriptors().size();
      }
      LOG.info("VPXZ Service initialized with {} files.", count);
    }).start();

    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
