package de.mephisto.vpin.server.fp;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FPService {

  @Autowired
  private FPCommandLineService fpCommandLineService;

  public boolean play(@Nullable Game game, @Nullable String altExe) {

    if (game != null) {
      return fpCommandLineService.execute(game, altExe);
    }

    return fpCommandLineService.launch();
  }
}
