package de.mephisto.vpin.server.highscores.vpreg.adapters;

import de.mephisto.vpin.server.highscores.vpreg.VPRegScoreSummary;
import org.apache.poi.poifs.filesystem.DirectoryEntry;

import java.io.IOException;

public interface VPRegHighscoreAdapter {

  boolean isApplicable(DirectoryEntry gameFolder) throws IOException;

  VPRegScoreSummary readHighscore(DirectoryEntry gameFolder) throws IOException;

  boolean resetHighscore(DirectoryEntry gameFolder) throws IOException;
}
