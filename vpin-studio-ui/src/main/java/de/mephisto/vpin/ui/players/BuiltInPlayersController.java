package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Account;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.preferences.PreferenceChangeListener;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.AvatarFactory;
import de.mephisto.vpin.ui.achievements.AchievementsUI;
import org.kordamp.ikonli.javafx.FontIcon;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.*;
import de.mephisto.vpin.commons.utils.i18n.Messages;

public class BuiltInPlayersController extends BasePlayersController implements Initializable, PreferenceChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(BuiltInPlayersController.class);
  private final static double AVATAR_SIZE = 44;

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
  private TableColumn<PlayerRepresentation, Label> maniaPlayerColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> columnCreatedAt;

  @FXML
  private StackPane tableStack;

  @FXML
  private Label playerCountLabel;

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
        playerCountLabel.setText(Messages.get("dialog.players_count", getCount()));

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
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, Messages.get("msg.delete_player", selection.getName()));
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        if (Features.MANIA_ENABLED && selection.getManiaAccountUuid() != null) {
          Optional<ButtonType> result2 = WidgetFactory.showConfirmation(Studio.stage, Messages.get("dialog.vpin_mania_player"), Messages.get("msg.the_player_is_a_registered_vpin_mania", selection.getName()), Messages.get("dialog.this_will_delete_the_online_account_and"));
          if (result2.isPresent() && result2.get().equals(ButtonType.OK)) {
            client.getPlayerService().deletePlayer(selection);

            Account acc = maniaClient.getAccountClient().getAccountByUuid(selection.getManiaAccountUuid());
            if (acc != null) {
              Cabinet cabinet = maniaClient.getCabinetClient().getDefaultCabinetCached();
              maniaClient.getAccountClient().deleteAccount(cabinet.getId(), acc.getId());
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
    NavigationController.setBreadCrumb(Arrays.asList(Messages.get("navigation.players"), Messages.get("navigation.build_in_players")));
    tableView.setPlaceholder(new Label(Messages.get("players.builtin_users.no_players_placeholder")));


    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loader.setResources(Messages.getBundle());
      playersLoadingOverlay = loader.load();
      WaitOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage(Messages.get("players.builtin_users.loading_players"));
    }
    catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    maniaPlayerColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getManiaAccountUuid()) && Features.MANIA_ENABLED) {
        try {
          Account accountByUuid = maniaClient.getAccountClient().getAccountByUuid(value.getManiaAccountUuid());
          if (accountByUuid != null) {
            return new SimpleObjectProperty<>(pill(Messages.get("players.builtin_users.linked"), "sm-pill-ok", "mdi2c-check"));
          }
        }
        catch (Exception e) {
          Label label = pill(Messages.get("players.builtin_users.link_error"), "sm-pill-error", "mdi2a-alert-circle-outline");
          label.setTooltip(new Tooltip(e.getMessage()));
          Features.MANIA_ENABLED = false;
          return new SimpleObjectProperty<>(label);
        }
      }
      Label label = new Label(Messages.get("players.builtin_users.not_linked"));
      label.getStyleClass().add("sm-hint");
      return new SimpleObjectProperty<>(label);
    });

    adminColumn.setVisible(true);
    adminColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.isAdministrative()) {
        return new SimpleObjectProperty<>(pill(Messages.get("players.builtin_users.default"), "sm-pill-ok", "mdi2c-check"));
      }
      Label label = new Label("—");
      label.getStyleClass().add("sm-hint");
      return new SimpleObjectProperty<>(label);
    });

    nameColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty<>(value.getName());
    });

    avatarColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (value.getAvatar() == null) {
        return new SimpleObjectProperty<>(AchievementsUI.initials(value.getInitials(), AVATAR_SIZE));
      }

      return new SimpleObjectProperty<>(AvatarFactory.create(client.getAssetService().getAsset(AssetType.AVATAR, value.getAvatar().getUuid()), AVATAR_SIZE));
    });

    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getDuplicatePlayerName())) {
        Label label = pill(value.getInitials(), "sm-pill-error", "mdi2a-alert-circle-outline");
        label.setTooltip(new Tooltip(Messages.get("players.players.duplicate_initials")));
        return new SimpleObjectProperty(label);
      }

      if (StringUtils.isEmpty(value.getInitials())) {
        return new SimpleObjectProperty(WidgetFactory.createExclamationIcon());
      }
      return new SimpleObjectProperty(value.getInitials().toUpperCase());
    });

    columnCreatedAt.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(value.getCreatedAt().atZone(ZoneId.systemDefault())));
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
      final ContextMenu menu = new ContextMenu();
      // filled in the capturing phase: ContextMenu#show ignores menus without items, so it must be populated before the row's own handler shows it
      row.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> refreshContextMenu(menu, row.getItem()));
      row.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> row.setContextMenu(isNowEmpty ? null : menu));
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
    maniaPlayerColumn.setVisible(Features.MANIA_ENABLED);

    if (PreferenceNames.MANIA_SETTINGS.equals(key)) {
      ManiaSettings settings = client.getPreferenceService().getJsonPreference(PreferenceNames.MANIA_SETTINGS, ManiaSettings.class);

      if (Features.MANIA_ENABLED) {
        maniaPlayerColumn.setVisible(!StringUtils.isEmpty(settings.getCabinetUuid()));
      }
    }
  }

  private void refreshContextMenu(ContextMenu ctxMenu, @Nullable PlayerRepresentation player) {
    ctxMenu.getItems().clear();
    if (player == null) {
      return;
    }

    MenuItem editItem = new MenuItem(Messages.get("players.builtin_users.context_menu.edit"));
    editItem.setGraphic(WidgetFactory.createIcon("mdi2p-pencil-outline"));
    editItem.setOnAction(e -> onEdit());
    ctxMenu.getItems().add(editItem);

    MenuItem addItem = new MenuItem(Messages.get("players.builtin_users.context_menu.add"));
    addItem.setGraphic(WidgetFactory.createIcon("mdi2p-plus"));
    addItem.setOnAction(e -> onAdd());
    ctxMenu.getItems().add(addItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem copyInitialsItem = new MenuItem(Messages.get("players.builtin_users.context_menu.copy_initials"));
    copyInitialsItem.setGraphic(WidgetFactory.createIcon("mdi2c-content-copy"));
    copyInitialsItem.setDisable(StringUtils.isEmpty(player.getInitials()));
    copyInitialsItem.setOnAction(e -> {
      ClipboardContent content = new ClipboardContent();
      content.putString(player.getInitials().toUpperCase());
      Clipboard.getSystemClipboard().setContent(content);
    });
    ctxMenu.getItems().add(copyInitialsItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteItem = new MenuItem(Messages.get("players.builtin_users.context_menu.delete"));
    deleteItem.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline"));
    deleteItem.setOnAction(e -> onDelete());
    ctxMenu.getItems().add(deleteItem);
  }

  private static Label pill(String text, String variant, String iconLiteral) {
    Label label = new Label(text);
    label.getStyleClass().addAll("sm-pill", variant);
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(12);
    icon.getStyleClass().add(variant.equals("sm-pill-ok") ? "c-icon-ok" : "c-icon-error");
    label.setGraphic(icon);
    return label;
  }

  @Override
  public void onViewActivated(@Nullable NavigationOptions options) {

  }
}
