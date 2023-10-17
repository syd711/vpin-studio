package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
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

  public File generateSampleCard(Game game) throws Exception {
    File cardSampleFile = getCardSampleFile();
    if (!cardSampleFile.exists()) {
      generateCard(game, true);
    }
    return getCardSampleFile();
  }

  public List<String> getBackgrounds() {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  public boolean generateCard(Game game, boolean generateSampleCard) throws Exception {
    try {
      long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
      ScoreSummary summary = highscoreService.getScoreSummary(serverId, game.getId(), game.getGameDisplayName());
      if (!summary.getScores().isEmpty() && !StringUtils.isEmpty(summary.getRaw())) {
        Config.getCardGeneratorConfig().reload();

        //sample card are always generated
        if (generateSampleCard) {
          BufferedImage bufferedImage = new CardGraphics(directB2SService, game, summary).draw();
          if (bufferedImage != null) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          return false;
        }

        //otherwise check if the card rendering is enabled
        String screenName = Config.getCardGeneratorConfig().getString("popper.screen", null);
        if (!StringUtils.isEmpty(screenName)) {
          BufferedImage bufferedImage = new CardGraphics(directB2SService, game, summary).draw();
          if (bufferedImage != null) {
            File highscoreCard = getCardFile(game, screenName);
            ImageUtil.write(bufferedImage, highscoreCard);
            return true;
          }
        }

        return false;
      }
      else {
        LOG.info("Skipped card generation for " + game.getGameDisplayName() + ", no scores found.");
      }
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
    return false;
  }

  private File getCardSampleFile() {
    return new File(SystemService.RESOURCES, "highscore-card-sample.png");
  }

  @NonNull
  private File getCardFile(@NonNull Game game, @NonNull String screenName) {
    PopperScreen screen = PopperScreen.valueOf(screenName);
    File mediaFolder = game.getPinUPMediaFolder(screen);
    return new File(mediaFolder, FilenameUtils.getBaseName(game.getGameFileName()) + ".png");
  }

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    //not used for card generation
  }

  @Override
  public void highscoreUpdated(@NonNull Game game, @NonNull Highscore highscore) {
    try {
      generateCard(game, false);
    } catch (Exception e) {
      LOG.error("Error updating card after highscore change event: " + e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.highscoreService.addHighscoreChangeListener(this);
  }
}
