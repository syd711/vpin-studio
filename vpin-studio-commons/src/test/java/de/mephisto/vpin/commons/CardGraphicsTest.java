package de.mephisto.vpin.commons;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.commons.fx.cards.CardGraphicsHighscore;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.highscores.HighscoreCardResolution;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class CardGraphicsTest extends Application {


  @Test
  public void generateHighscoreCard() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    Platform.startup(() -> {
      try {
        HighscoreCardResolution res = HighscoreCardResolution.HDReady;
        CardGraphicsHighscore graphics = generateHighscore(true);
        graphics.resize(res.toWidth(), res.toHeight());
        BufferedImage image = graphics.snapshot();
        assertNotNull(image);
        //ImageUtil.write(image, new File("c:/temp/highscord_raw.png"));

        graphics = generateHighscore(false);
        graphics.resize(res.toWidth(), res.toHeight());
        image = graphics.snapshot();
        assertNotNull(image);
        //ImageUtil.write(image, new File("c:/temp/highscord.png"));
      }
      catch (Exception e) {
        // make the test fail in case of Execption
        fail(e);
      }
      countDownLatch.countDown();
    });
    countDownLatch.await();
  }

  public CardTemplate loadTemplate(String filename) throws IOException {
    try (InputStream in = getClass().getResourceAsStream(filename)) {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(in, CardTemplate.class);	
    }
  }


  public CardGraphicsHighscore generateHighscore(boolean useRawScore) {
    CardGraphicsHighscore graphics = new CardGraphicsHighscore(true);
    try {
      CardTemplate template = loadTemplate("template.json");
      template.setRawScore(useRawScore);

      graphics.setTemplate(template);
    }
    catch (Exception e) {
      System.err.println("Cannot load template");
      e.printStackTrace(System.err);
    }

    CardData data = CardDataMock.create();
    graphics.setData(data);

    return graphics;
  }

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 800, 450));

    CardGraphicsHighscore graphics = generateHighscore(true);
    layout.setCenter(graphics);
    
    stage.show();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
