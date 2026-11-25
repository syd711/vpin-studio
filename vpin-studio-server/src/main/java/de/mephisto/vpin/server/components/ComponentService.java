package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.*;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ComponentService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentService.class);

  @Autowired
  private ComponentRepository componentRepository;

  @Autowired
  private SystemService systemService;

  @Autowired
  private EmulatorService emulatorService;

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

  @Autowired
  private ComponentArchiveCache archiveCache;

  private final Map<ComponentType, List<GithubRelease>> releaseCache = new ConcurrentHashMap<>();

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
      LOG.info("Applied version {} for {}", version, type.name());
      return true;
    }
    return false;
  }

  public boolean setTargetFolder(@NonNull ComponentType type, @Nullable String targetFolder) {
    Optional<Component> byType = componentRepository.findByType(type);
    if (byType.isPresent()) {
      Component component = byType.get();
      component.setTargetFolder(StringUtils.isEmpty(targetFolder) ? null : targetFolder);
      componentRepository.saveAndFlush(component);
      LOG.info("Set target folder override for {}: {}", type, targetFolder);
      return true;
    }
    return false;
  }

  /**
   * The folder to use for this component: the user-configured override when set, else the auto-detected one.
   */
  @Nullable
  public File getEffectiveTargetFolder(@NonNull ComponentType type) {
    Component component = getComponent(type);
    if (!StringUtils.isEmpty(component.getTargetFolder())) {
      return new File(component.getTargetFolder());
    }
    return getComponentFacade(type).getTargetFolder();
  }

  /**
   * Components like VPX can be installed into other folders than the component folder, e.g. for multiple VPX emulators.
   */
  private boolean isEffectiveTargetFolder(@NonNull ComponentType type, @NonNull File folder) {
    File effective = getEffectiveTargetFolder(type);
    if (effective == null) {
      return true;
    }
    return effective.getAbsoluteFile().toPath().normalize().equals(folder.getAbsoluteFile().toPath().normalize());
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
      LOG.info("Ignored version {} for {}", version, type.name());
      loadReleases(component);
      return true;
    }
    return false;
  }

  /**
   * Target folder value for checks that only refresh the release information, without comparing an installation.
   */
  public static final String ANY_TARGET_FOLDER = "-any-";

  /**
   * Compares the given release artifact with the installation in the target folder.
   *
   * @param refresh reloads the release information from GitHub first. The downloaded archives are cached per
   *                release, so they are not downloaded again.
   */
  public ReleaseArtifactActionLog check(@NonNull ComponentType type, @NonNull String tag, @NonNull String artifact,
                                        String targetDirectory, boolean refresh) {
    Component component = getComponent(type);
    ComponentFacade componentFacade = getComponentFacade(type);
    if (targetDirectory == null) {
      return new ReleaseArtifactActionLog(false, true);
    }

    // release check only, e.g. "check for updates" of the overview
    if (ANY_TARGET_FOLDER.equals(targetDirectory)) {
      if (refresh || StringUtils.isEmpty(component.getLatestReleaseVersion())) {
        loadReleases(component);
        component.setLastCheck(Instant.now());
        componentRepository.saveAndFlush(component);
      }
      return new ReleaseArtifactActionLog(false, true);
    }

    boolean reloadAllowed = true;
    if (refresh) {
      loadReleases(component);
      // a failed refresh (e.g. offline) must not be retried within the same check
      reloadAllowed = false;
    }

    GithubRelease githubRelease = findRelease(component, tag, reloadAllowed);
    if (githubRelease == null) {
      ReleaseArtifactActionLog log = new ReleaseArtifactActionLog(false, true);
      log.setStatus("No release information available for " + type + ", the GitHub release page could not be loaded.");
      return log;
    }

    ReleaseArtifact releaseArtifact = getReleaseArtifact(artifact, githubRelease);
    if (releaseArtifact == null) {
      throw new UnsupportedOperationException(type + " release artifact \"" + artifact + "\" not found for " + githubRelease.getTag() + ".");
    }

    File targetFolder = new File(targetDirectory);
    ReleaseArtifactActionLog install = releaseArtifact.diff(archiveCache.supplier(type, githubRelease, releaseArtifact), targetFolder,
        componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames(), componentFacade.getDiffList());
    if (install.getStatus() != null) {
      archiveCache.invalidate(type, githubRelease, releaseArtifact);
    }
    else if (!install.isDiffering() && isEffectiveTargetFolder(type, targetFolder)) {
      component.setInstalledVersion(githubRelease.getTag());
      LOG.info("Applied current version \"{}\" for {}", githubRelease.getTag(), component.getType());
    }

    component.setLastCheck(Instant.now());
    componentRepository.saveAndFlush(component);
    return install;
  }

  /**
   * The release with the given tag, "-latest-" or an unknown tag resolve to the latest release.
   * An unknown tag reloads the releases once, the tag may have been published after the last check.
   */
  @Nullable
  private GithubRelease findRelease(@NonNull Component component, @NonNull String tag, boolean reloadAllowed) {
    List<GithubRelease> githubReleases = reloadAllowed ? getCached(component) : releaseCache.getOrDefault(component.getType(), List.of());
    Optional<GithubRelease> match = githubReleases.stream().filter(r -> r.getTag().equals(tag)).findFirst();
    if (match.isEmpty() && !"-latest-".equals(tag) && reloadAllowed) {
      loadReleases(component);
      githubReleases = releaseCache.getOrDefault(component.getType(), List.of());
      match = githubReleases.stream().filter(r -> r.getTag().equals(tag)).findFirst();
    }
    if (match.isPresent()) {
      return match.get();
    }
    return githubReleases.isEmpty() ? null : githubReleases.getFirst();
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
    if (first.isEmpty()) {
      throw new UnsupportedOperationException(type + " release for tag \"" + tag + "\" not found.");
    }

    GithubRelease githubRelease = first.get();
    ReleaseArtifact releaseArtifact = githubRelease.getArtifacts().stream().filter(a -> a.getName().equals(artifact)).findFirst().orElse(null);
    if (releaseArtifact == null) {
      throw new UnsupportedOperationException(type + " release artifact \"" + artifact + "\" not found for " + tag + ".");
    }

    ComponentFacade componentFacade = getComponentFacade(type);
    File targetFolder = new File(targetDirectory);
    ReleaseArtifact.ArchiveSupplier archive = archiveCache.supplier(type, githubRelease, releaseArtifact);
    if (simulate) {
      ReleaseArtifactActionLog log = releaseArtifact.simulateInstall(archive, targetFolder, componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames());
      if (log.getStatus() != null) {
        archiveCache.invalidate(type, githubRelease, releaseArtifact);
      }
      return log;
    }

    // download before stopping running processes, so they are not stopped during the download
    ReleaseArtifactActionLog download = new ReleaseArtifactActionLog(false, false);
    File archiveFile;
    try {
      archiveFile = archive.get(download);
    }
    catch (Exception e) {
      LOG.error("Failed to download {} for {}: {}", artifact, type, e.getMessage(), e);
      if (download.getStatus() == null) {
        download.setStatus(e.getMessage());
      }
      return download;
    }

    if (type.equals(ComponentType.b2sbackglass)) {
      systemService.killProcesses("DFOLinx");
    }

    componentFacade.preProcess(releaseArtifact, download);

    //we have a real installation from here on
    ReleaseArtifactActionLog install = releaseArtifact.install(log -> {
      log.getLogs().addAll(download.getLogs());
      return archiveFile;
    }, targetFolder, componentFacade.getRootFolderInArchiveIndicators(), componentFacade.getExcludedFilenames(), componentFacade.getIncludedFilenames());
    if (install.getStatus() == null) {
      //unzipping was successful, the version is only tracked for the component folder, not for other VPX emulators
      if (isEffectiveTargetFolder(type, targetFolder)) {
        component.setInstalledVersion(githubRelease.getTag());
        componentRepository.saveAndFlush(component);
      }
      if (type.equals(ComponentType.vpinball)) {
        emulatorService.refreshVpxVersions();
      }

      //execute optional post processing
      componentFacade.postProcess(releaseArtifact, install);
    }
    else {
      archiveCache.invalidate(type, githubRelease, releaseArtifact);
    }

    return install;
  }

  private void loadReleases(Component component) {
    try {
      ComponentFacade componentFacade = getComponentFacade(component.getType());
      List<GithubRelease> githubReleases = componentFacade.loadReleases();
      if (!StringUtils.isEmpty(component.getIgnoredVersions())) {
        githubReleases = githubReleases.stream().filter(release -> !component.getIgnoredVersions().contains(release.getTag())).collect(Collectors.toList());
      }
      if (!githubReleases.isEmpty()) {
        component.setLatestReleaseVersion(githubReleases.getFirst().getTag());
      }
      componentRepository.saveAndFlush(component);
      this.releaseCache.put(component.getType(), githubReleases);
    }
    catch (UnknownHostException | ConnectException | SocketTimeoutException e) {
      LOG.warn("Failed to load releases for {}, GitHub is not reachable: {}", component, e.toString());
    }
    catch (IOException e) {
      LOG.error("Failed to initialize release for {}: {}", component, e.getMessage(), e);
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

    return this.releaseCache.getOrDefault(component.getType(), List.of());
  }

  public ComponentFacade getComponentFacade(ComponentType type) {
    return switch (type) {
      case vpinmame -> vPinMAMEComponent;
      case vpinball -> vpxComponent;
      case b2sbackglass -> backglassComponent;
      case flexdmd -> flexDMDComponent;
      case freezy -> freezyComponent;
      case serum -> serumComponent;
      case doflinx -> dofLinxComponent;
      case dof -> dofComponent;
      default -> throw new UnsupportedOperationException("Invalid component type " + type);
    };
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
