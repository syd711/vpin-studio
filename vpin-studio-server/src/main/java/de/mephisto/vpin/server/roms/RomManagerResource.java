package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roms")
public class RomManagerResource {

  @Autowired
  private RomService romService;

  @SuppressWarnings("unused")
  @Nullable
  public String rescanRom(Game game) {
    return this.romService.scanRom(game);
  }

}
