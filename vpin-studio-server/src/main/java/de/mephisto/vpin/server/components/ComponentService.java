package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.GithubReleaseFactory;
import de.mephisto.githubloader.InstallLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
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

  private Map<Component, GithubRelease> components = new HashMap<>();

  public List<Component> getComponents() {
    return new ArrayList<>(components.keySet());
  }

  public Component getComponent(ComponentType type) {
    return components.keySet().stream().filter(c -> c.getType().equals(type)).findFirst().orElse(null);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ComponentType[] values = ComponentType.values();
    for (ComponentType value : values) {
      Optional<Component> byName = componentRepository.findByType(value);
      if (byName.isEmpty()) {
        Component component = new Component();
        component.setType(value);
        Component updatedComponent = componentRepository.saveAndFlush(component);
        fillComponentForType(value, updatedComponent);
      }
      else {
        fillComponentForType(value, byName.get());
      }
    }
  }

  public boolean setVersion(@NonNull ComponentType type, @NonNull String version) {
    Optional<Component> byType = componentRepository.findByType(type);
    if (byType.isPresent()) {
      Component component = byType.get();
      component.setInstalledVersion(version);
      componentRepository.saveAndFlush(component);
      return true;
    }
    return false;
  }

  @Nullable
  public InstallLog install(@NonNull GameEmulator emulator, @NonNull ComponentType type, boolean simulate) {
    Component component = getComponent(type);
    GithubRelease githubRelease = components.get(component);
    if (githubRelease != null && githubRelease.getLatestArtifact() != null) {
      File targetFolder = resolveTargetFolder(emulator, type);
      if (simulate) {
        return githubRelease.getLatestArtifact().simulateInstall(targetFolder);
      }
      return githubRelease.getLatestArtifact().install(targetFolder);
    }
    return null;
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

  private void fillComponentForType(ComponentType type, Component component) {
    switch (type) {
      case vpinmame: {
        component.setUrl("https://github.com/vpinball/pinmame/releases");
        setLatestReleaseVersion(component, Arrays.asList("win-", "VPinMAME"), Arrays.asList("linux", "sc-", "osx"));
        break;
      }
      case vpinball: {
        component.setUrl("https://github.com/vpinball/vpinball/releases");
        setLatestReleaseVersion(component, Collections.emptyList(), Arrays.asList("Debug"));
        break;
      }
      case b2sbackglass: {
        component.setUrl("https://github.com/vpinball/b2s-backglass/releases");
        setLatestReleaseVersion(component, Collections.emptyList(), Arrays.asList("Source"));
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unsupported component type '" + type + "'");
      }
    }
  }

  private void setLatestReleaseVersion(Component component, List<String> allowList, List<String> denyList) {
    new Thread(() -> {
      try {
        GithubRelease githubRelease = GithubReleaseFactory.loadRelease(component.getUrl(), allowList, denyList);
        if (githubRelease != null) {
          component.setLatestReleaseVersion(githubRelease.getTag());
        }
        this.components.put(component, githubRelease);
      } catch (IOException e) {
        LOG.error("Failed to retrieve release information for \"" + component + "\": " + e.getMessage(), e);
      }
    }).start();
  }
}
