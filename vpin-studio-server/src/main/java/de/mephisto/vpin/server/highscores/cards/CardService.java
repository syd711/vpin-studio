package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.fx.cards.CardGraphicsHighscore;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CardService implements InitializingBean, HighscoreChangeListener, PreferenceChangedListener, TableStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CardService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DefaultPictureService directB2SService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CardTemplatesService cardTemplatesService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private ManiaService maniaService;


  private CardSettings cardSettings;


  private Map<Integer, ScoreSummary> scoreCache = new LinkedHashMap<>();

  public File generateTableCardFile(Game game) {
    generateCard(game);
    return getCardSampleFile();
  }

  public File generateTemplateTableCardFile(Game game, int templateId) {
    try {
      CardTemplate template = cardTemplatesService.getTemplate(templateId);
      generateCard(game, true, template);
      return getCardSampleFile();
    }
    catch (Exception e) {
      LOG.error("Failed to generate template card: " + e.getMessage(), e);
    }
    return getCardSampleFile();
  }

  public List<String> getBackgrounds() {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  public boolean generateCard(Game game) {
    try {
      CardTemplate template = cardTemplatesService.getTemplateForGame(game);
      return generateCard(game, false, template);
    }
    catch (Exception e) {
      LOG.error("Failed to generate card: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean generateCard(Game game, boolean generateSampleCard, int templateId) {
    try {
      CardTemplate template = cardTemplatesService.getTemplate(templateId);
      return generateCard(game, generateSampleCard, template);
    }
    catch (Exception e) {
      LOG.error("Failed to generate card: " + e.getMessage(), e);
    }
    return false;
  }

  /**
   * The card must be drawn synchronized and in a FX thread.
   * We need to wait until finished, because otherwise the UI would show the previous result
   *
   * @param game
   * @param generateSampleCard
   * @param template
   * @return
   */
  public synchronized boolean generateCard(Game game, boolean generateSampleCard, CardTemplate template) {
    try {
      ScoreSummary summary = getScoreSummary(game, template, generateSampleCard);
      Platform.runLater(() -> {
        Thread.currentThread().setName("FX Card Generator Thread for " + game.getGameDisplayName());
        doGenerateCard(game, summary, generateSampleCard, template);
        LOG.info("Finished card generation for \"{}\"", game.getGameDisplayName());
      });

      Thread.currentThread().setName("Card Generator Thread for " + game.getGameDisplayName());
      synchronized (this) {
        this.wait();
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to generate image: " + e.getMessage(), e);
      return false;
    }
  }

  @NonNull
  private ScoreSummary getScoreSummary(Game game, CardTemplate template, boolean generateSampleCard) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    ScoreSummary summary = template.isRenderScoreDates() ? 
      highscoreService.getScoreSummaryWithDates(serverId, game):
      highscoreService.getScoreSummary(serverId, game);

    if (template.isRenderFriends()) {
      //add simply caching until a real card is generated, should be sufficient while editing
      if (generateSampleCard) {
        if (!scoreCache.containsKey(game.getId())) {
          summary = getMergedScoreSummary(summary, game);
          scoreCache.put(game.getId(), summary);
        }
        return scoreCache.get(game.getId());
      }

      scoreCache.clear();
      summary = getMergedScoreSummary(summary, game);
    }
    return summary;
  }


  /**
   * Returns a list of all scores for the given game
   *
   * @param game the game to retrieve the highscores for
   * @return all highscores of the given player
   */
  @NonNull
  public ScoreSummary getMergedScoreSummary(ScoreSummary summary, Game game) {
    if (Features.MANIA_ENABLED) {
      List<Score> externalScores = maniaService.getFriendsScoresFor(game);
      summary.mergeScores(externalScores);
    }
    return summary;
  }



  private boolean doGenerateCard(Game game, ScoreSummary summary, boolean generateSampleCard, CardTemplate template) {
    try {
      if (!summary.getScores().isEmpty() && !StringUtils.isEmpty(summary.getRaw())) {
        String screenName = cardSettings.getPopperScreen();

        //sample card are always generated
        if (generateSampleCard || StringUtils.isEmpty(screenName)) {
          BufferedImage bufferedImage = generateCard(game, summary, template);
          if (bufferedImage != null) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          return false;
        }

        if (!StringUtils.isEmpty(screenName)) {
          if (!game.isCardDisabled()) {
            BufferedImage bufferedImage = generateCard(game, summary, template);
            if (bufferedImage != null) {
              File highscoreCard = getCardFile(game, screenName);
              ImageUtil.write(bufferedImage, highscoreCard);
              LOG.info("Written highscore card: " + highscoreCard.getAbsolutePath());
              SLOG.info("Written highscore card: " + highscoreCard.getAbsolutePath());
              return true;
            }
          }
          else {
            LOG.info("Skipped card generation for \"" + game.getGameDisplayName() + "\", generation not enabled.");
            SLOG.info("Skipped card generation for \"" + game.getGameDisplayName() + "\", generation not enabled.");
          }

        }
        else {
          LOG.info("Skipped card generation, no target screen set.");
          SLOG.info("Skipped card generation, no target screen set.");
        }

        return false;
      }
      else {
        LOG.info("Skipped card generation for \"" + game.getGameDisplayName() + "\", no scores found.");
        SLOG.info("Skipped card generation for \"" + game.getGameDisplayName() + "\", no scores found.");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to generate highscore card: {}", e.getMessage(), e);
      SLOG.error("Failed to generate highscore card: " + e.getMessage());
    }
    finally {
      synchronized (this) {
        notifyAll();
      }
    }
    return false;
  }

  private BufferedImage generateCard_old(Game game, ScoreSummary summary, CardTemplate template) throws Exception {
    return new CardGraphics(directB2SService, frontendService, cardSettings.getCardResolution(), template, game, summary).draw();
  }

  private BufferedImage generateCard(Game game, ScoreSummary summary, CardTemplate template) throws Exception {
    CardResolution res = cardSettings.getCardResolution();

    CardGraphicsHighscore cardGraphics = new CardGraphicsHighscore(false);
    cardGraphics.setTemplate(template);

    CardData data = getCardData(game, summary, template);
    data.addBaseUrl("http://localhost:" + systemService.getServerPort() + "/" + VPinStudioServer.API_SEGMENT);
    cardGraphics.setData(data, res);
    // resize the cards to the needed resolution    
    cardGraphics.resize(res.toWidth(), res.toHeight());

    // then export image
    return cardGraphics.snapshot();
  }

  public CardData getCardData(Game game, int templateId) throws Exception {
    CardTemplate template = cardTemplatesService.getTemplate(templateId);
    ScoreSummary summary = getScoreSummary(game, template, false);
    return getCardData(game, summary, template);
  }

  private CardData getCardData(Game game, ScoreSummary summary, CardTemplate template) throws Exception {
    CardData cardData = new CardData();

    VpsTable vpsTable = null;
    String vpsTableId = game.getExtTableId();
    if (!StringUtils.isEmpty(vpsTableId)) {
      vpsTable = vpsService.getTableById(vpsTableId);
    }
    if (vpsTable != null) {
      cardData.setVpsName(vpsTable.getName());
      cardData.setManufacturer(vpsTable.getManufacturer());
      cardData.setYear(vpsTable.getYear() > 0 ? vpsTable.getYear() : null);
      cardData.setVpsTableId(vpsTableId);
    }

    cardData.setGameId(game.getId());
    cardData.setGameDisplayName(game.getGameDisplayName());
    cardData.setGameName(game.getGameName());

    cardData.setWheelUrl("media/" + game.getId() + "/" + VPinScreen.Wheel);
    cardData.setBackgroundUrl("assets/defaultbackground/" + game.getId());
    
    if (summary != null) {
      cardData.setRawScore(summary.getRaw());

      ObjectMapper mapper = new ObjectMapper();
      ArrayList<ScoreRepresentation> scores = new ArrayList<>();
      for (Score score : summary.getScores()) {
        String s = mapper.writeValueAsString(score);
        scores.add(mapper.readValue(s, ScoreRepresentation.class));
      }
      cardData.setScores(scores);
    }

    return cardData;
  }

  private File getCardSampleFile() {
    return new File(SystemService.RESOURCES, "highscore-card-sample.png");
  }

  @NonNull
  public File getCardFile(@NonNull Game game, @NonNull String screenName) {
    VPinScreen screen = VPinScreen.valueOf(screenName);
    File mediaFolder = frontendService.getMediaFolder(game, screen, "png", true);
    return new File(mediaFolder, game.getGameName() + ".png");
  }

  @Override
  public void highscoreChanged(@NonNull HighscoreChangeEvent event) {
    //not used for card generation
  }

  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {
    generateCard(game);
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.HIGHSCORE_CARD_SETTINGS.equalsIgnoreCase(propertyName)) {
      cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
      if (cardSettings == null) {
        cardSettings = new CardSettings();
      }
    }
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {

  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    generateCard(game);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.highscoreService.addHighscoreChangeListener(this);
    this.preferencesService.addChangeListener(this);
    this.frontendStatusService.addTableStatusChangeListener(this);
    this.preferenceChanged(PreferenceNames.HIGHSCORE_CARD_SETTINGS, null, null);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
