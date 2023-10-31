package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.ReleaseArtifactActionLog;
import de.mephisto.vpin.restclient.components.ComponentActionLogRepresentation;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "components")
public class ComponentsResource {

  @Autowired
  private ComponentService componentService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @GetMapping
  public List<ComponentRepresentation> getComponents() {
    return componentService.getComponents().stream().map(this::toComponentRepresentation).collect(Collectors.toList());
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

  @PostMapping("/check/{type}")
  public ComponentActionLogRepresentation check(@PathVariable("type") ComponentType type) {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    ReleaseArtifactActionLog log = componentService.check(defaultGameEmulator, type);
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

  private ComponentActionLogRepresentation toActionLog(ReleaseArtifactActionLog log) {
    ComponentActionLogRepresentation representation = new ComponentActionLogRepresentation();
    representation.setLogs(log.getLogs());
    representation.setStatus(log.getStatus());
    representation.setSimulated(log.isSimulated());
    representation.setDiff(log.isDiff());
    return representation;
  }

  private ComponentRepresentation toComponentRepresentation(Component component) {
    ComponentRepresentation representation = new ComponentRepresentation();
    representation.setType(component.getType());
    representation.setInstalledVersion(component.getInstalledVersion());
    representation.setLatestReleaseVersion(component.getLatestReleaseVersion());
    representation.setArtifacts(componentService.getLatestReleaseArtifacts(component.getType()).stream().map(a -> a.getName()).collect(Collectors.toList()));
    return representation;
  }
}
