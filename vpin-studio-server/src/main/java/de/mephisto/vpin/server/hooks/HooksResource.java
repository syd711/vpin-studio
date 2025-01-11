package de.mephisto.vpin.server.hooks;

import de.mephisto.vpin.restclient.hooks.HookCommand;
import de.mephisto.vpin.restclient.hooks.HookList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "hooks")
public class HooksResource {

  @Autowired
  private HooksService hooksService;

  @PostMapping
  public HookCommand execute(@RequestBody HookCommand cmd) throws Exception {
    return hooksService.execute(cmd);
  }

  @GetMapping
  public HookList getHooks() {
    return hooksService.getHooks();
  }
}
