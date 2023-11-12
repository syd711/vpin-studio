package de.mephisto.vpin.server.components.facades;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public interface ComponentFacade {
  @NonNull
  String[] getDiffList();

  @NonNull
  String getReleasesUrl();

  GithubRelease loadRelease() throws IOException;

  @NonNull
  File getTargetFolder(@NonNull GameEmulator gameEmulator);

  @Nullable
  Date getModificationDate(@NonNull GameEmulator gameEmulator);
}
