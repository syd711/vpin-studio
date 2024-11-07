package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FPService {

  @Autowired
  private FPCommandLineService fpCommandLineService;

  @Autowired
  private FrontendService frontendService;

  public boolean play(@Nullable Game game, @Nullable String altExe) {
    frontendService.killFrontend();

    if (game != null) {
      return fpCommandLineService.execute(game, altExe);
    }

    return fpCommandLineService.launch();
  }
}
