package de.mephisto.vpin.commons;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.commons.fx.cards.CardData;
import de.mephisto.vpin.commons.fx.cards.CardGraphicsHighscore;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class CardGraphicsTest extends Application {


  @Test
  public void generateHighscoreCard() throws Exception {
    Platform.startup(() -> {
      try {
        CardGraphicsHighscore graphics = generateHighscore(true);
        graphics.resize(1024, 768);
        BufferedImage image = graphics.snapshot();
        ImageUtil.write(image, new File("c:/temp/highscord_raw.png"));

        graphics = generateHighscore(false);
        graphics.resize(1024, 768);
        image = graphics.snapshot();
        ImageUtil.write(image, new File("c:/temp/highscord.png"));
      }
      catch (IOException ioe) {
      }
      });
  }

  public CardTemplate loadTemplate(String filename) throws IOException {
    try (InputStream in = getClass().getResourceAsStream(filename)) {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readValue(in, CardTemplate.class);	
    }
  }


  public CardGraphicsHighscore generateHighscore(boolean useRawScore) {
    CardGraphicsHighscore graphics = new CardGraphicsHighscore();
    try {
      CardTemplate template = loadTemplate("template.json");
      template.setRawScore(useRawScore);

      graphics.setTemplate(template);
    }
    catch (Exception e) {
      System.err.println("Cannot load template");
      e.printStackTrace(System.err);
    }

    CardData data = new CardDataMock();
    graphics.setData(data);

    return graphics;
  }

  @Override
  public void start(final Stage stage) {
  
    BorderPane layout = new BorderPane();
    stage.setScene(new Scene(layout, 600, 400));

    CardGraphicsHighscore graphics = generateHighscore(true);
    layout.setCenter(graphics);
    
    stage.show();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }
}
