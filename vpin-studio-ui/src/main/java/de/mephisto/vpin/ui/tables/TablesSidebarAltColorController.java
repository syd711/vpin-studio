package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.representations.GameRepresentation;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarAltColorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarAltColorController.class);

  @FXML
  private Button uploadBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  private Optional<GameRepresentation> game = Optional.empty();

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarAltColorController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      boolean uploaded = Dialogs.openAltSoundUploadDialog(tablesSidebarController, game.get());
      if (uploaded) {
        this.tablesSidebarController.getTablesController().onReload();
      }
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
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    lastModifiedLabel.setText("-");

//    if (g.isPresent()) {
//      GameRepresentation game = g.get();
//      boolean altColorAvailable = game.isAltSoundAvailable();
//
//      dataBox.setVisible(altColorAvailable);
//      emptyDataBox.setVisible(!altColorAvailable);
//
//      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
//
//      if (altColorAvailable) {
////        altSound = Studio.client.getAltSoundService().getAltSound(game.getId());
////        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(altSound.getModificationDate()));
//      }
//    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}