package de.mephisto.vpin.server.highscores.parsing.nvram.adapters;

import de.mephisto.vpin.restclient.util.ScoreFormatUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.*;
import java.util.regex.*;

// E.g. Transformers has a seperate highscore list for Autobots and Decepticons
// This adapter combines all scores into one list
public class SortedScoreAdapter implements ScoreNvRamAdapter {

  private String name;

  public SortedScoreAdapter(String name) {
    this.name = name;
  }

  public SortedScoreAdapter() {

  }

  @Override
  public boolean isApplicable(@NonNull String nvRam, @NonNull List<String> lines) {
    return nvRam.equals(name);
  }

  @Override
  public String convert(@NonNull String nvRam, @NonNull List<String> lines) {
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
    scores.sort((a, b) -> Integer.compare(b.getScoreValue(), a.getScoreValue()));

    StringBuilder converted = new StringBuilder();
    int i = 1;
    for (HighScore score : scores) {
      converted.append(String.format("#%d %s     %s%n", i++, score.getPlayer(), score.getScore()));
    }

    return converted.toString();
  }

  // Only used in SortedScoreAdapter
  private class HighScore {
    private final String player;
    private final String score;
    private final int scoreValue;

    public HighScore(String player, String score) {
      this.player = player;
      this.score = score;
      String scoreToParse = ScoreFormatUtil.cleanScore(score);
      this.scoreValue = Integer.parseInt(scoreToParse);
    }

    public String getPlayer() {
      return player;
    }

    public String getScore() {
      return score;
    }

    public int getScoreValue() {
      return scoreValue;
    }

  }
}
