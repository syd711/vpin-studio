package de.mephisto.vpin.ui.cards;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.ui.Studio;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * What is it ? 
 * Small UI to scan instruction cards and run an image recognition to recognize text
 * The tool also maps the instruction card to a VPS database entry 
 * Also possible to quickly reformat captured text
 * Also possible to capture some portion of the image
 * The information is stored in a table JSON file placed in the database folder
 * 
 * How it works ? 
 * - point folder field to a folder that contains the instruction cards images
 * - point database field to a folder that will contain all table JSON files
 * - run the application
 */
public class InstructionsCardScanner extends Application {

  private final static Logger LOG = LoggerFactory.getLogger(InstructionsCardScanner.class);

  private File folder = new File("D:/Pincab Assets/_INSTRUCTION CARDS");
  private File database = new File("d:/TablesDB");


  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(final Stage stage) {
    LOG.info("load Instructions Cards Scanner...");
    Studio.stage = stage;
    Studio.hostServices = getHostServices();

    try {
      Rectangle2D screenBounds = Screen.getPrimary().getBounds();
      FXMLLoader loader = new FXMLLoader(InstructionsCardScanner.class.getResource("instructions-card.fxml"));
      Parent root = loader.load();
      InstructionsCardsController controller = loader.getController();
      controller.setRootFolder(folder);
      controller.setDatabase(database);

      Scene scene = new Scene(root);
      scene.setFill(Paint.valueOf("#212529"));
      stage.setTitle("Instructions Cards");
      stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-64.png")));
      stage.setScene(scene);
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setX((screenBounds.getWidth() / 2) - (1400 / 2));
      stage.setY((screenBounds.getHeight() / 2) - (900 / 2));
     
      stage.show();
    }
    catch (IOException e) {
      LOG.error("Failed to load scanner", e);
    }
  }
}
