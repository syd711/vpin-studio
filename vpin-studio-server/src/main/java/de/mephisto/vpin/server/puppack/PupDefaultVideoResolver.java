package de.mephisto.vpin.server.puppack;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class PupDefaultVideoResolver {
  private final static Logger LOG = LoggerFactory.getLogger(PupDefaultVideoResolver.class);

  public static final String SCREENS_PUP = "screens.pup";
  public static final String TRIGGERS_PUP = "triggers.pup";

  private final File pupFolder;

  public PupDefaultVideoResolver(@NonNull File pupFolder) {
    this.pupFolder = pupFolder;
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
      File csvFile = new File(pupFolder, TRIGGERS_PUP);
      if (!csvFile.exists()) {
        return null;
      }

      in = new FileReader(csvFile);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      iterator.next();

      while(iterator.hasNext()) {
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
      LOG.error("Failed to resolve default trigger video for " + pupFolder.getAbsolutePath() + ": " + e.getMessage(), e);
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
      File csvFile = new File(pupFolder, SCREENS_PUP);
      if (!csvFile.exists()) {
        return null;
      }

      in = new FileReader(csvFile);
      Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
      Iterator<CSVRecord> iterator = records.iterator();
      iterator.next();

      while(iterator.hasNext()) {
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
      LOG.error("Failed to resolve default screen video for " + pupFolder.getAbsolutePath() + ": " + e.getMessage(), e);
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
      File video = new File(new File(pupFolder, playList), playFile);
      if (video.exists()) {
        return video;
      }
    }

    //search playlist if specified without default file
    if (!StringUtils.isEmpty(playList) && StringUtils.isEmpty(playFile)) {
      File playlistFolder = new File(pupFolder, playList);
      File[] videos = playlistFolder.listFiles((dir, name) -> name.endsWith(".mp4") || name.endsWith(".m4v"));
      if (videos != null && videos.length > 0) {
        return videos[0];
      }
    }
    return null;
  }
}
