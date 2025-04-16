package de.mephisto.vpin.server.highscores.parsing.ini.adapters;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * [HighScores]
 * score_1_label=GRAND CHAMPION
 * score_1_name=P2O
 * score_1_value=152850
 * score_2_label=HIGH SCORE 1
 * score_2_name=LOO
 * score_2_value=148540
 * score_3_label=HIGH SCORE 2
 * score_3_name=DAD
 * score_3_value=116530
 * score_4_label=HIGH SCORE 3
 * score_4_name=AAA
 * score_4_value=21920
 * [MachineVars]
 * won_game=0
 * message_num=0
 * player1_score=0
 * player2_score=0
 * player3_score=0
 * player4_score=0
 */
public class DefaultIniHighscoreFileAdapter implements IniScoreFileAdapter {

  @Override
  public boolean isApplicable(@NonNull File file, @NonNull List<String> lines) {
    for (String line : lines) {
      if (String.valueOf(line).contains("score_1_value")) {
        return true;
      }
    }

    return false;
  }

  @Override
  public String convert(@NonNull File file, @NonNull List<String> lines) {
    Map<String, String> records = new LinkedHashMap<>();
    lines.stream().filter(l -> l.startsWith("score_")).forEach(l -> {
      String[] split = l.split("=");
      records.put(split[0].trim(), split[1].trim());
    });


    StringBuilder builder = new StringBuilder("HIGHEST SCORES\n");
    Set<Map.Entry<String, String>> entries = records.entrySet();
    int index = 1;
    for (Map.Entry<String, String> entry : entries) {
      String keyName = "score_" + index + "_name";
      String keyValue = "score_" + index + "_value";
      if (!records.containsKey(keyName)) {
        break;
      }

      builder.append("#");
      builder.append(index);
      builder.append(" ");
      builder.append(records.get(keyName));
      builder.append("   ");
      builder.append(records.get(keyValue));
      builder.append("\n");
      index++;
    }

    return builder.toString();
  }

  @Override
  public List<String> resetHighscore(@NonNull File file, @NonNull List<String> lines, long score) {
    List<String> newScoreText = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith("score_")) {
        String[] split = line.split("=");
        String key = split[0].trim();
        if (key.endsWith("_name")) {
          line = key + "=???";
        }
        else if (key.endsWith("_value")) {
          line = key + "=0";
        }
      }
      else if (line.startsWith("player") && line.contains("=")) {
        String[] split = line.split("=");
        String key = split[0].trim();

        line = key + "=0";
      }
      newScoreText.add(line);
    }

    return newScoreText;
  }
}
