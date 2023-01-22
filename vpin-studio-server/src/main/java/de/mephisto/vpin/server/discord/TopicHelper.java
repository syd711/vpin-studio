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
import de.mephisto.vpin.server.players.PlayerService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class TopicHelper {
  private final static Logger LOG = LoggerFactory.getLogger(TopicHelper.class);
  private final static String DATA_INDICATOR = "data:";

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @NonNull
  public static String toTopic(@NonNull Competition competition, @NonNull Game game, @NonNull ScoreSummary summary) {
    try {
      StringBuilder b = new StringBuilder();
      b.append("Active Competition Table: '");
      b.append(game.getGameDisplayName());
      b.append("'");
      b.append("\n\n");

      TopicData data = new TopicData();
      data.setName(competition.getName());
      data.setCreatedAt(new Date());
      data.setFileSize(game.getGameFileSize());
      data.setUuid(competition.getUuid());
      data.setRom(game.getRom());
      data.setOwner(competition.getOwner());

      List<Score> scores = summary.getScores();
      for (Score score : scores) {
        data.getScores().add(new ScoreEntry(score));
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
    TopicData topicData = getTopicData(topic);
    if (topicData != null) {
      return UUID.fromString(topicData.getUuid());
    }
    return null;
  }


  @Nullable
  public static ScoreSummary getScores(@NonNull DiscordService discordService, @Nullable String topic) {
    TopicData topicData = getTopicData(topic);
    if (topicData != null) {
      List<Score> scores = new ArrayList<>();
      ScoreSummary summary = new ScoreSummary(scores, topicData.getCreatedAt());
      List<ScoreEntry> scoresEntries = topicData.getScores();
      for (ScoreEntry scoresEntry : scoresEntries) {
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
  private static TopicData getTopicData(@Nullable String topic) {
    try {
      if (topic == null) {
        return null;
      }
      if (topic.contains(DATA_INDICATOR)) {
        String dataBase64 = topic.substring(topic.indexOf(DATA_INDICATOR) + DATA_INDICATOR.length()).trim();
        String data = new String(new Base64Encoder().decode(dataBase64));
        return objectMapper.readValue(data, TopicData.class);
      }
      return null;
    } catch (JsonProcessingException e) {
      LOG.error("Failed to read competition data: " + e.getMessage(), e);
    }
    return null;
  }

  static class TopicData {
    private String uuid;
    private String owner;
    private String rom;
    private long fileSize;
    private Date createdAt;
    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    private List<ScoreEntry> scores = new ArrayList<>();

    public Date getCreatedAt() {
      return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
      this.createdAt = createdAt;
    }

    public List<ScoreEntry> getScores() {
      return scores;
    }

    public void setScores(List<ScoreEntry> scores) {
      this.scores = scores;
    }

    public String getUuid() {
      return uuid;
    }

    public void setUuid(String uuid) {
      this.uuid = uuid;
    }

    public String getOwner() {
      return owner;
    }

    public void setOwner(String owner) {
      this.owner = owner;
    }

    public String getRom() {
      return rom;
    }

    public void setRom(String rom) {
      this.rom = rom;
    }

    public long getFileSize() {
      return fileSize;
    }

    public void setFileSize(long fileSize) {
      this.fileSize = fileSize;
    }
  }

  static class ScoreEntry {
    private int position;
    private String initials;
    private double numericScore;
    private String score;

    public ScoreEntry() {

    }

    public ScoreEntry(Score score) {
      this.position = score.getPosition();
      this.initials = score.getPlayerInitials();
      this.numericScore = score.getNumericScore();
      this.score = score.getScore();
    }

    public int getPosition() {
      return position;
    }

    public void setPosition(int position) {
      this.position = position;
    }

    public String getInitials() {
      return initials;
    }

    public void setInitials(String initials) {
      this.initials = initials;
    }

    public double getNumericScore() {
      return numericScore;
    }

    public void setNumericScore(double numericScore) {
      this.numericScore = numericScore;
    }

    public String getScore() {
      return score;
    }

    public void setScore(String score) {
      this.score = score;
    }
  }
}
