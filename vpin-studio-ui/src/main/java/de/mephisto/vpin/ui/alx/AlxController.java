package de.mephisto.vpin.ui.alx;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxController implements Initializable, StudioFXController, StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(AlxController.class);

  @FXML
  private VBox mostPlayedWidget;

  @FXML
  private VBox timePlayedWidget;

  @FXML
  private VBox scoresWidget;


  // Add a public no-args constructor
  public AlxController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<TableAlxEntry> entries = client.getAlxService().getAlxEntries();
    AlxFactory.createMostPlayed(mostPlayedWidget, entries);
    AlxFactory.createLongestPlayed(timePlayedWidget, entries);
//    AlxFactory.createRecordedScores(scoresWidget, entries);

    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));
  }

  @Override
  public void onViewActivated() {
    NavigationController.setBreadCrumb(Arrays.asList("Analytics"));
  }
}