package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControls;
import de.mephisto.vpin.restclient.TableManagerSettings;
import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "popper")
public class PopperServiceResource {
  private final static Logger LOG = LoggerFactory.getLogger(PopperServiceResource.class);

  @Autowired
  private PopperService popperService;

  @GetMapping("/pincontrol/{screen}")
  public PinUPControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return popperService.getPinUPControlFor(PopperScreen.valueOf(screenName));
  }

  @GetMapping("/pincontrols")
  public PinUPControls getPinUPControls() {
    return popperService.getPinUPControls();
  }

  @GetMapping("/playlists")
  public List<Playlist> getPlaylists() {
    return popperService.getPlaylists();
  }

  @GetMapping("/running")
  public boolean isRunning() {
    return popperService.isPinUPRunning();
  }

  @PostMapping("/manager")
  public boolean saveArchiveManager(@RequestBody TableManagerSettings descriptor) {
    return popperService.saveArchiveManager(descriptor);
  }

  @GetMapping("/manager")
  public TableManagerSettings getArchiveManagerDescriptor() {
    return popperService.getArchiveManagerDescriptor();
  }

  @GetMapping("/terminate")
  public boolean terminate() {
    return popperService.terminate();
  }

  @GetMapping("/restart")
  public boolean restart() {
    return popperService.restart();
  }
}
