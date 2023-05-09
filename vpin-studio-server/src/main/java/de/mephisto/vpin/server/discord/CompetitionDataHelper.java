package de.mephisto.vpin.server.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import de.mephisto.vpin.connectors.discord.DiscordMessage;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CompetitionDataHelper {
  private final static Logger LOG = LoggerFactory.getLogger(CompetitionDataHelper.class);

  private static final ObjectMapper objectMapper = new ObjectMapper();
  public static final String DATA_INDICATOR = "Data: ";

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Nullable
  public static String toBase64(@NonNull Competition competition, @NonNull Game game) {
    try {
      DiscordCompetitionData data = new DiscordCompetitionData();
      data.setName(competition.getName());
      data.setTname(game.getGameDisplayName());
      data.setSdt(competition.getStartDate());
      data.setMode(competition.getJoinMode());
      data.setEdt(competition.getEndDate());
      data.setFs(game.getGameFileSize());
      data.setUuid(competition.getUuid());
      data.setRom(game.getRom());
      data.setOwner(competition.getOwner());

      String json = objectMapper.writeValueAsString(data);
      return new Base64Encoder().encode(json.getBytes(StandardCharsets.UTF_8));
    } catch (JsonProcessingException e) {
      LOG.error("Failed to persist competition data: " + e.getMessage(), e);
    }
    return null;
  }


  @Nullable
  public static DiscordCompetitionData getCompetitionData(@NonNull DiscordMessage msg) {
    DiscordCompetitionData competitionData = getCompetitionData(msg.getEmbedDescription());
    if(competitionData != null) {
      competitionData.setMsgId(msg.getId());
    }

    return competitionData;
  }

  @Nullable
  public static DiscordCompetitionData getCompetitionData(@NonNull Message msg) {
    List<MessageEmbed> embeds = msg.getEmbeds();
    for (MessageEmbed embed : embeds) {
      DiscordCompetitionData competitionData = getCompetitionData(embed.getDescription());
      if(competitionData != null) {
        competitionData.setMsgId(msg.getIdLong());
      }

      return competitionData;
    }
    return null;
  }

  @Nullable
  private static DiscordCompetitionData getCompetitionData(@Nullable String messageText) {
    try {
      if (messageText == null) {
        return null;
      }
      if (messageText.contains(DATA_INDICATOR)) {
        String dataBase64 = messageText.substring(messageText.indexOf(DATA_INDICATOR) + DATA_INDICATOR.length()).trim();
        String data = new String(new Base64Encoder().decode(dataBase64));
        return objectMapper.readValue(data, DiscordCompetitionData.class);
      }
      return null;
    } catch (JsonProcessingException e) {
      LOG.info("Failed to read competition data from '" + messageText + "':" + e.getMessage());
    }
    return null;
  }
}
