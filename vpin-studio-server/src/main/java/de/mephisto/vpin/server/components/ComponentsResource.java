package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.facades.ComponentFacade;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "components")
public class ComponentsResource {
  private final static Logger LOG = LoggerFactory.getLogger(ComponentsResource.class);

  @Autowired
  private ComponentService componentService;

  @Autowired
  private PinUPConnector pinUPConnector;

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

  @PostMapping("/check/{type}/{artifact}")
  public ComponentActionLogRepresentation check(@PathVariable("type") ComponentType type, @PathVariable("artifact") String artifact) {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    ReleaseArtifactActionLog log = componentService.check(defaultGameEmulator, type, artifact);
    return toActionLog(log);
  }

  @PostMapping("/install/{type}/{artifact}")
  public ComponentActionLogRepresentation install(@PathVariable("type") ComponentType type, @PathVariable("artifact") String artifact) throws Exception {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    ReleaseArtifactActionLog log = componentService.install(defaultGameEmulator, type, artifact, false);
    return toActionLog(log);
  }

  @PostMapping("/simulate/{type}/{artifact}")
  public ComponentActionLogRepresentation simulate(@PathVariable("type") ComponentType type, @PathVariable("artifact") String artifact) throws Exception {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    ReleaseArtifactActionLog log = componentService.install(defaultGameEmulator, type, artifact, true);
    return toActionLog(log);
  }

  private ComponentRepresentation toComponentRepresentation(Component component) {
    ComponentType componentType = component.getType();

    ComponentFacade componentFacade = componentService.getComponentFacade(componentType);
    GithubRelease latestRelease = componentService.getLatestRelease(componentType);

    ComponentRepresentation representation = new ComponentRepresentation();
    representation.setType(componentType);
    representation.setUrl(latestRelease.getReleasesUrl());
    representation.setInstalledVersion(component.getInstalledVersion());
    representation.setLatestReleaseVersion(component.getLatestReleaseVersion());
    representation.setLastCheck(component.getLastCheck());
    representation.setArtifacts(latestRelease.getArtifacts().stream().map(a -> a.getName()).collect(Collectors.toList()));
    representation.setLastModified(componentFacade.getModificationDate(pinUPConnector.getDefaultGameEmulator()));

    if (representation.getLastModified() == null) {
      LOG.warn("Failed to resolve modification date for " + component);
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
