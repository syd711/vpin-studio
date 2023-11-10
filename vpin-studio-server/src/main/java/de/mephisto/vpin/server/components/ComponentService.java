package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ComponentService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentService.class);

  @Autowired
  private ComponentRepository componentRepository;

  private Map<ComponentType, GithubRelease> releases = new HashMap<>();

  public List<Component> getComponents() {
    return componentRepository.findAll();
  }

  public Component getComponent(ComponentType type) {
    Component component = componentRepository.findByType(type).get();
    if (releases.get(type) == null) {
      loadReleases(component);
    }
    return component;
  }

  public List<ReleaseArtifact> getLatestReleaseArtifacts(ComponentType type) {
    GithubRelease githubRelease = releases.get(type);
    if (githubRelease != null) {
      return githubRelease.getArtifacts();
    }
    return Collections.emptyList();
  }

  public boolean setVersion(@NonNull ComponentType type, @NonNull String version) {
    Optional<Component> byType = componentRepository.findByType(type);
    if (byType.isPresent()) {
      Component component = byType.get();
      component.setInstalledVersion(version);
      componentRepository.saveAndFlush(component);
      getComponent(type).setInstalledVersion(version);
      LOG.info("Applied version " + version + " for " + type.name());
      return true;
    }
    return false;
  }

  public ReleaseArtifactActionLog check(@NonNull GameEmulator emulator, @NonNull ComponentType type, @NonNull String artifact) {
    Component component = getComponent(type);
    GithubRelease githubRelease = releases.get(component.getType());
    ReleaseArtifactActionLog install = null;
    if (githubRelease == null || githubRelease.getLatestArtifact() == null) {
      throw new UnsupportedOperationException("Release or latest artifact for " + type.name() + " not found.");
    }

    ReleaseArtifact releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
    File targetFolder = resolveTargetFolder(emulator, type);
    install = releaseArtifact.diff(targetFolder);
    return install;
  }

  @NonNull
  public ReleaseArtifactActionLog install(@NonNull GameEmulator emulator, @NonNull ComponentType type, @NonNull String artifact, boolean simulate) {
    Component component = getComponent(type);
    GithubRelease githubRelease = releases.get(component.getType());
    ReleaseArtifactActionLog install = null;
    if (githubRelease == null || githubRelease.getLatestArtifact() == null) {
      throw new UnsupportedOperationException("Release or latest artifact for " + type.name() + " not found.");
    }

    ReleaseArtifact releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
    File targetFolder = resolveTargetFolder(emulator, type);
    if (simulate) {
      return releaseArtifact.simulateInstall(targetFolder);
    }

    install = releaseArtifact.install(targetFolder);
    if (install.getStatus() == null) {
      component.setInstalledVersion(githubRelease.getTag());
      componentRepository.saveAndFlush(component);
    }
    return install;
  }

  private File resolveTargetFolder(GameEmulator gameEmulator, ComponentType type) {
    switch (type) {
      case vpinmame: {
        return gameEmulator.getMameFolder();
      }
      case vpinball: {
        return gameEmulator.getInstallationFolder();
      }
      case b2sbackglass: {
        return gameEmulator.getTablesFolder();
      }
      default: {
        throw new UnsupportedOperationException("Unsupported component type '" + type + "'");
      }
    }
  }

  private void loadReleases(Component component) {
    try {
      GithubRelease githubRelease = null;
      switch (component.getType()) {
        case vpinmame: {
          githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/pinmame/releases", Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "sc-", "osx"));
          break;
        }
        case vpinball: {
          githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/vpinball/releases", Collections.emptyList(), Arrays.asList("Debug"));
          break;
        }
        case b2sbackglass: {
          githubRelease = GithubReleaseFactory.loadRelease("https://github.com/vpinball/b2s-backglass/releases", Collections.emptyList(), Arrays.asList("Source"));
          break;
        }
        default: {
          throw new UnsupportedOperationException("Unsupported component type '" + component.getType() + "'");
        }
      }

      component.setLatestReleaseVersion(githubRelease.getTag());
      componentRepository.saveAndFlush(component);
      this.releases.put(component.getType(), githubRelease);
    } catch (IOException e) {
      LOG.error("Failed to initialize release for " + component + ": " + e.getMessage(), e);
    }
  }

  public boolean clearCache() {
    ComponentType[] values = ComponentType.values();
    for (ComponentType value : values) {
      Optional<Component> byName = componentRepository.findByType(value);
      if (byName.isEmpty()) {
        Component component = new Component();
        component.setType(value);
        componentRepository.saveAndFlush(component);
      }
    }

    List<Component> all = componentRepository.findAll();
    for (Component component : all) {
      loadReleases(component);
    }
    return true;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
  }
}
