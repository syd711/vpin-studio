package de.mephisto.vpin.server.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionScoreEntry;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreParser;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompetitionDataHelper {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionDataHelper.class);
  private final static String DATA_INDICATOR = "data:";

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Nullable
  public static String toDataString(@NonNull Competition competition, @NonNull Game game, @NonNull ScoreSummary summary, long messageId) {
    try {
      StringBuilder b = new StringBuilder();
      b.append("Competition Table: ");
      b.append(game.getGameDisplayName());
      b.append("\n\n");

      String tableName = game.getGameDisplayName();
      if (tableName.length() > 40) {
        tableName = tableName.substring(0, 40);
      }

      DiscordCompetitionData data = new DiscordCompetitionData();
      data.setName(competition.getName());
      data.setTname(tableName);
      data.setSdt(competition.getStartDate());
      data.setEdt(competition.getEndDate());
      data.setFs(game.getGameFileSize());
      data.setMsgId(messageId);
      data.setUuid(competition.getUuid());
      data.setRom(game.getRom());
      data.setOwner(competition.getOwner());

      String json = objectMapper.writeValueAsString(data);
      b.append(DATA_INDICATOR);
      b.append(new Base64Encoder().encode(json.getBytes(StandardCharsets.UTF_8)));
      return b.toString();
    } catch (JsonProcessingException e) {
      LOG.error("Failed to persist competition data: " + e.getMessage(), e);
    }
    return null;
  }

  public static String getName(String topic) {
    DiscordCompetitionData data = getCompetitionData(topic);
    if (data != null) {
      return data.getName();
    }
    return null;
  }

  public static long getStartMessageId(@Nullable String topic) {
    DiscordCompetitionData data = getCompetitionData(topic);
    if (data != null) {
      return data.getMsgId();
    }
    return -1;
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
      LOG.info("Failed to read competition data from '" + topic + "':" + e.getMessage());
    }
    return null;
  }

  public static DiscordCompetitionScoreEntry toScoreEntry(Score score) {
    DiscordCompetitionScoreEntry entry = new DiscordCompetitionScoreEntry();
    entry.setS(score.getScore());
    entry.setI(score.getPlayerInitials());
    entry.setP(score.getPosition());
    return entry;
  }
}
