package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.AltSound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "altsound")
public class AltSoundResource {

  @Autowired
  private AltSoundService altSoundService;

  @GetMapping("{id}")
  public AltSound csv(@PathVariable("id") int id) {
    return altSoundService.getAltSound(id);
  }

  @PostMapping("/save/{id}")
  public AltSound save(@PathVariable("id") int id, @RequestBody AltSound altSound) throws Exception {
    return altSoundService.save(id, altSound);
  }

  @GetMapping("/restore/{id}")
  public AltSound restore(@PathVariable("id") int id) {
    return altSoundService.restore(id);
  }
}
