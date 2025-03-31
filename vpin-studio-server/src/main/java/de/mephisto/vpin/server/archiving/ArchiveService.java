package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.archiving.ArchiveType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.archiving.ArchiveSourceRepresentation;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaArchiveSource;
import de.mephisto.vpin.server.archiving.adapters.vpa.VpaArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmArchiveSource;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmArchiveSourceAdapter;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
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

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

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
  private VpbmService vpbmService;

  @Autowired
  private PreferencesService preferencesService;

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
  public ArchiveDescriptor getArchiveDescriptor(@NonNull File out) {
    Optional<ArchiveDescriptor> first = getArchiveDescriptors().stream().filter(f -> f.getFilename().equals(out.getName())).findFirst();
    return first.orElse(null);
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

  public boolean isValidArchiveDescriptor(@NonNull ArchiveDescriptor archiveDescriptor) {
    return archiveDescriptor.getFilename() != null;
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

    adapterCache.put(updatedSource.getId(), ArchiveSourceAdapterFactory.create(this, updatedSource));
    LOG.info("(Re)created archive source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  public File getTargetFile(ArchiveDescriptor archiveDescriptor) {
    String descriptorFilename = archiveDescriptor.getFilename();
    ArchiveType archiveType = ArchiveType.VPA;
    if (descriptorFilename.endsWith("vpinzip")) {
      archiveType = ArchiveType.VPBM;
    }

    switch (archiveType) {
      case VPBM: {
        return new File(vpbmService.getArchiveFolder(), archiveDescriptor.getFilename());
      }
      case VPA: {
        return new File(VpaArchiveSource.FOLDER, archiveDescriptor.getFilename());
      }
    }
    return null;
  }

  public File getArchivesFolder() {
    ArchiveType archiveType = systemService.getArchiveType();
    switch (archiveType) {
      case VPBM: {
        return vpbmService.getBundlesFolder();
      }
      case VPA: {
        return new File(RESOURCES, "vpa");
      }
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() {
    String systemName = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
    if(!StringUtils.isEmpty(systemName) && systemName.contains("Syd")) {
//      systemService.setArchiveType(ArchiveType.VPA);
//      LOG.info("Switched archiving mode to VPA.");
    }

    //VPA
    if (systemService.getArchiveType().equals(ArchiveType.VPA)) {
      ArchiveSource archiveSource = new VpaArchiveSource();
      this.defaultArchiveSourceAdapter = new VpaArchiveSourceAdapter(archiveSource);
      this.adapterCache.put(archiveSource.getId(), this.defaultArchiveSourceAdapter);
    }

    //VPBM
    if (systemService.getArchiveType().equals(ArchiveType.VPBM)) {
      File vpbmArchiveFolder = vpbmService.getArchiveFolder();
      ArchiveSource archiveSource = new VpbmArchiveSource(vpbmArchiveFolder);
      this.defaultArchiveSourceAdapter = new VpbmArchiveSourceAdapter(archiveSource, vpbmService);
      this.adapterCache.put(archiveSource.getId(), this.defaultArchiveSourceAdapter);
    }

    //EXTERNAL
    List<ArchiveSource> all = archiveSourceRepository.findAll();
    for (ArchiveSource as : all) {
      ArchiveSourceAdapter vpaSourceAdapter = ArchiveSourceAdapterFactory.create(this, as);
      this.adapterCache.put(as.getId(), vpaSourceAdapter);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
