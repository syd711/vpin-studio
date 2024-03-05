package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.DirectoryEntry;

import java.io.IOException;

public interface VPRegHighscoreAdapter {

  boolean isApplicable(DirectoryEntry gameFolder) throws IOException;

  ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException;

  boolean resetHighscore(DirectoryEntry gameFolder) throws IOException;
}
