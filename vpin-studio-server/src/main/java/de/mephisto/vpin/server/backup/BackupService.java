package de.mephisto.vpin.server.backup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.backup.BackupDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.Preferences;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpsdb.VpsDbEntry;
import de.mephisto.vpin.server.vpsdb.VpsEntryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.util.*;

@Service
public class BackupService {
  private final static Logger LOG = LoggerFactory.getLogger(BackupService.class);

  @Autowired
  private VpsEntryService vpsEntryService;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameService gameService;

  private final static List<String> IGNORED_PREFERENCES = Arrays.asList("id", "class", "filterSettings", "ignoredValidations");

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  public String create() throws Exception {
    Map<String, Object> backup = new HashMap<>();

    Preferences preferences = preferencesService.getPreferences();
    backup.put("preferences", preferences);

    List<Player> buildInPlayers = playerService.getBuildInPlayers();
    backup.put("players", buildInPlayers);

    List<Game> games = gameService.getGames();
    List<Object> gameEntryList = new ArrayList<>();

    for (Game game : games) {
      Map<String, Object> gameData = new HashMap<>();
      gameData.put("fileName", game.getGameFileName());
      gameData.put("notes", game.getComment());
      gameData.put("vpsTableId", game.getExtTableId());
      gameData.put("vpsVersionId", game.getExtTableVersionId());
      gameData.put("version", game.getVersion());
      gameData.put("highscoreCardsDisabled", game.isCardDisabled());
      gameEntryList.add(gameData);
      backup.put("games", gameEntryList);
    }

    List<VpsDbEntry> allVpsEntries = vpsEntryService.getAllVpsEntries();
    backup.put("vpsEntries", allVpsEntries);

    return objectMapper.writeValueAsString(backup);
  }

  public boolean restore(String backupJson, String backupDescriptorJson) {
    try {
      BackupDescriptor backupDescriptor = objectMapper.readValue(backupDescriptorJson, BackupDescriptor.class);
      Map<String, Object> map = objectMapper.readValue(backupJson, Map.class);

      if (backupDescriptor.isPlayers()) {
        List<Map<String, Object>> playerEntries = (List<Map<String, Object>>) map.get("players");
        List<Player> players = Arrays.asList(objectMapper.convertValue(playerEntries, Player[].class));
        for (Player entry : players) {
          Player playerForInitials = playerService.getPlayerForInitials(-1, entry.getInitials());
          if (playerForInitials == null || !playerForInitials.getName().equalsIgnoreCase(entry.getName())) {
            entry.setId(0l);
            entry.setTournamentUserUuid(null);
            entry.setAvatar(null);
            Player save = playerService.save(entry);
            LOG.info("Restored player {}", save);
          }
        }
      }

      if (backupDescriptor.isPreferences()) {
        Map<String, Object> preferenceEntry = (Map<String, Object>) map.get("preferences");
        Preferences restoredPreferences = objectMapper.convertValue(preferenceEntry, Preferences.class);
        BeanWrapper sourceBean = new BeanWrapperImpl(restoredPreferences);
        PropertyDescriptor[] propertyDescriptors = sourceBean.getPropertyDescriptors();

        Map<String, Object> values = new HashMap<>();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
          String name = propertyDescriptor.getName();
          if (IGNORED_PREFERENCES.contains(name)) {
            continue;
          }
          values.put(name, sourceBean.getPropertyValue(name));
        }
        preferencesService.savePreference(values);
        LOG.info("Updated preferences from restored values.");

      }

      if (backupDescriptor.isVpsComments()) {
        List<Map<String, Object>> vpsEntries = (List<Map<String, Object>>) map.get("vpsEntries");
        List<VpsDbEntry> allVpsEntries = Arrays.asList(objectMapper.convertValue(vpsEntries, VpsDbEntry[].class));
        for (VpsDbEntry entry : allVpsEntries) {
          VpsDbEntry vpsEntry = vpsEntryService.getVpsEntry(entry.getVpsTableId());
          if (!StringUtils.isEmpty(vpsEntry.getComment())) {
            vpsEntry.setComment(entry.getComment());
            vpsEntryService.save(vpsEntry);
            LOG.info("Restored {}", vpsEntry);
          }
        }
      }

      if (backupDescriptor.isGames()) {
        List<Game> knownGames = gameService.getKnownGames(-1);
        List<Map<String, Object>> games = (List<Map<String, Object>>) map.get("games");
        for (Map<String, Object> gameEntry : games) {
          String fileName = (String) gameEntry.get("fileName");
          Optional<Game> gameOptional = knownGames.stream().filter(g -> g.getGameFileName().equalsIgnoreCase(fileName)).findFirst();
          if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            if (backupDescriptor.isGameComments() && StringUtils.isEmpty(game.getComment())) {
              game.setComment((String) gameEntry.get("notes"));
            }
            if (backupDescriptor.isGameVersion()) {
              game.setVersion((String) gameEntry.get("version"));
            }
            if (backupDescriptor.isGameVpsMapping()) {
              game.setExtTableId((String) gameEntry.get("vpsTableId"));
              game.setExtTableVersionId((String) gameEntry.get("vpsVersionId"));
            }
            if (backupDescriptor.isGameCardSettings()) {
              game.setCardDisabled((Boolean) gameEntry.get("highscoreCardsDisabled"));
            }

//            gameService.save(game);
            LOG.info("Restored game settings for \"" + game.getGameFileName() + "\"");
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to restore backup: {}", e.getMessage(), e);
    }
    return false;
  }
}
