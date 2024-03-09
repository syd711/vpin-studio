package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;

public interface VPRegHighscoreAdapter {
  String BASE64_ZERO_SCORE = "AAA=";

  boolean isApplicable(DirectoryEntry gameFolder) throws IOException;

  ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException;

  boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException;
}
