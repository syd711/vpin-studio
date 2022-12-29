package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
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

  @GetMapping("/playlists")
  public List<Playlist> getPlaylists() {
    return popperService.getPlaylists();
  }
}
