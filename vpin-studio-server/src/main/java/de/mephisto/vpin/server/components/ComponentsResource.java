package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.InstallLog;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.games.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "components")
public class ComponentsResource {

  @Autowired
  private ComponentService componentService;

  @GetMapping
  public List<Component> getComponents() {
    return componentService.getComponents();
  }

  @GetMapping("/component/{type}")
  public Component getComponent(@PathVariable("type") ComponentType type) {
    return componentService.getComponent(type);
  }

  @PutMapping("/setversion/{type}/{version}")
  public boolean setVersion(@PathVariable("type") ComponentType type, @PathVariable("version") String version) {
    return componentService.setVersion(type, version);
  }

  @PostMapping("/install/{type}")
  public InstallLog install(@PathVariable("type") ComponentType type) {
    return componentService.install(type, false);
  }

  @PostMapping("/simulate/{type}")
  public InstallLog simulate(@PathVariable("type") ComponentType type) {
    return componentService.install(type, true);
  }
}
