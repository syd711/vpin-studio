package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MenuScoreViewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private ImageView sectionIcon;

  @FXML
  private Label nameLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Label authorsLabel;

  @FXML
  private Label scoreInfoLabel;

  @FXML
  private BorderPane widgetPane;

  private WidgetCompetitionSummaryController competitionWidgetController;

  public void setData(GameRepresentation game, VpsTable tableById, PauseMenuItem pauseMenuItem, Image sectionImage) {
    this.nameLabel.setText(game.getGameDisplayName());
    this.versionLabel.setText("");
    this.authorsLabel.setText("");
    this.scoreInfoLabel.setText("");

    this.sectionIcon.setImage(sectionImage);

    // when game is mapped to VPS Table
    if (tableById != null) {
      String extVersion = game.getExtTableVersionId();
      VpsTableVersion version = tableById.getTableVersionById(extVersion);
      if (version != null) {
        this.versionLabel.setText(version.getComment());
        List<String> authors = version.getAuthors();
        if (authors != null && !authors.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", authors));
        }
      }
      else {
        this.versionLabel.setText(tableById.getManufacturer() + " (" + tableById.getYear() + ")");
        List<String> designers = tableById.getDesigners();
        if (designers != null && !designers.isEmpty()) {
          this.authorsLabel.setText(String.join(", ", designers));
        }
      }
    }

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      Pane competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);
      widgetPane.setCenter(competitionWidgetRoot);

      competitionWidgetController.setCompetition(CompetitionType.valueOf(pauseMenuItem.getCompetition().getType()), pauseMenuItem.getCompetition());
      competitionWidgetController.setFontSize(29);
    }
    catch (IOException e) {
      LOG.error("Failed to load pause score widget: " + e.getMessage(), e);
    }
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
