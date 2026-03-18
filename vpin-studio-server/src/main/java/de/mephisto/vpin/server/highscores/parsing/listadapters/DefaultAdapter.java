package de.mephisto.vpin.server.highscores.parsing.listadapters;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAdapter extends ScoreListAdapterBase implements ScoreListAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultAdapter.class);

  @Override
  public boolean isApplicable(@NonNull Game game) {
    return true;
  }

  @Override
  @NonNull
  public List<Score> getScores(@Nullable Game game, @NonNull Date createdAt, @NonNull List<String> lines, List<String> titles, boolean parseAll) {
    try {
      List<Score> scores = new ArrayList<>();

      int gameId = -1;
      String source = null;
      if (game != null) {
        gameId = game.getId();
        source = game.getGameDisplayName() + "/" + game.getRom() + "/" + game.getHsFileName();
      }

	    String currentTitle = null;
      String currentSuffix = null;
      Score currentScore = null;
      for (int i = 0; i < lines.size(); i++) {
        String line = lines.get(i).trim();
      	if (StringUtils.isEmpty(line)) {
          if (currentSuffix != null && currentScore != null) {
            currentScore.setSuffix(currentSuffix);
          }
        	// restart a possible new sequence
        	currentTitle = null;
          currentSuffix = null;
          currentScore = null;
        	if (scores.size() >= 3 && !parseAll) {
            	break;
        	}
          continue;
      	}

        if (isScoreLine(line)) {
          currentScore = createScore(createdAt, currentTitle, line, source, gameId);
          if (currentScore != null) {
            scores.add(currentScore);
          }
        }
        else if (isTitleScoreLine(line)) {
          if (parseAll || titles.contains(currentTitle)) {
            currentScore = createTitledScore(createdAt, currentTitle, line, source, gameId);
            if (currentScore != null) {
              scores.add(currentScore);
            }
          }
        }
        else if (StringUtils.isNotEmpty(line)) {
          if (currentScore != null) {
            currentSuffix = " " + line;
          }
          currentTitle = line;
        }
      }
      if (currentSuffix != null && currentScore != null) {
        currentScore.setSuffix(currentSuffix);
      }

      return filterDuplicates(scores);
    }
    catch (Exception e) {
      if (game != null) {
        LOG.error("Score parsing failed for \"" + game.getGameDisplayName() + "\": {}", e.getMessage(), e);
      }
      else {
        LOG.error("Score parsing failed: {}", e.getMessage(), e);
      }
      throw e;
    }
  }

  //-------------------------

  private static final String _patternIndex = "(\\d+\\)|#\\d+|\\d+#|\\d+\\.:) +";
  private static final String _patternScore = "(.{3})?(\\s+-)?(\\s+(\\d\\d?\\d?(?:[., ?\u00a0\u202f\ufffd\u00ff]?\\d\\d\\d)*(\\.\\d)?)((\\s?[a-zA-Z]+)*))+$";

  private static final Pattern patternScoreLine = Pattern.compile(_patternIndex + _patternScore);
  private static final Pattern patternScoreTitle = Pattern.compile(_patternScore);


  public boolean isTitleScoreLine(String line) {
    Matcher m = patternScoreTitle.matcher(line);
    return m.find();
  }

  public boolean isScoreLine(String line) {
    Matcher m = patternScoreLine.matcher(line);
    return m.find();
  }

  /**
   * Parses score that are shown right behind a possible title.
   * These scores do not have a leading position number.
   */
  @Nullable
  protected Score createTitledScore(@NonNull Date createdAt, @Nullable String title, @NonNull String line, @Nullable String source, int gameId) {
    Matcher m = patternScoreTitle.matcher(line);
    if (m.find()) {
      String initials = m.group(1);
      if (StringUtils.isEmpty(initials)) {
        initials = "???";
      }

      String scoreString = m.group(4).trim();
      long scoreValue = toNumericScore(scoreString, source, false);
      if (scoreValue != -1) {
        Score sc = new Score(createdAt, gameId, initials.trim(), null, scoreString, scoreValue, 1);
        sc.setLabel(title);

        // do not trim and keep spaces at beginning if present
        String suffix = m.group(6);
        if (StringUtils.isNotEmpty(suffix)) {
          sc.setSuffix(suffix);
        }
        return sc;
      }
    }
    return null;
  }

  @Nullable
  public Score createScore(@NonNull Date createdAt, @Nullable String title, @NonNull String line, @Nullable String source, int gameId) {
    String idx = StringUtils.substringBefore(line, " ");
    idx = idx.replace(")", "");
    idx = idx.replace("#", "");
    idx = idx.replace(".:", "");
    int index = Integer.parseInt(idx);
    
    line = StringUtils.substringAfter(line, " ");
    Score sc = createTitledScore(createdAt, title, line, source, gameId);
    sc.setPosition(index);
    return sc;
  }
}