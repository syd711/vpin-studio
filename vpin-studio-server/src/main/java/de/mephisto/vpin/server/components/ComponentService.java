package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.*;
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
    List<Component> all = componentRepository.findAll();
    all.stream().forEach(c -> getComponent(c.getType()));
    return all;
  }

  public Component getComponent(ComponentType type) {
    Component component = componentRepository.findByType(type).get();
    if (releases.get(type) == null) {
      loadReleases(component);
    }
    return component;
  }

  public GithubRelease getLatestRelease(ComponentType type) {
    return releases.get(type);
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
    ComponentFacade componentFacade = getComponentFacade(type);
    File targetFolder = componentFacade.getTargetFolder(emulator);
    install = releaseArtifact.diff(targetFolder, componentFacade.isSkipRootFolder(), componentFacade.getExclusionList(), componentFacade.getDiffList());
    boolean diff = install.isDiffering();
    if (!diff) {
      component.setInstalledVersion(githubRelease.getTag());
      LOG.info("Applied current version \"" + githubRelease.getTag() + " for " + component.getType());
    }

    component.setLastCheck(new Date());
    componentRepository.saveAndFlush(component);

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
    ComponentFacade componentFacade = getComponentFacade(type);
    File targetFolder = componentFacade.getTargetFolder(emulator);
    if (simulate) {
      return releaseArtifact.simulateInstall(targetFolder, componentFacade.isSkipRootFolder(), componentFacade.getExclusionList());
    }

    //we have a real installation from here on
    install = releaseArtifact.install(targetFolder, componentFacade.isSkipRootFolder(), componentFacade.getExclusionList());
    if (install.getStatus() == null) {
      //unzipping was successful
      component.setInstalledVersion(githubRelease.getTag());
      componentRepository.saveAndFlush(component);

      //execute optional post processing
      componentFacade.postProcess(emulator, releaseArtifact, install);
    }

    return install;
  }

  private void loadReleases(Component component) {
    try {
      ComponentFacade componentFacade = getComponentFacade(component.getType());
      GithubRelease githubRelease = componentFacade.loadRelease();
      component.setLatestReleaseVersion(githubRelease.getTag());
      componentRepository.saveAndFlush(component);
      this.releases.put(component.getType(), githubRelease);
    } catch (IOException e) {
      LOG.error("Failed to initialize release for " + component + ": " + e.getMessage(), e);
    }
  }

  public boolean clearCache() {
    List<Component> all = componentRepository.findAll();
    for (Component component : all) {
      loadReleases(component);
    }
    return true;
  }

  public ComponentFacade getComponentFacade(ComponentType type) {
    switch (type) {
      case vpinmame: {
        return new VPinMAMEComponent();
      }
      case vpinball: {
        return new VpxComponent();
      }
      case b2sbackglass: {
        return new BackglassComponent();
      }
      case flexdmd: {
        return new FlexDMDComponent();
      }
      case freezy: {
        return new FreezyComponent();
      }
      case serum: {
        return new SerumComponent();
      }
      default: {
        throw new UnsupportedOperationException("Invalid component type " + type);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ComponentType[] values = ComponentType.values();
    for (ComponentType value : values) {
      Optional<Component> byName = componentRepository.findByType(value);
      if (byName.isEmpty()) {
        Component component = new Component();
        component.setType(value);
        componentRepository.saveAndFlush(component);
      }
    }
  }
}
