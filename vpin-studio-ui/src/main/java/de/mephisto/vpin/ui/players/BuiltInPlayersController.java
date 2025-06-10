package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.AvatarFactory;
import de.mephisto.vpin.ui.util.Dialogs;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class BuiltInPlayersController extends BasePlayersController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(BuiltInPlayersController.class);

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private TableView<PlayerRepresentation> tableView;

  @FXML
  private TableColumn<PlayerRepresentation, String> nameColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> initialsColumn;

  @FXML
  private TableColumn<PlayerRepresentation, Label> adminColumn;

  @FXML
  private TableColumn<PlayerRepresentation, Object> avatarColumn;

  @FXML
  private TableColumn<PlayerRepresentation, Label> tournamentColumn;

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
      players = client.getPlayerService().getPlayers();

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
    PlayerRepresentation player = Dialogs.openPlayerDialog(null, client.getPlayerService().getPlayers());
    if (player != null) {
      onReload();
      tableView.getSelectionModel().select(player);
    }
  }

  @FXML
  private void onEdit() {
    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      PlayerRepresentation player = Dialogs.openPlayerDialog(selection, client.getPlayerService().getPlayers());
      if (player != null) {
        onReload();
        tableView.getSelectionModel().select(player);
      }
    }
  }

  @FXML
  private void onDelete() {
    PlayerRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Player '" + selection.getName() + "'?");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        if (Features.MANIA_ENABLED && selection.getTournamentUserUuid() != null) {
          Optional<ButtonType> result2 = WidgetFactory.showConfirmation(Studio.stage, "Tournament Player", "The player \"" + selection.getName() + "\" is a registered tournament player.", "This will delete the online account and all related highscores and subscribed tournaments too.");
          if (result2.isPresent() && result2.get().equals(ButtonType.OK)) {
            client.getPlayerService().deletePlayer(selection);

            Account acc = maniaClient.getAccountClient().getAccountByUuid(selection.getTournamentUserUuid());
            if (acc != null) {
              maniaClient.getAccountClient().deleteAccount(acc.getId());
            }
            tableView.getSelectionModel().clearSelection();
            onReload();
          }
        }
        else {
          client.getPlayerService().deletePlayer(selection);
          tableView.getSelectionModel().clearSelection();
          onReload();
        }
      }
    }
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize();
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
    tableView.setPlaceholder(new Label("          No one want's to play with you?\n" +
        "Add new players or connect a Discord server."));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      playersLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Players...");
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }


    tournamentColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getTournamentUserUuid()) && Features.MANIA_ENABLED) {
        try {
          Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(value.getTournamentUserUuid());
          if (accountByUuid != null) {
            Label label = new Label();
            label.setGraphic(WidgetFactory.createCheckIcon());
            return new SimpleObjectProperty<>(label);
          }
        }
        catch (Exception e) {
          Label label = new Label();
          label.setGraphic(WidgetFactory.createExclamationIcon());
          label.setTooltip(new Tooltip(e.getMessage()));
          Features.MANIA_ENABLED = false;
          return new SimpleObjectProperty<>(label);
        }
      }
      return null;
    });

    adminColumn.setVisible(true);
    adminColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.isAdministrative()) {
        Label label = new Label();
        label.setGraphic(WidgetFactory.createCheckIcon());
        return new SimpleObjectProperty<>(label);
      }
      return null;
    });

    nameColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty<>(value.getName());
    });

    avatarColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.getAvatar() == null) {
        return new SimpleObjectProperty<>("");
      }

      return new SimpleObjectProperty<>(AvatarFactory.create(client.getAsset(AssetType.AVATAR, value.getAvatar().getUuid())));
    });

    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getDuplicatePlayerName())) {
        Label label = new Label(value.getInitials());
        String color = WidgetFactory.ERROR_COLOR;
        label.setStyle("-fx-font-color: " + color + ";-fx-text-fill: " + color + ";-fx-font-weight: bold;");
        return new SimpleObjectProperty(label);
      }

      if (StringUtils.isEmpty(value.getInitials())) {
        return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
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
      tableView.getSelectionModel().clearSelection();

      List<PlayerRepresentation> filtered = filterPlayers(this.players);
      tableView.setItems(FXCollections.observableList(filtered));
    });

    client.getPreferenceService().addListener(this);

    preferencesChanged(PreferenceNames.MANIA_SETTINGS, null);
    this.onReload();
  }

  @Override
  public void preferencesChanged(String key, Object value) {
    tournamentColumn.setVisible(Features.MANIA_ENABLED);

    if (PreferenceNames.MANIA_SETTINGS.equals(key)) {
      ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

      if (Features.MANIA_ENABLED) {
        tournamentColumn.setVisible(settings.isEnabled());
      }
    }
  }

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {

  }
}