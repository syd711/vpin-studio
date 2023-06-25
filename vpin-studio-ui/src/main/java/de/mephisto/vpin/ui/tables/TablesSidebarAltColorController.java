package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.AltColor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAltColorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAltColorController.class);

  @FXML
  private Button uploadBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label typeLabel;

  @FXML
  private Label filesLabel;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  private AltColor altColor;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarAltColorController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      Dialogs.openAltColorUploadDialog(tablesSidebarController, game.get());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    this.altColor = null;

    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    lastModifiedLabel.setText("-");
    typeLabel.setText("-");
    filesLabel.setText("-");

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean altColorAvailable = game.isAltColorAvailable();

      dataBox.setVisible(altColorAvailable);
      emptyDataBox.setVisible(!altColorAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));

      if (altColorAvailable) {
        altColor = Studio.client.getAltColorService().getAltColor(game.getId());
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altColor.getModificationDate()));
        typeLabel.setText(altColor.getAltColorType().name());

        List<String> files = altColor.getFiles();
        filesLabel.setText(String.join(", ", files));
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}