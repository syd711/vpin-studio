package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface VpsTableDataChangedListener {
  void tableDataChanged(@NonNull Game game);
}
