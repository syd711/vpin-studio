package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.Dialogs;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class BuildInPlayersController implements Initializable, StudioFXController {

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private TableView<PlayerRepresentation> tableView;

  @FXML
  private TableColumn<PlayerRepresentation, String> idColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> nameColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> initialsColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> avatarColumn;

  // Add a public no-args constructor
  public BuildInPlayersController() {
  }

  @FXML
  private void onReload() {
    PlayerRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    List<PlayerRepresentation> players = client.getPlayers();
    ObservableList<PlayerRepresentation> data = FXCollections.observableList(players);
    tableView.setItems(data);
    tableView.refresh();
    tableView.getSelectionModel().select(selection);

    if (selection == null && !data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  @FXML
  private void onAdd() {
    PlayerRepresentation p = Dialogs.openPlayerDialog(null);
    if (p != null) {
      doSave(p);
    }
  }

  @FXML
  private void onEdit() {
    PlayerRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    if (selection != null) {
      PlayerRepresentation player = Dialogs.openPlayerDialog(selection);
      if (player != null) {
        doSave(player);
      }
    }
  }

  @FXML
  private void onDelete() {
    PlayerRepresentation selection = tableView.getSelectionModel().selectedItemProperty().get();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation("Delete Player '" + selection.getName() + "'?", "Delete Player");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.deletePlayer(selection);
        onReload();
      }
    }
  }

  private void doSave(PlayerRepresentation p) {
    try {
      PlayerRepresentation newPlayer = client.savePlayer(p);
      onReload();
      tableView.getSelectionModel().select(newPlayer);
    } catch (Exception e) {
      WidgetFactory.showAlert(e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
    tableView.setPlaceholder(new Label("            No one want's to play with you?\n" +
        "Add new players or connect a Discord server."));

    idColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(String.valueOf(value.getId()));
    });
    nameColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });
    avatarColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.getAvatar() == null) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(18);
      fontIcon.setIconColor(Paint.valueOf("#66FF66"));
      fontIcon.setIconLiteral("bi-check-circle");
      return new SimpleObjectProperty(fontIcon);
    });
    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (StringUtils.isEmpty(value.getInitials())) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleObjectProperty(value.getInitials().toUpperCase());
    });

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      editBtn.setDisable(disable);
      deleteBtn.setDisable(disable);
      refreshView(Optional.ofNullable(newSelection));
    });

    onReload();
  }

  private void refreshView(Optional<PlayerRepresentation> newSelection) {

  }
}