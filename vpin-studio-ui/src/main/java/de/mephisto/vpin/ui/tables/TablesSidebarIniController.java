package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.ini.IniRepresentation;
import de.mephisto.vpin.restclient.ini.IniSectionRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesSidebarIniController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(TablesSidebarIniController.class);
  private final Debouncer debouncer = new Debouncer();

  @FXML
  private Button uploadBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button editBtn;

  @FXML
  private VBox dataBox;

  @FXML
  private VBox emptyDataBox;

  private Optional<GameRepresentation> game = Optional.empty();

  // Add a public no-args constructor
  public TablesSidebarIniController() {
  }

  @FXML
  private void onUpload() {
    if (game.isPresent()) {
      TableDialogs.directUpload(Studio.stage, AssetType.INI, game.get(), null);
    }
  }


  @FXML
  private void onEdit() {
    if (game.isPresent()) {
      try {
        GameRepresentation gameRepresentation = game.get();
        String iniPath = gameRepresentation.getIniPath();
        if (iniPath != null) {
          Studio.editGameFile(gameRepresentation, iniPath);
        }
      }
      catch (Exception e) {
        LOG.error("Failed to open .ini file: {}", e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open .ini file: " + e.getMessage());
      }
    }
  }

  @FXML
  private void onDelete() {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete .ini for table '" + this.game.get().getGameDisplayName() + "'?");
    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
      client.getIniService().delete(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(this.game.get().getId(), this.game.get().getRom());
    }
  }

  @FXML
  private void onReload() {
    if (this.game.isPresent()) {
      Studio.client.getGameService().reload(this.game.get().getId());
      EventManager.getInstance().notifyTableChange(game.get().getId(), null);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataBox.managedProperty().bindBidirectional(dataBox.visibleProperty());
    emptyDataBox.managedProperty().bindBidirectional(emptyDataBox.visibleProperty());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    deleteBtn.setDisable(true);
  }

  public void setGame(Optional<GameRepresentation> game) {
    this.game = game;
    this.refreshView(game);
  }

  public void refreshView(Optional<GameRepresentation> g) {
    reloadBtn.setDisable(g.isEmpty());

    dataBox.getChildren().removeAll(dataBox.getChildren());
    dataBox.setVisible(false);
    emptyDataBox.setVisible(true);
    uploadBtn.setDisable(true);
    deleteBtn.setDisable(true);
    editBtn.setDisable(true);

    if (g.isPresent()) {
      GameRepresentation game = g.get();
      boolean iniFileAvailable = game.getIniPath() != null;

      editBtn.setDisable(!iniFileAvailable);
      dataBox.setVisible(iniFileAvailable);
      emptyDataBox.setVisible(!iniFileAvailable);

      uploadBtn.setDisable(StringUtils.isEmpty(game.getRom()));
      deleteBtn.setDisable(!iniFileAvailable);

      if (iniFileAvailable) {
        IniRepresentation iniFile = client.getIniService().getIniFile(game.getId());
        if (iniFile != null) {
          List<IniSectionRepresentation> sections = iniFile.getSections();
          for (IniSectionRepresentation section : sections) {
            Label title = new Label(section.getName());
            title.setOpaqueInsets(new Insets(12, 0, 0, 0));
            title.getStyleClass().add("default-headline");
            dataBox.getChildren().add(title);

            Map<String, Object> values = section.getValues();
            Set<Map.Entry<String, Object>> entries = values.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
              HBox hBox = new HBox(3);
              hBox.setAlignment(Pos.CENTER_LEFT);
              Label key = new Label(entry.getKey() + ":");
              key.setPrefWidth(200);
              key.getStyleClass().add("default-text");
              hBox.getChildren().add(key);

              TextField textField = new TextField();
              textField.setText(String.valueOf(entry.getValue()));
              textField.setPrefWidth(200);
              textField.setStyle("-fx-font-size: 14px;");
              textField.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce(entry.getKey(), () -> {
                section.getValues().put(entry.getKey(), t1);
                save(iniFile);
              }, 300));

              hBox.getChildren().add(textField);

              dataBox.getChildren().add(hBox);
            }
            dataBox.getChildren().add(new Label(""));
          }
        }
        else {
          emptyDataBox.setVisible(true);
          dataBox.setVisible(false);
        }
      }
    }
  }

  private void save(IniRepresentation iniFile) {
    try {
      client.getIniService().save(iniFile, game.get().getId());
    }
    catch (Exception e) {
      LOG.error("Failed to save ini file: " + e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to save ini file: " + e.getMessage());
    }
  }
}