package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.cards.CardResolution;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Service
public class CardService implements InitializingBean, HighscoreChangeListener, PreferenceChangedListener, TableStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CardService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DefaultPictureService defaultPictureService;

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
  private ManiaService maniaService;


  private CardSettings cardSettings;


  private Map<Integer, ScoreSummary> scoreCache = new LinkedHashMap<>();

  public byte[] generateTableCardFile(Game game) {
    CardTemplate template = cardTemplatesService.getTemplateForGame(game);
    ScoreSummary summary = getScoreSummary(game, template, true);
    return generatePreview(game, summary, template);
  }

  public byte[] generateTemplateTableCardFile(Game game, long templateId) {
    CardTemplate template = cardTemplatesService.getTemplateOrDefault(templateId);
    ScoreSummary summary = getScoreSummary(game, template, true);
    return generatePreview(game, summary, template);
  }

  public boolean generateCard(Game game) {
    CardTemplate template = cardTemplatesService.getTemplateForGame(game);
    return generateCard(game, template);
  }

  public boolean generateCard(Game game, Long templateId) {
    CardTemplate template = cardTemplatesService.getTemplateOrDefault(templateId);
    return generateCard(game, template);
  }

  @NonNull
  private ScoreSummary getScoreSummary(Game game, CardTemplate template, boolean generatePreview) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    ScoreSummary summary = (template != null && template.isRenderScoreDates()) ?
        highscoreService.getScoreSummaryWithDates(serverId, game) :
        highscoreService.getScoreSummary(serverId, game);

    if (template != null && template.isRenderFriends()) {
      //add simply caching until a real card is generated, should be sufficient while editing
      if (generatePreview) {
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

  private byte[] generatePreview(Game game, ScoreSummary summary, CardTemplate template) {
    try {
      BufferedImage bufferedImage = doGenerateCardImage(game, summary, template);
      return ImageUtil.toBytes(bufferedImage);
    }
    catch (Exception e) {
      LOG.error("Failed to generate highscore preview", e);
      SLOG.error("Failed to generate highscore card: " + e.getMessage());
      return null;
    }
  }

  /**
   * Generate the card and store it
   */
  public synchronized boolean generateCard(Game game, CardTemplate template) {
    try {
      ScoreSummary summary = getScoreSummary(game, template, false);
      if (!summary.getScores().isEmpty() && !StringUtils.isEmpty(summary.getRaw())) {
        String screenName = cardSettings.getPopperScreen();
        if (!StringUtils.isEmpty(screenName)) {
          if (!game.isCardDisabled()) {
            BufferedImage bufferedImage = doGenerateCardImage(game, summary, template);
            if (bufferedImage != null) {
              File highscoreCard = getCardFile(game, screenName);
              //TODO add a parameter to define the policy
              //REPLACE => keep highscoreCard
              //APPEND => highscoreCard = FileUtils.uniqueAssetByMarker(highscoreCard, "Highscore Card");
              //PREPEND/BACKUP => highscoreCard = FileUtils.backupAssetByMarker(highscoreCard, "Highscore Card");

              if (highscoreCard.exists() && !highscoreCard.delete()) {
                LOG.info("Writing highscore card {} failed, file is locked.", highscoreCard.getAbsolutePath());
                SLOG.info("Writing highscore card " + highscoreCard.getAbsolutePath() + " failed, file is locked.");
              }
              else {
                ImageUtil.write(bufferedImage, highscoreCard);
                LOG.info("Written highscore card: " + highscoreCard.getAbsolutePath());
                SLOG.info("Written highscore card: " + highscoreCard.getAbsolutePath());
              }

//              FileUtils.addMarker(highscoreCard, "Highscore Card");


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
    return false;
  }

  /*
   * The card must be drawn synchronized and in a FX thread.
   * We need to wait until finished, because otherwise the UI would show the previous result
   */
  private BufferedImage doGenerateCardImage(Game game, ScoreSummary summary, CardTemplate template) throws Exception {
    // sync between FX thread and calling thread
    CountDownLatch latch = new CountDownLatch(1);
    BufferedImage[] generatedImage = {null};
    Platform.runLater(() -> {
      try {
        CardResolution res = cardSettings.getCardResolution();

        CardGraphicsHighscore cardGraphics = new CardGraphicsHighscore(false);
        cardGraphics.setTemplate(template);

        CardData data = getCardData(game, summary, template, true);

        cardGraphics.setData(data, res);
        // resize the cards to the needed resolution    
        cardGraphics.resize(res.toWidth(), res.toHeight());

        // then export image
        generatedImage[0] = cardGraphics.snapshot();

        LOG.info("Finished card generation for \"{}\"", game.getGameDisplayName());
      }
      finally {
        latch.countDown();
      }
    });
    // wait for termination of FX thread
    latch.await();

    return generatedImage[0];
  }

  //-----------------------------------------

  public List<String> getBackgrounds() {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  public CardTemplate getCardTemplate(long templateId) {
    return cardTemplatesService.getTemplateOrDefault(templateId);
  }

  public CardData getCardData(Game game, long templateId, boolean withStreams) {
    CardTemplate template = cardTemplatesService.getTemplateOrDefault(templateId);
    return getCardData(game, template, withStreams);
  }

  public CardData getCardData(Game game, CardTemplate template, boolean withStreams) {
    ScoreSummary summary = getScoreSummary(game, template, false);
    return getCardData(game, summary, template, withStreams);
  }

  private CardData getCardData(Game game, ScoreSummary summary, CardTemplate template, boolean withStreams) {
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

    // load overwrites
    TableDetails details = frontendService.getTableDetails(game.getId());
    if (details != null) {
      if (StringUtils.isNotEmpty(details.getManufacturer())) {
        cardData.setManufacturer(details.getManufacturer());
      }
      if (details.getGameYear() != null) {
        cardData.setYear(details.getGameYear());
      }
    }

    cardData.setGameId(game.getId());
    cardData.setGameDisplayName(game.getGameDisplayName());
    cardData.setGameName(game.getGameName());

    if (withStreams) {
      cardData.setBackground(getImage(game, cardData, template, "background"));

      cardData.setWheel(getImage(game, cardData, template, "wheel"));

      if (template.isRenderManufacturerLogo()) {
        cardData.setManufacturerLogo(getImage(game, cardData, template, "manufacturerLogo"));
      }

      if (template.isRenderOtherMedia()) {
        cardData.setOtherMedia(getImage(game, cardData, template, "otherMedia"));
      }
    }

    if (summary != null) {
      cardData.setRawScore(summary.getRaw());

      ObjectMapper mapper = new ObjectMapper();
      ArrayList<ScoreRepresentation> scores = new ArrayList<>();
      for (Score score : summary.getScores()) {
        try {
          String s = mapper.writeValueAsString(score);
          scores.add(mapper.readValue(s, ScoreRepresentation.class));
        }
        catch (Exception e) {
          LOG.error("cannot decode score %s", score.getFormattedScore(), e);
        }
      }
      cardData.setScores(scores);
    }

    return cardData;
  }

  public byte[] getImage(Game game, CardData cardData, CardTemplate template, String imageName) {
    try {
      if ("background".equals(imageName)) {
        File background = defaultPictureService.getRawDefaultPicture(game);
        if (background != null && !background.exists()) {
          defaultPictureService.extractDefaultPicture(game);
        }
        if (background != null && background.exists()) {
          return org.apache.commons.io.FileUtils.readFileToByteArray(background);
        }
      }
      else if ("wheel".equals(imageName)) {
        FrontendMediaItem media = frontendService.getDefaultMediaItem(game, VPinScreen.Wheel);
        if (media != null && media.getFile().exists()) {
          return org.apache.commons.io.FileUtils.readFileToByteArray(media.getFile());
        }
      }
      else if ("manufacturerLogo".equals(imageName)) {
        File manufacturer = defaultPictureService.getManufacturerPicture(cardData.getManufacturer(), cardData.getYear(), template.isManufacturerLogoUseYear());
        if (manufacturer != null && manufacturer.exists()) {
          return org.apache.commons.io.FileUtils.readFileToByteArray(manufacturer);
        }
      }
      else if ("otherMedia".equals(imageName)) {
        if (template.getOtherMediaScreen() != null) {
          List<FrontendMediaItem> medias = frontendService.getMediaItems(game, template.getOtherMediaScreen());
          for (FrontendMediaItem media : medias) {
            if (media.getFile().exists() && media.getMimeType().contains("image")) {
              return org.apache.commons.io.FileUtils.readFileToByteArray(media.getFile());
            }
          }
        }
      }
    }
    catch (IOException e) {
      LOG.info("Cannot load {} for game {}: {}", imageName, game.getGameDisplayName(), e.getMessage());
    }
    return null;
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
