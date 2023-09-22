package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.restclient.NVRamList;
import de.mephisto.vpin.restclient.mame.MameOptions;
import de.mephisto.vpin.server.mame.MameService;
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
