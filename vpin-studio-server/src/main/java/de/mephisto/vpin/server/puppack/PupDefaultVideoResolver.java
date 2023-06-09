package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class PupDefaultVideoResolver {
  private final static Logger LOG = LoggerFactory.getLogger(PupDefaultVideoResolver.class);

  private final PupPack pupPack;

  public PupDefaultVideoResolver(@NonNull PupPack pupPack) {
    this.pupPack = pupPack;
  }

  @Nullable
  public File findDefaultVideo() {
    File video = findVideoFromScreens();
    if (video == null) {
      List<TriggerEntry> entries = pupPack.getTriggersPup().getEntries();
      for (TriggerEntry entry : entries) {
        if (entry.getScreenNum() == 2) {
          String playList = entry.getPlayList();
          String playFile = entry.getPlayFile();
          video = resolveFile(playList, playFile);
          break;
        }
      }
    }
    return video;
  }

  @Nullable
  private File findVideoFromScreens() {
    List<ScreenEntry> entries = pupPack.getScreensPup().getEntries();
    for (ScreenEntry entry : entries) {

      String playList = entry.getPlayList();
      String playFile = entry.getPlayFile();
      File video = resolveFile(playList, playFile);
      if (video != null) {
        return video;
      }
    }
    return null;
  }

  @Nullable
  private File resolveFile(@Nullable String playList, @Nullable String playFile) {
    //try to find default file for any screen
    if (!StringUtils.isEmpty(playList) && !StringUtils.isEmpty(playFile)) {
      File video = new File(new File(pupPack.getPupPackFolder(), playList), playFile);
      if (video.exists()) {
        return video;
      }
    }

    //search playlist if specified without default file
    if (!StringUtils.isEmpty(playList) && StringUtils.isEmpty(playFile)) {
      File playlistFolder = new File(pupPack.getPupPackFolder(), playList);
      File[] videos = playlistFolder.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".m4v") || name.endsWith(".mov"));
      if (videos != null && videos.length > 0) {
        return videos[0];
      }
    }
    return null;
  }
}
