package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.restclient.representations.ArchiveSourceRepresentation;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
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

  private ArchiveSourceAdapterFileSystem defaultArchiveSourceAdapter;
  
  private final Map<Long, ArchiveSourceAdapter> adapterCache = new LinkedHashMap<>();

  public List<ArchiveDescriptor> getArchiveDescriptors(int gameId) {
    Game game = gameService.getGame(gameId);
    
    return getArchiveDescriptors().stream().filter(ArchiveDescriptor -> {
      TableDetails manifest = ArchiveDescriptor.getTableDetails();
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  @Nullable
  public List<ArchiveDescriptor> getArchiveDescriptors() {
    List<ArchiveDescriptor> result = new ArrayList<>();
    for (ArchiveSourceAdapter adapter : adapterCache.values()) {
      if(adapter.getArchiveSource().isEnabled()) {
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
      if(filename.equals(descriptorFilename)) {
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
      return getDefaultArchiveSourceAdapter().delete(descriptor);
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

  public ArchiveSourceAdapterFileSystem getDefaultArchiveSourceAdapter() {
    return this.defaultArchiveSourceAdapter;
  }

  public ArchiveSourceAdapter getArchiveSourceAdapter(long sourceId) {
    return adapterCache.get(sourceId);
  }

  public void invalidateCache(long id) {
    ArchiveSourceAdapter sourceAdapter = this.adapterCache.get(id);
    if(sourceAdapter != null) {
      sourceAdapter.invalidate();
    }
    else {
      LOG.error("Invalid archive source adapter id " + id);
    }
  }

  public ArchiveSource save(ArchiveSourceRepresentation representation) {
    Optional<ArchiveSource> byId = archiveSourceRepository.findById(representation.getId());
    ArchiveSource vpaSource = null;
    if (byId.isPresent()) {
      vpaSource = byId.get();
    }
    else {
      vpaSource = new ArchiveSource();
      vpaSource.setCreatedAt(new Date());
      vpaSource.setType(representation.getType());
    }

    vpaSource.setLocation(representation.getLocation());
    vpaSource.setName(representation.getName());
    vpaSource.setAuthenticationType(representation.getAuthenticationType());
    vpaSource.setLogin(representation.getLogin());
    vpaSource.setPassword(representation.getPassword());
    vpaSource.setEnabled(representation.isEnabled());
    vpaSource.setSettings(representation.getSettings());

    ArchiveSource updatedSource = archiveSourceRepository.saveAndFlush(vpaSource);
    adapterCache.remove(updatedSource.getId());

    adapterCache.put(updatedSource.getId(), ArchiveSourceAdapterFactory.create(updatedSource));
    LOG.info("(Re)created VPA source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  @Override
  public void afterPropertiesSet() {
    DefaultArchiveSource defaultVpaSource = new DefaultArchiveSource(systemService.getVpaArchiveFolder());
    defaultArchiveSourceAdapter = new ArchiveSourceAdapterFileSystem(defaultVpaSource);
    this.adapterCache.put(defaultVpaSource.getId(), defaultArchiveSourceAdapter);

    List<ArchiveSource> all = archiveSourceRepository.findAll();
    for (ArchiveSource vpaSource : all) {
      ArchiveSourceAdapter vpaSourceAdapter = ArchiveSourceAdapterFactory.create(vpaSource);
      this.adapterCache.put(vpaSource.getId(), vpaSourceAdapter);
    }
  }
}
