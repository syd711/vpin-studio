package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.restclient.highscores.NVRamList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "nvrams")
public class NVRamResource {

  @Autowired
  private NVRamService nvRamService;

  @GetMapping
  public NVRamList getList() {
    return nvRamService.getResettedNVRams();
  }
}
