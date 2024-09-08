package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
public class CardService implements InitializingBean, HighscoreChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(CardService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DefaultPictureService directB2SService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private CardTemplatesService cardTemplatesService;

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

  public boolean generateCard(Game game, boolean generateSampleCard, CardTemplate template) {
    try {
      Semaphore semaphore = new Semaphore(0);
      Platform.runLater(() -> {
        doGenerateCard(game, generateSampleCard, template);
        semaphore.release();
      });
      semaphore.acquire();
      return true;
    }
    catch (InterruptedException e) {
      LOG.error("Failed to generate image: " + e.getMessage(), e);
      return false;
    }
  }

  private boolean doGenerateCard(Game game, boolean generateSampleCard, CardTemplate template) {
    try {
      long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
      ScoreSummary summary = highscoreService.getScoreSummary(serverId, game);
      if (!summary.getScores().isEmpty() && !StringUtils.isEmpty(summary.getRaw())) {
        //otherwise check if the card rendering is enabled
        CardSettings cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
        if (cardSettings == null) {
          cardSettings = new CardSettings();
        }

        //sample card are always generated
        if (generateSampleCard) {
          BufferedImage bufferedImage = new CardGraphics(directB2SService, cardSettings.getCardResolution(), template, game, summary).draw();
          if (bufferedImage != null) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          return false;
        }

        String screenName = cardSettings.getPopperScreen();
        if (!StringUtils.isEmpty(screenName)) {
          if (!game.isCardDisabled()) {
            BufferedImage bufferedImage = new CardGraphics(directB2SService, cardSettings.getCardResolution(), template, game, summary).draw();
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
      LOG.error("Failed to generate highscore card: " + e.getMessage(), e);
      SLOG.error("Failed to generate highscore card: " + e.getMessage());
    }
    return false;
  }

  private File getCardSampleFile() {
    return new File(SystemService.RESOURCES, "highscore-card-sample.png");
  }

  @NonNull
  private File getCardFile(@NonNull Game game, @NonNull String screenName) {
    VPinScreen screen = VPinScreen.valueOf(screenName);
    File mediaFolder = game.getMediaFolder(screen);
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
  public void afterPropertiesSet() throws Exception {
    this.highscoreService.addHighscoreChangeListener(this);
  }
}
