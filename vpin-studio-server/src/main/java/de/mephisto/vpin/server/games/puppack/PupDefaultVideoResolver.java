package de.mephisto.vpin.server.games.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

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
      video = findVideoFromTriggers();
    }

    return video;
  }

  @Nullable
  private File findVideoFromTriggers() {
    Reader in = null;
    try {
      File csvFile = pupPack.getTriggersPup();
      if (!csvFile.exists()) {
        return null;
      }

      in = new FileReader(csvFile);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      iterator.next();

      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();
        TriggerEntry entry = new TriggerEntry(record);
        if (entry.getScreenNum() == 2) {
          String playList = entry.getPlayList();
          String playFile = entry.getPlayFile();
          File video = resolveFile(playList, playFile);
          if (video != null) {
            return video;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to resolve default trigger video for " + pupPack + ": " + e.getMessage(), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          //ignore
        }
      }
    }
    return null;
  }

  @Nullable
  private File findVideoFromScreens() {
    Reader in = null;
    try {
      File csvFile = pupPack.getScreensPup();
      if (!csvFile.exists()) {
        return null;
      }

      in = new FileReader(csvFile);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      iterator.next();

      while (iterator.hasNext()) {
        CSVRecord record = iterator.next();
        ScreenEntry entry = new ScreenEntry(record);
        String playList = entry.getPlayList();
        String playFile = entry.getPlayFile();
        File video = resolveFile(playList, playFile);
        if (video != null) {
          return video;
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to resolve default screen video for " + pupPack + ": " + e.getMessage(), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          //ignore
        }
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
