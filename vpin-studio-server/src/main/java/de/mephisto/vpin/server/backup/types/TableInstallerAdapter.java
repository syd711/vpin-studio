package de.mephisto.vpin.server.backup.types;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface TableInstallerAdapter extends Job {

  @Nullable
  Game installTable();
}
