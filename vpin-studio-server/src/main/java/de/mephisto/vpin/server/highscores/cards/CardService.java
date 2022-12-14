package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {
  private final static Logger LOG = LoggerFactory.getLogger(CardService.class);

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DirectB2SService directB2SService;

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
      ScoreSummary summary = highscoreService.getHighscores(game.getId(), game.getGameDisplayName());
      if (!summary.getScores().isEmpty() && !StringUtils.isEmpty(summary.getRaw())) {
        Config.getCardGeneratorConfig().reload();


        BufferedImage bufferedImage = new CardGraphics(directB2SService, game, summary).draw();
        if (bufferedImage != null) {
          if (generateSampleCard) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          else {
            File highscoreCard = getCardFile(game);
            ImageUtil.write(bufferedImage, highscoreCard);
            return true;
          }
        }
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
  private File getCardFile(@NonNull Game game) {
    String screenName = Config.getCardGeneratorConfig().getString("popper.screen", PopperScreen.Other2.name());
    PopperScreen screen = PopperScreen.valueOf(screenName);
    File mediaFolder = game.getPinUPMediaFolder(screen);
    return new File(mediaFolder, FilenameUtils.getBaseName(game.getGameFileName()) + ".png");
  }
}
