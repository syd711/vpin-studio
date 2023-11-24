package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.ReleaseArtifact;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.*;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private SystemService systemService;

  private Map<ComponentType, GithubRelease> releases = new HashMap<>();

  public List<Component> getComponents() {
    List<Component> result = new ArrayList<>();
    ComponentType[] values = ComponentType.values();
    for (ComponentType value : values) {
      result.add(getComponent(value));
    }
    return result;
  }

  public Component getComponent(ComponentType type) {
    return componentRepository.findByType(type).get();
  }

  public GithubRelease getLatestRelease(ComponentType type) {
    return releases.get(type);
  }

  public boolean setVersion(@NonNull ComponentType type, @NonNull String version) {
    Optional<Component> byType = componentRepository.findByType(type);
    if (byType.isPresent()) {
      if (StringUtils.isEmpty(version) || version.equals("-")) {
        version = null;
      }
      Component component = byType.get();
      component.setInstalledVersion(version);
      componentRepository.saveAndFlush(component);
      getComponent(type).setInstalledVersion(version);
      LOG.info("Applied version " + version + " for " + type.name());
      return true;
    }
    return false;
  }

  public ReleaseArtifactActionLog check(@NonNull GameEmulator emulator, @NonNull ComponentType type, @NonNull String artifact, boolean forceDownload) {
    Component component = getComponent(type);
    if (StringUtils.isEmpty(component.getLatestReleaseVersion()) || forceDownload) {
      loadReleases(component);

      GithubRelease githubRelease = releases.get(component.getType());
      ReleaseArtifactActionLog install = null;
      if (githubRelease == null || githubRelease.getLatestArtifact() == null) {
        throw new UnsupportedOperationException("Release or latest artifact for " + type.name() + " not found.");
      }

      ReleaseArtifact releaseArtifact = getReleaseArtifact(artifact, githubRelease);
      ComponentFacade componentFacade = getComponentFacade(type);
      File targetFolder = componentFacade.getTargetFolder(emulator);

      File folder = systemService.getComponentArchiveFolder(type);
      File artifactArchive = new File(folder, releaseArtifact.getName());
      if (artifactArchive.exists() && forceDownload) {
        artifactArchive.delete();
      }

      install = releaseArtifact.diff(artifactArchive, targetFolder, componentFacade.isSkipRootFolder(), componentFacade.getExclusionList(), componentFacade.getDiffList());
      boolean diff = install.isDiffering();
      if (!diff) {
        component.setInstalledVersion(githubRelease.getTag());
        LOG.info("Applied current version \"" + githubRelease.getTag() + " for " + component.getType());
      }
      else {
        if (githubRelease.getTag().equals(component.getInstalledVersion())) {
          component.setInstalledVersion("?");
          LOG.info("Reverted current version for " + component.getType());
        }
      }

      component.setLastCheck(new Date());
      componentRepository.saveAndFlush(component);

      return install;
    }
    return new ReleaseArtifactActionLog(false, true);
  }

  @Nullable
  private ReleaseArtifact getReleaseArtifact(@NotNull String artifact, GithubRelease githubRelease) {
    ReleaseArtifact releaseArtifact = null;
    if (artifact.equals("-latest-")) {
      if (githubRelease.getArtifacts().size() == 1) {
        releaseArtifact = githubRelease.getLatestArtifact();
      }
      else {
        String systemPreset = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_PRESET);
        if (systemPreset == null || systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT)) {
          releaseArtifact = githubRelease.getArtifacts().stream().filter(r -> r.getName().contains("x64")).findFirst().orElse(null);
        }
        else {
          releaseArtifact = githubRelease.getArtifacts().stream().filter(r -> !r.getName().contains("x64")).findFirst().orElse(null);
        }
      }

    }

    if (releaseArtifact == null) {
      releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
    }
    return releaseArtifact;
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
      this.releases.put(value, null);
      Optional<Component> byName = componentRepository.findByType(value);
      if (byName.isEmpty()) {
        Component component = new Component();
        component.setType(value);
        component.setInstalledVersion("?");
        componentRepository.saveAndFlush(component);
      }
    }
  }
}
