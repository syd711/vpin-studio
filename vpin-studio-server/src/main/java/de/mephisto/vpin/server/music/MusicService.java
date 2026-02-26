package de.mephisto.vpin.server.music;


import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import de.mephisto.vpin.server.vpx.MusicInstallationUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class MusicService {
  private final static Logger LOG = LoggerFactory.getLogger(MusicService.class);

  @Autowired
  private FolderLookupService folderLookupService;

  @Nullable
  public File getMusicFolder(Game game) {
    return folderLookupService.getMusicFolder(game);
  }

  public void installMusic(@NonNull File out, @NonNull Game game, @NonNull UploaderAnalysis analysis, @Nullable String rom, boolean acceptAllAudio) throws IOException {
    File musicFolder = folderLookupService.getMusicFolder(game);
    if (musicFolder.exists()) {
      MusicInstallationUtil.unpack(out, musicFolder, analysis, rom, analysis.getRelativeMusicPath(acceptAllAudio));
    }
    else {
      LOG.warn("Skipped installation of music bundle, no music folder {} found.", musicFolder.getAbsolutePath());
    }
  }

  public boolean delete(Game game) {
    File musicFolder = getMusicFolder(game);
    return FileUtils.deleteFolder(musicFolder);
  }
}
