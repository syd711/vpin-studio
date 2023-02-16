package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VpaService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  public final static String DATA_HIGHSCORE_HISTORY = "highscores";
  public final static String DATA_HIGHSCORE = "highscore";
  public final static String DATA_VPREG_HIGHSCORE = "vpregHighscore";

  @Autowired
  private SystemService systemService;

  private VpaSource defaultVpaSource;
  private List<VpaSource> vpaSources = new ArrayList<>();

  public List<VpaDescriptor> getVpasFor(Game game) {
    return getVpaDescriptors().stream().filter(vpaDescriptor -> {
      VpaManifest manifest = vpaDescriptor.getManifest();
      return (manifest.getGameName() != null && manifest.getGameName().equals(game.getGameDisplayName())) ||
          (manifest.getGameFileName() != null && manifest.getGameFileName().equals(game.getGameFileName())) ||
          (manifest.getGameDisplayName() != null && manifest.getGameDisplayName().equals(game.getGameDisplayName()));
    }).collect(Collectors.toList());
  }

  public List<VpaDescriptor> getVpaDescriptors() {
    List<VpaDescriptor> result = new ArrayList<>();
    for (VpaSource vpaSource : vpaSources) {
      List<VpaDescriptor> descriptors = vpaSource.getDescriptors();
      result.addAll(descriptors);
    }
    return result;
  }

  public boolean deleteVpa(String uuid) {
    Optional<VpaDescriptor> first = getVpaDescriptors().stream().filter(vpaDescriptor -> vpaDescriptor.getManifest().getUuid().equals(uuid)).findFirst();
    if (first.isPresent()) {
      VpaDescriptor descriptor = first.get();
      descriptor.getSource().invalidate();
      return descriptor.getSource().delete(descriptor);
    }
    return false;
  }

  public VpaSource getDefaultVpaSource() {
    return this.defaultVpaSource;
  }

  @Override
  public void afterPropertiesSet() {
    this.defaultVpaSource = new VpaSourceFileSystem(systemService.getVpaArchiveFolder());
    this.vpaSources.add(defaultVpaSource);
  }
}
