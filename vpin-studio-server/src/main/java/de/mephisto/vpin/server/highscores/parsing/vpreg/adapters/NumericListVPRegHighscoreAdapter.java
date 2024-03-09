package de.mephisto.vpin.server.highscores.parsing.vpreg.adapters;

import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingEntry;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.apache.poi.poifs.filesystem.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class NumericListVPRegHighscoreAdapter extends VPRegHighscoreAdapterImpl {
  public static final String HIGH_SCORE = "HighScore";
  public static final String NAME_SUFFIX = "Name";

  @Override
  public boolean isApplicable(DirectoryEntry gameFolder) {
    if (gameFolder.hasEntry(HIGH_SCORE + "1") && gameFolder.hasEntry(HIGH_SCORE + "1" + NAME_SUFFIX)) {
      return true;
    }
    return false;
  }

  @Override
  public ScoreParsingSummary readHighscore(DirectoryEntry gameFolder) throws IOException {
    ScoreParsingSummary summary = new ScoreParsingSummary();
    int index = 1;
    String prefix = HIGH_SCORE;
    String nameSuffix = "Name";
    while (gameFolder.hasEntry(prefix + index) && gameFolder.hasEntry(prefix + index + nameSuffix)) {
      DocumentEntry nameEntry = (DocumentEntry) gameFolder.getEntry(prefix + index + nameSuffix);
      String nameString = super.getNameString(nameEntry);

      DocumentEntry scoreEntry = (DocumentEntry) gameFolder.getEntry(prefix + index);
      String scoreString = super.getScoreEntry(scoreEntry);

      ScoreParsingEntry score = new ScoreParsingEntry();
      score.setInitials(nameString);
      score.setScore(parseScoreString(scoreString));
      score.setPos(index);
      summary.getScores().add(score);
      index++;
    }
    return summary;
  }

  @Override
  public boolean resetHighscore(POIFSFileSystem fs, DirectoryEntry gameFolder) throws IOException {
    int index = 1;
    while (gameFolder.hasEntry(HIGH_SCORE + index) && gameFolder.hasEntry(HIGH_SCORE + index + NAME_SUFFIX)) {
      String scoreKey = HIGH_SCORE + index;
      DocumentNode scoreEntry = (DocumentNode) gameFolder.getEntry(scoreKey);
      POIFSDocument scoreDocument = new POIFSDocument(scoreEntry);
      scoreDocument.replaceContents(new ByteArrayInputStream(new Base64Encoder().decode(VPRegHighscoreAdapter.BASE64_ZERO_SCORE)));

      String nameKey = HIGH_SCORE + index + NAME_SUFFIX;
      DocumentNode nameEntry = (DocumentNode) gameFolder.getEntry(nameKey);
      POIFSDocument nameDocument = new POIFSDocument(nameEntry);
      nameDocument.replaceContents(new ByteArrayInputStream("".getBytes()));

      index++;

      fs.writeFilesystem();
    }
    return true;
  }
}
