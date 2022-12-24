package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.utils.ImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class BuiltInPlayersController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(BuiltInPlayersController.class);

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private TextField searchTextField;

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

  @FXML
  private TableColumn<PlayerRepresentation, String> columnCreatedAt;

  @FXML
  private StackPane tableStack;

  private Parent playersLoadingOverlay;


  private ObservableList<PlayerRepresentation> data;
  private List<PlayerRepresentation> players;
  private PlayersController playersController;

  // Add a public no-args constructor
  public BuiltInPlayersController() {
  }

  @FXML
  private void onReload() {
    this.searchTextField.setDisable(true);

    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();
    boolean disable = selection == null;
    editBtn.setDisable(disable);
    deleteBtn.setDisable(disable);

    tableView.setVisible(false);
    tableStack.getChildren().add(playersLoadingOverlay);

    new Thread(() -> {
      players = client.getPlayers();

      Platform.runLater(() -> {
        data = FXCollections.observableList(filterPlayers(players));
        tableView.setItems(data);
        tableView.refresh();
        if (data.contains(selection)) {
          tableView.getSelectionModel().select(selection);
          editBtn.setDisable(false);
          deleteBtn.setDisable(false);
        }

        this.searchTextField.setDisable(false);

        tableStack.getChildren().remove(playersLoadingOverlay);
        tableView.setVisible(true);

      });
    }).start();
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
    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      PlayerRepresentation player = Dialogs.openPlayerDialog(selection);
      if (player != null) {
        doSave(player);
      }
      else {
        onReload();
      }
    }
  }

  @FXML
  private void onDelete() {
    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Player '" + selection.getName() + "'?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        client.deletePlayer(selection);
        tableView.getSelectionModel().clearSelection();
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
    tableView.setPlaceholder(new Label("          No one want's to play with you?\n" +
        "Add new players or connect a Discord server."));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      playersLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Players...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

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
        return new SimpleObjectProperty("");
      }

      Image image = new Image(client.getAsset(value.getAvatar().getUuid()));
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      ImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
      return new SimpleObjectProperty(view);
    });
    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getDuplicatePlayerName())) {
        Label label = new Label(value.getInitials());
        String color = "#FF3333";
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
        return new SimpleObjectProperty(label);
      }

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
    columnCreatedAt.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(DateFormat.getInstance().format(value.getCreatedAt()));
    });

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      boolean disable = newSelection == null;
      editBtn.setDisable(disable);
      deleteBtn.setDisable(disable);

      if (oldSelection == null || !oldSelection.equals(newSelection)) {
        updateSelection(Optional.ofNullable(newSelection));
      }
    });


    tableView.setRowFactory(tv -> {
      TableRow<PlayerRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
          onEdit();
        }
      });
      return row;
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      onReload();
    });

    this.onReload();
  }

  public void setPlayersController(PlayersController playersController) {
    this.playersController = playersController;
  }

  private void updateSelection(Optional<PlayerRepresentation> player) {
    playersController.updateSelection(player);
  }

  private List<PlayerRepresentation> filterPlayers(List<PlayerRepresentation> players) {
    List<PlayerRepresentation> filtered = new ArrayList<>();
    String filterValue = searchTextField.textProperty().getValue();
    if (filterValue == null) {
      filterValue = "";
    }

    for (PlayerRepresentation player : players) {
      if (player.getName() == null || player.getInitials() == null) {
        continue;
      }

      if (player.getName().toLowerCase().contains(filterValue.toLowerCase()) || player.getInitials().toLowerCase().contains(filterValue)) {
        filtered.add(player);
      }
    }
    return filtered;
  }

  public Optional<PlayerRepresentation> getSelection() {
    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }

  public int getCount() {
    return this.players != null ? this.players.size() : 0;
  }
}