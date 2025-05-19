package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.*;
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
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComponentService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentService.class);

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private SystemService systemService;

  @Autowired
  private VPinMAMEComponent vPinMAMEComponent;

  @Autowired
  private VpxComponent vpxComponent;

  @Autowired
  private BackglassComponent backglassComponent;

  @Autowired
  private FlexDMDComponent flexDMDComponent;

  @Autowired
  private FreezyComponent freezyComponent;

  @Autowired
  private SerumComponent serumComponent;

  @Autowired
  private DOFLinxComponent dofLinxComponent;

  @Autowired
  private DOFComponent dofComponent;

  private Map<ComponentType, List<GithubRelease>> releaseCache = new HashMap<>();

  public List<Component> getComponents() {
    List<Component> result = new ArrayList<>();
    ComponentType[] values = ComponentType.getValues();
    // sort by visibility order
    Arrays.sort(values, (a, b) -> a.getOrder() - b.getOrder());

    for (ComponentType value : values) {
      result.add(getComponent(value));
    }
    return result;
  }

  public Component getComponent(ComponentType type) {
    return componentRepository.findByType(type).get();
  }

  public List<GithubRelease> getReleases(ComponentType type) {
    return releaseCache.get(type);
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

  public boolean ignoreVersion(@NonNull ComponentType type, @NonNull String version) {
    Optional<Component> byType = componentRepository.findByType(type);
    if (byType.isPresent()) {
      Component component = byType.get();
      String ignored = component.getIgnoredVersions();
      List<String> values = new ArrayList<>();
      if (ignored != null) {
        values.addAll(Arrays.asList(ignored.split(",")));
      }
      values.add(version);
      List<String> listWithoutDuplicates = new ArrayList<>(new HashSet<>(values));
      component.setIgnoredVersions(String.join(",", listWithoutDuplicates));
      componentRepository.saveAndFlush(component);
      LOG.info("Ignored version " + version + " for " + type.name());
      loadReleases(component);
      return true;
    }
    return false;
  }

  public ReleaseArtifactActionLog check(@NonNull ComponentType type, @NonNull String tag, @NonNull String artifact, 
        String targetDirectory, boolean forceDownload) {
    Component component = getComponent(type);
    ComponentFacade componentFacade = getComponentFacade(type);
    if (targetDirectory != null) {
      if (StringUtils.isEmpty(component.getLatestReleaseVersion()) || forceDownload) {
        loadReleases(component);

        ReleaseArtifactActionLog install = null;
        List<GithubRelease> githubReleases = getCached(component);
        Optional<GithubRelease> first = githubReleases.stream().filter(r -> r.getTag().equals(tag)).findFirst();
        GithubRelease githubRelease = githubReleases.get(0);
        if (first.isPresent()) {
          githubRelease = first.get();
        }

        if (githubRelease != null) {
          ReleaseArtifact releaseArtifact = getReleaseArtifact(artifact, githubRelease);
          File targetFolder = new File(targetDirectory);

          File folder = systemService.getComponentArchiveFolder(type);
          File artifactArchive = new File(folder, releaseArtifact.getName());
          if (artifactArchive.exists() && forceDownload) {
            artifactArchive.delete();
          }

          install = releaseArtifact.diff(artifactArchive, targetFolder, componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames(), componentFacade.getDiffList());
          boolean diff = install.isDiffering();
          if (!diff) {
            component.setInstalledVersion(githubRelease.getTag());
            LOG.info("Applied current version \"" + githubRelease.getTag() + " for " + component.getType());
          }

          component.setLastCheck(new Date());
          componentRepository.saveAndFlush(component);
        }
        else {
          throw new UnsupportedOperationException(type + " release for tag \"" + tag + "\" not found.");
        }

        return install;
      }
    }
    return new ReleaseArtifactActionLog(false, true);
  }

  @Nullable
  private ReleaseArtifact getReleaseArtifact(@NonNull String artifact, GithubRelease githubRelease) {
    ReleaseArtifact releaseArtifact = null;
    if (artifact.equals("-latest-")) {
      if (githubRelease.getArtifacts().size() == 1) {
        releaseArtifact = githubRelease.getLatestArtifact();
      }
      else {
        releaseArtifact = githubRelease.getArtifacts().stream().filter(r -> r.getName().contains("x64")).findFirst().orElse(null);
      }
    }

    if (releaseArtifact == null) {
      releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
    }

    if (releaseArtifact == null) {
      releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().toLowerCase().contains("release")).findFirst().orElse(null);
    }
    return releaseArtifact;
  }

  @NonNull
  public ReleaseArtifactActionLog install(@NonNull ComponentType type, @NonNull String tag, @NonNull String artifact, 
        String targetDirectory, boolean simulate) {
    Component component = getComponent(type);
    List<GithubRelease> githubReleases = getCached(component);
    Optional<GithubRelease> first = githubReleases.stream().filter(g -> g.getTag().equals(tag)).findFirst();
    if (first.isPresent()) {
      ReleaseArtifactActionLog install = null;
      GithubRelease githubRelease = first.get();
      ReleaseArtifact releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
      ComponentFacade componentFacade = getComponentFacade(type);
      File targetFolder = new File(targetDirectory);
      if (simulate) {
        return releaseArtifact.simulateInstall(targetFolder, componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames());
      }

      componentFacade.preProcess(releaseArtifact, install);

      //we have a real installation from here on
      install = releaseArtifact.install(targetFolder, componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames());
      if (install.getStatus() == null) {
        //unzipping was successful
        component.setInstalledVersion(githubRelease.getTag());
        componentRepository.saveAndFlush(component);

        //execute optional post processing
        componentFacade.postProcess(releaseArtifact, install);
      }

      return install;
    }
    else {
      throw new UnsupportedOperationException(type + " release for tag \"" + tag + "\" not found.");
    }
  }

  private void loadReleases(Component component) {
    try {
      ComponentFacade componentFacade = getComponentFacade(component.getType());
      List<GithubRelease> githubReleases = componentFacade.loadReleases();
      if (!StringUtils.isEmpty(component.getIgnoredVersions())) {
        githubReleases = githubReleases.stream().filter(release -> !component.getIgnoredVersions().contains(release.getTag())).collect(Collectors.toList());
      }
      if (!githubReleases.isEmpty()) {
        component.setLatestReleaseVersion(githubReleases.get(0).getTag());
      }
      componentRepository.saveAndFlush(component);
      this.releaseCache.put(component.getType(), githubReleases);
    }
    catch (IOException e) {
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

  public List<GithubRelease> getCached(Component component) {
    if (this.releaseCache.containsKey(component.getType())) {
      List<GithubRelease> releases = this.releaseCache.get(component.getType());
      if (releases.isEmpty()) {
        loadReleases(component);
      }
    }
    else {
      loadReleases(component);
    }

    return this.releaseCache.get(component.getType());
  }

  public ComponentFacade getComponentFacade(ComponentType type) {
    switch (type) {
      case vpinmame: {
        return vPinMAMEComponent;
      }
      case vpinball: {
        return vpxComponent;
      }
      case b2sbackglass: {
        return backglassComponent;
      }
      case flexdmd: {
        return flexDMDComponent;
      }
      case freezy: {
        return freezyComponent;
      }
      case serum: {
        return serumComponent;
      }
      case doflinx: {
        return dofLinxComponent;
      }
      case dof: {
        return dofComponent;
      }
      default: {
        throw new UnsupportedOperationException("Invalid component type " + type);
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ComponentType[] values = ComponentType.getValues();
    for (ComponentType value : values) {
      this.releaseCache.put(value, new ArrayList<>());
      Optional<Component> byName = componentRepository.findByType(value);
      if (byName.isEmpty()) {
        Component component = new Component();
        component.setType(value);
        component.setInstalledVersion("?");
        componentRepository.saveAndFlush(component);
      }
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
