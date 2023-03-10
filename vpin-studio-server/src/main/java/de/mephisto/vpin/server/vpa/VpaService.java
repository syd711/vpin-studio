package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
import de.mephisto.vpin.restclient.util.PasswordUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
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

@Service
public class VpaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);


  public final static String DATA_HIGHSCORE_HISTORY = "highscores";
  public final static String DATA_HIGHSCORE = "highscore";
  public final static String DATA_VPREG_HIGHSCORE = "vpregHighscore";

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private VpaSourceRepository vpaSourceRepository;

  private VpaSourceAdapterFileSystem defaultVpaSourceAdapter;
  private final Map<Long, VpaSourceAdapter> adapterCache = new HashMap<>();

  public List<VpaDescriptor> getVpaDescriptors(int gameId) {
    Game game = gameService.getGame(gameId);
    return getVpaDescriptors().stream().filter(vpaDescriptor -> {
      VpaManifest manifest = vpaDescriptor.getManifest();
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  @Nullable
  public List<VpaDescriptor> getVpaDescriptors() {
    List<VpaDescriptor> result = new ArrayList<>();
    for (VpaSourceAdapter adapter : adapterCache.values()) {
      if(adapter.getVpaSource().isEnabled()) {
        List<VpaDescriptor> descriptors = adapter.getVpaDescriptors();
        result.addAll(descriptors);
      }
    }
    result.sort((o1, o2) -> {
      if (o1.getFilename() != null && o2.getFilename() != null) {
        return o1.getFilename().compareTo(o2.getFilename());
      }
      return o1.getManifest().getGameDisplayName().compareTo(o2.getManifest().getGameDisplayName());
    });
    return result;
  }

  @Nullable
  public VpaDescriptor getVpaDescriptor(@NonNull File out) {
    Optional<VpaDescriptor> first = getVpaDescriptors().stream().filter(f -> f.getFilename().equals(out.getName())).findFirst();
    return first.orElse(null);
  }

  @Nullable
  public VpaDescriptor getVpaDescriptor(long sourceId, @NonNull String uuid) {
    VpaSourceAdapter vpaSourceAdapter = adapterCache.get(sourceId);
    List<VpaDescriptor> vpaDescriptors = vpaSourceAdapter.getVpaDescriptors();
    for (VpaDescriptor vpaDescriptor : vpaDescriptors) {
      String descriptorUUID = vpaDescriptor.getManifest().getUuid();
      if(descriptorUUID.equals(uuid)) {
        return vpaDescriptor;
      }
    }
    return null;
  }

  public boolean deleteVpaDescriptor(long sourceId, @NonNull String uuid) {
    List<VpaDescriptor> descriptors = adapterCache.get(sourceId).getVpaDescriptors();
    Optional<VpaDescriptor> first = descriptors.stream().filter(vpaDescriptor -> vpaDescriptor.getManifest().getUuid().equals(uuid)).findFirst();
    if (first.isPresent()) {
      VpaDescriptor descriptor = first.get();
      return getDefaultVpaSourceAdapter().delete(descriptor);
    }
    return false;
  }

  public boolean deleteVpaSource(long id) {
    if (adapterCache.containsKey(id)) {
      this.adapterCache.remove(id);
      this.vpaSourceRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public List<VpaSource> getVpaSources() {
    return adapterCache.values().stream().map(VpaSourceAdapter::getVpaSource).collect(Collectors.toList());
  }

  public VpaSourceAdapterFileSystem getDefaultVpaSourceAdapter() {
    return this.defaultVpaSourceAdapter;
  }

  public VpaSourceAdapter getVpaSourceAdapter(long sourceId) {
    return adapterCache.get(sourceId);
  }

  public void invalidateCache(long id) {
    VpaSourceAdapter vpaSourceAdapter = this.adapterCache.get(id);
    if(vpaSourceAdapter != null) {
      vpaSourceAdapter.invalidate();
    }
    else {
      LOG.error("Invalid VPA source adapter id " + id);
    }
  }

  public VpaSource save(VpaSourceRepresentation representation) {
    Optional<VpaSource> byId = vpaSourceRepository.findById(representation.getId());
    VpaSource vpaSource = null;
    if (byId.isPresent()) {
      vpaSource = byId.get();
    }
    else {
      vpaSource = new VpaSource();
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

    VpaSource updatedSource = vpaSourceRepository.saveAndFlush(vpaSource);
    adapterCache.remove(updatedSource.getId());

    adapterCache.put(updatedSource.getId(), VpaSourceAdapterFactory.create(updatedSource));
    LOG.info("(Re)created VPA source adapter \"" + updatedSource + "\"");
    return updatedSource;
  }

  @Override
  public void afterPropertiesSet() {
    DefaultVpaSource defaultVpaSource = new DefaultVpaSource(systemService.getVpaArchiveFolder());
    defaultVpaSourceAdapter = new VpaSourceAdapterFileSystem(defaultVpaSource);
    this.adapterCache.put(defaultVpaSource.getId(), defaultVpaSourceAdapter);

    List<VpaSource> all = vpaSourceRepository.findAll();
    for (VpaSource vpaSource : all) {
      VpaSourceAdapter vpaSourceAdapter = VpaSourceAdapterFactory.create(vpaSource);
      this.adapterCache.put(vpaSource.getId(), vpaSourceAdapter);
    }
  }
}
