package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.InstallLog;
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

  @PostMapping("/install/{type}/{artifact}")
  public InstallLog install(@PathVariable("type") ComponentType type, @PathVariable("artifact") String artifact) {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    return componentService.install(defaultGameEmulator, type, artifact, false);
  }

  @PostMapping("/simulate/{type}/{artifact}")
  public InstallLog simulate(@PathVariable("type") ComponentType type, @PathVariable("artifact") String artifact) {
    GameEmulator defaultGameEmulator = pinUPConnector.getDefaultGameEmulator();
    return componentService.install(defaultGameEmulator, type, artifact, true);
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
