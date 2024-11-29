package de.mephisto.vpin.server.highscores.parsing.listadapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// E.g. Transformers has a seperate highscore list for Autobots and Decepticons
// This adapter combines all scores into one list
public class SortedScoreAdapter implements ScoreListAdapter {

  private String name;

  public SortedScoreAdapter(String name) {
    this.name = name;
  }

  public SortedScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull Game game) {
    return game.getRom() != null && game.getRom().equals(name);
  }

  @NonNull
  public List<Score> getScores(@NonNull Game game, @NonNull Date createdAt, @NonNull List<String> lines) {
    List<HighScore> scores = new ArrayList<>();

    // Regex for scores with or without thousands seperator (e.g., "OPT 75.000.000", "OPT 30000", "#1 OPT 20.000", OPT 10,000,000)
    Pattern scorePattern = Pattern.compile("([A-Z]{3})\\s+(\\d+([.,]\\d{3})*)$");

    // Process each line
    for (String line : lines) {
      Matcher matcher = scorePattern.matcher(line);
      if (matcher.find()) {
        String player = matcher.group(1);
        String score = matcher.group(2);
        scores.add(new HighScore(player, score));
      }
    }

    // Sort scores in descending order
    scores.sort((a, b) -> Double.compare(b.getScoreValue(), a.getScoreValue()));
    List<Score> result = new ArrayList<>();
    int i = 1;
    for (HighScore score : scores) {
      result.add(new Score(createdAt, game.getId(), score.player, null, score.score, score.scoreValue, i));
    }
    return result;
  }

  // Only used in SortedScoreAdapter
  private class HighScore {
    private final String player;
    private final String score;
    private final double scoreValue;

    public HighScore(String player, String score) {
      this.player = player;
      this.score = score;
      String scoreToParse = ScoreFormatUtil.cleanScore(score);
      this.scoreValue = Double.parseDouble(scoreToParse);
    }

    public String getPlayer() {
      return player;
    }

    public String getScore() {
      return score;
    }

    public double getScoreValue() {
      return scoreValue;
    }
  }
}
