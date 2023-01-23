package de.mephisto.vpin.server.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompetitionDataHelper {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionDataHelper.class);
  private final static String DATA_INDICATOR = "data:";

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @NonNull
  public static String toDataString(@NonNull Competition competition, @NonNull Game game, @NonNull ScoreSummary summary, @NonNull String messageId) {
    try {
      StringBuilder b = new StringBuilder();
      b.append("Active Competition Table: '");
      b.append(game.getGameDisplayName());
      b.append("'");
      b.append("\n\n");

      DiscordCompetitionData data = new DiscordCompetitionData();
      data.setName(competition.getName());
      data.setCreatedAt(new Date());
      data.setFileSize(game.getGameFileSize());
      data.setStartMessageId(messageId);
      data.setUuid(competition.getUuid());
      data.setRom(game.getRom());
      data.setOwner(competition.getOwner());

      List<Score> scores = summary.getScores();
      for (Score score : scores) {
        data.getScores().add(new DiscordCompetitionData.ScoreEntry(score));
      }

      String json = objectMapper.writeValueAsString(data);
      b.append(DATA_INDICATOR);
      b.append(new Base64Encoder().encode(json.getBytes(StandardCharsets.UTF_8)));
      return b.toString();
    } catch (JsonProcessingException e) {
      LOG.error("Failed to persist competition data: " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public static UUID getUuid(@Nullable String topic) {
    DiscordCompetitionData topicData = getCompetitionData(topic);
    if (topicData != null) {
      return UUID.fromString(topicData.getUuid());
    }
    return null;
  }

  @Nullable
  public static ScoreSummary getScores(@NonNull DiscordService discordService, @Nullable String topic) {
    DiscordCompetitionData topicData = getCompetitionData(topic);
    if (topicData != null) {
      List<Score> scores = new ArrayList<>();
      ScoreSummary summary = new ScoreSummary(scores, topicData.getCreatedAt());
      List<DiscordCompetitionData.ScoreEntry> scoresEntries = topicData.getScores();
      for (DiscordCompetitionData.ScoreEntry scoresEntry : scoresEntries) {
        Player player = null;
        Optional<Player> playerForInitials = discordService.getPlayerByInitials(scoresEntry.getInitials());
        if (playerForInitials.isPresent()) {
          player = playerForInitials.get();
        }
        Score score = new Score(topicData.getCreatedAt(), -1, scoresEntry.getInitials(), player, scoresEntry.getScore(), scoresEntry.getNumericScore(), scoresEntry.getPosition(), null);
        scores.add(score);
      }
      return summary;
    }
    return null;
  }

  @Nullable
  public static DiscordCompetitionData getCompetitionData(@Nullable String topic) {
    try {
      if (topic == null) {
        return null;
      }
      if (topic.contains(DATA_INDICATOR)) {
        String dataBase64 = topic.substring(topic.indexOf(DATA_INDICATOR) + DATA_INDICATOR.length()).trim();
        String data = new String(new Base64Encoder().decode(dataBase64));
        return objectMapper.readValue(data, DiscordCompetitionData.class);
      }
      return null;
    } catch (JsonProcessingException e) {
      LOG.error("Failed to read competition data: " + e.getMessage(), e);
    }
    return null;
  }
}
