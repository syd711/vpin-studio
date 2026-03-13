package de.mephisto.vpin.server.highscores.parsing.listadapters;

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
public class SortedScoreAdapter extends ScoreListAdapterBase implements ScoreListAdapter {

  private String name;

  // Regex for scores with or without thousands seperator
  // Could start with #1<space>, 1#<space> or 1)<space>
  // Starts with or is followed by 1 to 3 characters or spaces => player initials
  // Followed by one or more spaces
  // Followed by decimals which might include dots and comma's => score
  // Followed by an <eol> (so no more characters)
  static Pattern scorePattern = Pattern.compile("(?:^|#\\d+ |\\d+# |\\d+\\) )([\\S ]{1,3})\\s+(\\d+([.,?\u00a0\u202f\ufffd\u00ff]\\d{3})*)$");

  public SortedScoreAdapter(String name) {
    this.name = name;
  }

  @Override
  public boolean isApplicable(@NonNull Game game) {
    return game != null && game.getRom() != null && game.getRom().equals(name);
  }

  @NonNull
  public List<Score> getScores(@NonNull Game game, @NonNull Date createdAt, @NonNull List<String> lines, @NonNull List<String> titles) {
    List<Score> scores = new ArrayList<>();

    String source = game.getGameDisplayName() + "/" + game.getRom() + "/" + game.getHsFileName();

    // Process each line
    for (String line : lines) {
      Matcher matcher = scorePattern.matcher(line);
      if (matcher.find()) {
        String player = matcher.group(1);
        String score = matcher.group(2);
        long scoreValue = toNumericScore(score, source, true);
        if (scoreValue != -1) {
          scores.add(new Score(createdAt, game.getId(), player, null, score, scoreValue, 0));
        }
      }
    }

    // remove duplicates
    scores = filterDuplicates(scores);

    // Sort scores in descending order
    scores.sort((a, b) -> Long.compare(b.getScore(), a.getScore()));

    int i = 1;
    for (Score score : scores) {
      score.setPosition(i);
      i++;
    }

    return scores;
  }
}
