package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.WidgetFactory;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.events.TileEvent;
import eu.hansolo.tilesfx.tools.Rank;
import eu.hansolo.tilesfx.tools.Ranking;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;

import java.net.URL;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.ResourceBundle;

public class CompetitionsController implements Initializable, StudioFXController {

  @FXML
  private TableView tableView;

  // Add a public no-args constructor
  public CompetitionsController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = WidgetFactory.openCompetitionDialog();
    if(c != null) {
      Studio.client.saveCompetition(c);
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Offline Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));
  }
}