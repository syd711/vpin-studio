package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.pausemenu.model.PauseMenuItem;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameStatus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MenuSubmitterViewController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MenuSubmitterViewController.class);

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

  public void setData(GameRepresentation game, GameStatus status, VpsTable tableById, PauseMenuItem pauseMenuItem, Image sectionImage) {
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
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }
}
