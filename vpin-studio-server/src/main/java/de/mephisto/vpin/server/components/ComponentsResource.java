package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentInstallation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.components.GithubReleaseRepresentation;
import de.mephisto.vpin.server.components.facades.ComponentFacade;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "components")
public class ComponentsResource {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentsResource.class);

  @Autowired
  private ComponentService componentService;

  @GetMapping
  public List<ComponentRepresentation> getComponents() {
    return componentService.getComponents().stream().map(c -> toComponentRepresentation(c)).collect(Collectors.toList());
  }

  @GetMapping("/{type}")
  public ComponentRepresentation getComponent(@PathVariable("type") ComponentType type) {
    return toComponentRepresentation(componentService.getComponent(type));
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return componentService.clearCache();
  }

  @PutMapping("/setversion/{type}/{version}")
  public boolean setVersion(@PathVariable("type") ComponentType type, @PathVariable("version") String version) {
    return componentService.setVersion(type, version);
  }

  @PutMapping("/ignoreversion/{type}/{version}")
  public boolean ignoreVersion(@PathVariable("type") ComponentType type, @PathVariable("version") String version) {
    return componentService.ignoreVersion(type, version);
  }

  @PostMapping("/check/{forceDownload}")
  public ComponentActionLogRepresentation check(@RequestBody ComponentInstallation installation,
                                                @PathVariable("forceDownload") boolean forceDownload) {
    ReleaseArtifactActionLog log = componentService.check(installation.getComponent(), installation.getReleaseTag(), 
        installation.getArtifactName(), installation.getTargetFolder(), forceDownload);
    return toActionLog(log);
  }

  @PostMapping("/install")
  public ComponentActionLogRepresentation install(@RequestBody ComponentInstallation installation) {
    ReleaseArtifactActionLog log = componentService.install(installation.getComponent(), installation.getReleaseTag(), 
        installation.getArtifactName(), installation.getTargetFolder(), false);
    return toActionLog(log);
  }

  @PostMapping("/simulate")
  public ComponentActionLogRepresentation simulate(@RequestBody ComponentInstallation installation) {
    ReleaseArtifactActionLog log = componentService.install(installation.getComponent(), installation.getReleaseTag(), 
        installation.getArtifactName(), installation.getTargetFolder(), true);
    return toActionLog(log);
  }

  private ComponentRepresentation toComponentRepresentation(Component component) {
    ComponentType componentType = component.getType();

    ComponentFacade componentFacade = componentService.getComponentFacade(componentType);
    List<GithubRelease> releases = componentService.getReleases(componentType);
    if (!StringUtils.isEmpty(component.getIgnoredVersions())) {
      releases = releases.stream().filter(release -> !component.getIgnoredVersions().contains(release.getTag())).collect(Collectors.toList());
    }

    List<GithubReleaseRepresentation> artifacts = new ArrayList<>();
    releases.stream().forEach(release -> {
      GithubReleaseRepresentation rep = new GithubReleaseRepresentation();
      rep.setName(release.getName());
      rep.setReleaseNotes(release.getReleaseNotes());
      rep.setTag(release.getTag());
      rep.setReleasesUrl(release.getReleasesUrl());
      rep.setUrl(release.getUrl());
      rep.setArtifacts(release.getArtifacts().stream().map(artifact -> artifact.getName()).collect(Collectors.toList()));
      artifacts.add(rep);
    });


    ComponentRepresentation representation = new ComponentRepresentation();
    representation.setReleases(artifacts);
    representation.setUrl(componentFacade.getReleasesUrl());
    representation.setType(componentType);
    representation.setInstalledVersion(component.getInstalledVersion());
    representation.setLatestReleaseVersion(component.getLatestReleaseVersion());
    representation.setLastCheck(component.getLastCheck());
    representation.setExclusions(componentFacade.getExcludedFilenames());
    representation.setInstalled(componentFacade.isInstalled());

    try {
      representation.setLastModified(componentFacade.getModificationDate());
      File targetFolder = componentFacade.getTargetFolder();
      if (targetFolder != null) {
        representation.setTargetFolder(targetFolder.getAbsolutePath());
      }
      else {
        LOG.warn("No target folder resolved for {}", component);
      }
    }
    catch (Exception e) {
      LOG.error("Error returning component data for " + component + ": " + e.getMessage(), e);
    }
    return representation;
  }

  private ComponentActionLogRepresentation toActionLog(ReleaseArtifactActionLog log) {
    ComponentActionLogRepresentation representation = new ComponentActionLogRepresentation();
    representation.setLogsSummary(log.toLogString());
    representation.setStatus(log.getStatus());
    representation.setSimulated(log.isSimulated());
    representation.setDiff(log.isDiff());
    representation.setDiffSummary(log.toDiffString());
    representation.setSummary(log.getSummary());
    return representation;
  }
}
