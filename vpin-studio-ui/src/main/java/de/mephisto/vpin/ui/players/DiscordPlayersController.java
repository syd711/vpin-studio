package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.utils.ImageUtil;
import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class DiscordPlayersController implements Initializable {
  @FXML
  private TextField searchTextField;

  @FXML
  private CheckBox validPlayersCheckbox;

  @FXML
  private TableView<PlayerRepresentation> tableView;

  @FXML
  private TableColumn<PlayerRepresentation, String> nameColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> initialsColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> avatarColumn;

  private ObservableList<PlayerRepresentation> data;
  private List<PlayerRepresentation> players;
  private PlayersController playersController;
  private boolean filterValidPlayers = true;

  // Add a public no-args constructor
  public DiscordPlayersController() {
  }

  @FXML
  private void onReload() {
    this.players = client.getPlayers(PlayerDomain.DISCORD);
    this.refreshView();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Discord Members"));
    tableView.setPlaceholder(new Label("                           No one wants to play with you?\n" +
        "Edit your preferences to connect a Discord server with your VPin."));

    nameColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (StringUtils.isEmpty(value.getInitials())) {
        Label label = new Label(value.getName());
        String color = "#888585";
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + "; -fx-font-style: italic;");
        return new SimpleObjectProperty(label);
      }

      return new SimpleObjectProperty(value.getName());
    });
    avatarColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.getAvatarUrl() == null) {
        return new SimpleObjectProperty("");
      }

      Image image = new Image(client.getCachedUrlImage(value.getAvatarUrl()));
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      ImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
      return new SimpleObjectProperty(view);
    });
    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (StringUtils.isEmpty(value.getInitials())) {
        return new SimpleObjectProperty("");
      }

      if(!StringUtils.isEmpty(value.getDuplicatePlayerName())) {
        Label label = new Label(value.getInitials());
        String color = "#FF3333";
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
        return new SimpleObjectProperty(label);
      }

      return new SimpleObjectProperty(value.getInitials().toUpperCase());
    });

    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      updateSelection(Optional.ofNullable(newSelection));
    });

    searchTextField.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();

      List<PlayerRepresentation> filtered = filterPlayers(this.players);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    validPlayersCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      filterValidPlayers = newValue;
      refreshView();
    });

    this.players = client.getPlayers(PlayerDomain.DISCORD);
    this.refreshView();
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
    for (PlayerRepresentation player : players) {
      if(filterValidPlayers && StringUtils.isEmpty(player.getInitials())) {
        continue;
      }

      if (player.getName().toLowerCase().contains(filterValue.toLowerCase()) ||
          (player.getInitials() != null && player.getInitials().toLowerCase().contains(filterValue))) {
        filtered.add(player);
      }
    }
    return filtered;
  }

  public void refreshView() {
    this.searchTextField.setDisable(true);

    PlayerRepresentation playerRepresentation = tableView.getSelectionModel().getSelectedItem();
    tableView.getSelectionModel().clearSelection();

    new Thread(() -> {
      Platform.runLater(() -> {
        data = FXCollections.observableList(filterPlayers(players));
        tableView.setItems(data);
        tableView.refresh();
        if (data.contains(playerRepresentation)) {
          tableView.getSelectionModel().select(playerRepresentation);
        }
        this.searchTextField.setDisable(false);
      });
    }).start();
  }

  public Optional<PlayerRepresentation> getSelection() {
    PlayerRepresentation playerRepresentation = tableView.getSelectionModel().getSelectedItem();
    if(playerRepresentation != null) {
      return Optional.of(playerRepresentation);
    }
    return Optional.empty();
  }

  public int getCount() {
    return this.players.size();
  }
}