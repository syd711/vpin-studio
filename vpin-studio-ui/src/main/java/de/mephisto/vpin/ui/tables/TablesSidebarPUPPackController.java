package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.popper.ScreenMode;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PupPackRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TablesSidebarPUPPackController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarPUPPackController.class);

  private Optional<GameRepresentation> game = Optional.empty();

  @FXML
  private Button uploadBtn;

  @FXML
  private Label lastModifiedLabel;

  @FXML
  private Label bundleSizeLabel;

  @FXML
  private VBox emptyDataBox;

  @FXML
  private VBox dataBox;

  @FXML
  private ComboBox<String> optionsCombo;

  @FXML
  private CheckBox enabledCheckbox;

  @FXML
  private FontIcon screenBackglass;

  @FXML
  private FontIcon screenDMD;

  @FXML
  private FontIcon screenFullDMD;

  @FXML
  private FontIcon screenTopper;

  @FXML
  private Label screenBackglassLabel;

  @FXML
  private Label screenDMDLabel;

  @FXML
  private Label screenFullDMDLabel;

  @FXML
  private Label screenTopperLabel;

  @FXML
  private Button applyBtn;

  private TablesSidebarController tablesSidebarController;

  // Add a public no-args constructor
  public TablesSidebarPUPPackController() {
  }

  @FXML
  private void onOptionApply() {

  }

  @FXML
  private void onLink(ActionEvent e) {
    Hyperlink link = (Hyperlink) e.getSource();
    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(new URI(link.getText()));
      } catch (Exception ex) {
        LOG.error("Failed to open link: " + ex.getMessage(), ex);
      }
    }
  }

  @FXML
  private void onPupPackEnable() {
    if (game.isPresent() && game.get().isPupPackAvailable()) {
      GameRepresentation g = game.get();
      Studio.client.getPupPackService().setPupPackEnabled(g.getId(), enabledCheckbox.isSelected());
    }
  }


  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      GameRepresentation g = game.get();
      if (StringUtils.isEmpty(g.getRom())) {
        WidgetFactory.showAlert(Studio.stage, "No ROM name found for \"" + g.getGameDisplayName() + "\".", "To upload a PUP pack, a ROM name must have been resolved for the table.");
        return;
      }

      boolean uploaded = Dialogs.openPupPackUploadDialog(tablesSidebarController, game.get());
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
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    enabledCheckbox.setVisible(false);
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);

    bundleSizeLabel.setText("-");
    lastModifiedLabel.setText("-");

    optionsCombo.getItems().clear();
    optionsCombo.setItems(FXCollections.emptyObservableList());
    optionsCombo.setDisable(true);
    applyBtn.setDisable(true);

    screenBackglass.setVisible(false);
    screenDMD.setVisible(false);
    screenTopper.setVisible(false);
    screenFullDMD.setVisible(false);

    screenBackglassLabel.setText("-");
    screenDMDLabel.setText("-");
    screenTopperLabel.setText("-");
    screenFullDMDLabel.setText("-");

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean pupPackAvailable = game.isPupPackAvailable();


      dataBox.setVisible(pupPackAvailable);
      emptyDataBox.setVisible(!pupPackAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      enabledCheckbox.setSelected(false);

      if (pupPackAvailable) {
        PupPackRepresentation pupPack = Studio.client.getPupPackService().getPupPack(game.getId());
        enabledCheckbox.setSelected(pupPack.isEnabled());

        List<String> options = pupPack.getOptions();
        optionsCombo.setItems(FXCollections.observableList(options));
        optionsCombo.setDisable(options.isEmpty());
        applyBtn.setDisable(options.isEmpty());

        if(!pupPack.getScreenBackglassMode().equals(ScreenMode.off)) {
          screenBackglassLabel.setText("(" + pupPack.getScreenBackglassMode().name() + ")");
          screenBackglass.setVisible(true);
        }

        if(!pupPack.getScreenDMDMode().equals(ScreenMode.off)) {
          screenDMDLabel.setText("(" + pupPack.getScreenDMDMode().name() + ")");
          screenDMD.setVisible(true);
        }

        if(!pupPack.getScreenFullDMDMode().equals(ScreenMode.off)) {
          screenFullDMD.setText("(" + pupPack.getScreenFullDMDMode().name() + ")");
          screenFullDMDLabel.setVisible(true);
        }

        if(!pupPack.getScreenTopperMode().equals(ScreenMode.off)) {
          screenTopperLabel.setText("(" + pupPack.getScreenTopperMode().name() + ")");
          screenTopper.setVisible(true);
        }

        bundleSizeLabel.setText(FileUtils.readableFileSize(pupPack.getSize()));
        lastModifiedLabel.setText(SimpleDateFormat.getDateTimeInstance().format(pupPack.getModificationDate()));
      }
    }
  }

  public void setSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}