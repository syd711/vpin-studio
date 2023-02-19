package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.representations.VpaSourceRepresentation;
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
      List<VpaDescriptor> descriptors = adapter.getDescriptors();
      result.addAll(descriptors);
    }
    result.sort(Comparator.comparing(VpaDescriptor::getFilename));
    return result;
  }

  @Nullable
  public VpaDescriptor getVpaDescriptor(@NonNull File out) {
    Optional<VpaDescriptor> first = getVpaDescriptors().stream().filter(f -> f.getFilename().equals(out.getName())).findFirst();
    return first.orElse(null);
  }

  @Nullable
  public VpaDescriptor getVpaDescriptor(@NonNull String uuid) {
    Optional<VpaDescriptor> first = getVpaDescriptors().stream().filter(f -> f.getManifest().getUuid().equals(uuid)).findFirst();
    return first.orElse(null);
  }

  public boolean deleteVpa(String uuid) {
    Optional<VpaDescriptor> first = getVpaDescriptors().stream().filter(vpaDescriptor -> vpaDescriptor.getManifest().getUuid().equals(uuid)).findFirst();
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

  public VpaSourceAdapter getDefaultVpaSourceAdapter() {
    return this.defaultVpaSourceAdapter;
  }

  public void invalidateDefaultCache() {
    this.getDefaultVpaSourceAdapter().invalidate();
  }

  public VpaSourceRepresentation save(VpaSourceRepresentation vpaSourceRepresentation) {
    Optional<VpaSource> byId = vpaSourceRepository.findById(vpaSourceRepresentation.getId());
    if(byId.isPresent()) {
      VpaSource vpaSource = byId.get();

    }
    return null;
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
