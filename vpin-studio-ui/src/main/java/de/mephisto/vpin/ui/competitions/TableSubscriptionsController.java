package de.mephisto.vpin.ui.competitions;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.fx.widgets.WidgetCompetitionSummaryController;
import de.mephisto.vpin.commons.utils.CommonImageUtil;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.CompetitionType;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.WaitOverlayController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.mephisto.vpin.ui.Studio.client;

public class TableSubscriptionsController implements Initializable, StudioFXController {
  private final static Logger LOG = LoggerFactory.getLogger(TableSubscriptionsController.class);

  @FXML
  private TableView<CompetitionRepresentation> tableView;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnName;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnTable;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnCompetitionOwner;

  @FXML
  private TableColumn<CompetitionRepresentation, String> columnServer;

  @FXML
  private Button deleteBtn;

  @FXML
  private Button addBtn;

  @FXML
  private Button reloadBtn;

  @FXML
  private Button joinBtn;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private BorderPane competitionWidget;

  @FXML
  private StackPane tableStack;

  private Parent loadingOverlay;
  private WidgetCompetitionSummaryController competitionWidgetController;
  private BorderPane competitionWidgetRoot;


  private ObservableList<CompetitionRepresentation> data;
  private List<CompetitionRepresentation> competitions;
  private CompetitionsController competitionsController;
  private WaitOverlayController loaderController;

  private long discordBotId;

  // Add a public no-args constructor
  public TableSubscriptionsController() {
  }

  @FXML
  private void onCompetitionCreate() {
    CompetitionRepresentation c = Dialogs.openSubscriptionDialog(this.competitions, null);
    if (c != null) {
      CompetitionRepresentation newCmp = null;
      try {
        newCmp = client.getCompetitionService().saveCompetition(c);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
      onReload();
      tableView.getSelectionModel().select(newCmp);
    }
  }

  @FXML
  private void onJoin() {
    client.clearDiscordCache();
    CompetitionRepresentation c = Dialogs.openDiscordJoinCompetitionDialog();
    if (c != null) {
      try {
        CompetitionRepresentation newCmp = client.getCompetitionService().saveCompetition(c);
        onReload();
        tableView.getSelectionModel().select(newCmp);
      } catch (Exception e) {
        WidgetFactory.showAlert(Studio.stage, e.getMessage());
      }
    }
    else {
      onReload();
    }
  }

  @FXML
  private void onDelete() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      boolean isOwner = selection.getOwner().equals(String.valueOf(client.getDiscordService().getDiscordStatus(selection.getDiscordServerId()).getBotId()));
      String help = "You are the owner of this subscription.";
      String help2 = "The subscription and the corresponding channel will be deleted.";

      if (!isOwner) {
        help = "You are not the owner of this subscription.";
        help2 = "The subscription will be deleted and none of your highscores will be pushed there anymore.";
      }

      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete Subscription '" + selection.getName() + "'?",
          help, help2, "Delete Subscription");
      if (result.isPresent() && result.get().equals(ButtonType.OK)) {
        tableView.getSelectionModel().clearSelection();
        client.getCompetitionService().deleteCompetition(selection);
        NavigationController.setBreadCrumb(Arrays.asList("Competitions", "Discord Competitions"));
        onReload();
      }
    }
  }

  @FXML
  public void onReload() {
    client.clearWheelCache();

    tableView.setVisible(false);
    tableStack.getChildren().add(loadingOverlay);

    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    DiscordBotStatus discordStatus = client.getDiscordService().getDiscordStatus(guildId);
    if (!discordStatus.isValid()) {
      textfieldSearch.setDisable(true);
      addBtn.setDisable(true);
      deleteBtn.setDisable(true);
      reloadBtn.setDisable(true);
      joinBtn.setDisable(true);

      tableView.setPlaceholder(new Label("                                         No Discord bot found.\nCreate a Discord bot and add it in the preference section \"Discord Preferences\"."));
      tableView.setVisible(true);
      tableStack.getChildren().remove(loadingOverlay);

      if (competitionWidget != null) {
        competitionWidget.setVisible(false);
      }

      tableView.setItems(FXCollections.emptyObservableList());
      tableView.refresh();
      return;
    }

    new Thread(() -> {
      CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
      competitions = client.getCompetitionService().getSubscriptions();
      filterCompetitions(competitions);
      data = FXCollections.observableList(competitions);

      Platform.runLater(() -> {
        competitionWidget.setVisible(true);
        if (competitions.isEmpty()) {
          competitionWidget.setTop(null);
        }
        else {
          if (competitionWidget.getTop() == null) {
            competitionWidget.setTop(competitionWidgetRoot);
          }
        }

        tableView.setItems(data);
        tableView.refresh();
        if (selection != null) {
          tableView.getSelectionModel().select(selection);
        }
        else if (!data.isEmpty()) {
          tableView.getSelectionModel().select(0);
        }
        else {
          refreshView(Optional.empty());
        }

        tableStack.getChildren().remove(loadingOverlay);
        tableView.setVisible(true);
      });
    }).start();

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Competitions"));
    tableView.setPlaceholder(new Label("            No competitions found.\nClick the '+' button to create a new one."));

    try {
      FXMLLoader loader = new FXMLLoader(WaitOverlayController.class.getResource("overlay-wait.fxml"));
      loadingOverlay = loader.load();
      loaderController = loader.getController();
      loaderController.setLoadingMessage("Loading Competitions...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    columnName.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      Label label = new Label(value.getName());
      return new SimpleObjectProperty(label);
    });


    columnTable.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();
      GameRepresentation game = client.getGame(value.getGameId());
      Label label = new Label("- not available anymore -");
      if (game != null) {
        label = new Label(game.getGameDisplayName());
      }
      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);

      ByteArrayInputStream gameMediaItem = OverlayWindowFX.client.getGameMediaItem(value.getGameId(), PopperScreen.Wheel);
      Image image = new Image(gameMediaItem);
      ImageView view = new ImageView(image);
      view.setPreserveRatio(true);
      view.setSmooth(true);
      view.setFitWidth(60);
      view.setFitHeight(60);
      hBox.getChildren().addAll(view, label);

      return new SimpleObjectProperty(hBox);
    });

    columnServer.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();

      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);

      DiscordServer discordServer = client.getDiscordServer(value.getDiscordServerId());
      if (discordServer != null) {
        String avatarUrl = discordServer.getAvatarUrl();
        Image image = null;
        if (avatarUrl == null) {
          image = new Image(Studio.class.getResourceAsStream("avatar-blank.png"));
        }
        else {
          image = new Image(avatarUrl);
        }
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(50);
        view.setFitHeight(50);

        CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));
        Label label = new Label(discordServer.getName());
        hBox.getChildren().addAll(view, label);
      }

      return new SimpleObjectProperty(hBox);
    });

    columnCompetitionOwner.setCellValueFactory(cellData -> {
      CompetitionRepresentation value = cellData.getValue();

      HBox hBox = new HBox(6);
      hBox.setAlignment(Pos.CENTER_LEFT);
      PlayerRepresentation discordPlayer = client.getDiscordService().getDiscordPlayer(value.getDiscordServerId(), Long.valueOf(value.getOwner()));
      if (discordPlayer != null) {
        Image image = new Image(client.getCachedUrlImage(discordPlayer.getAvatarUrl()));
        ImageView view = new ImageView(image);
        view.setPreserveRatio(true);
        view.setFitWidth(50);
        view.setFitHeight(50);
        CommonImageUtil.setClippedImage(view, (int) (image.getWidth() / 2));

        Label label = new Label(discordPlayer.getName());
        hBox.getChildren().addAll(view, label);
      }

      return new SimpleObjectProperty(hBox);
    });

    tableView.setPlaceholder(new Label("            Try table subscriptions!\n" +
        "Create a new subscription by pressing the '+' button."));
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      refreshView(Optional.ofNullable(newSelection));
    });

    try {
      FXMLLoader loader = new FXMLLoader(WidgetCompetitionSummaryController.class.getResource("widget-competition-summary.fxml"));
      competitionWidgetRoot = loader.load();
      competitionWidgetController = loader.getController();
      competitionWidgetRoot.setMaxWidth(Double.MAX_VALUE);

      competitionWidgetRoot.managedProperty().bindBidirectional(competitionWidget.visibleProperty());
    } catch (IOException e) {
      LOG.error("Failed to load c-widget: " + e.getMessage(), e);
    }

    tableView.setRowFactory(tv -> {
      TableRow<CompetitionRepresentation> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && (!row.isEmpty())) {
//          onEdit();
        }
      });
      return row;
    });

    bindSearchField();
    onViewActivated();
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      tableView.getSelectionModel().clearSelection();
      refreshView(Optional.empty());

      filterCompetitions(this.competitions);
      tableView.setItems(data);
    });
  }

  private void filterCompetitions(List<CompetitionRepresentation> competitions) {
    List<CompetitionRepresentation> filtered = new ArrayList<>();
    String filterValue = textfieldSearch.textProperty().getValue();
    for (CompetitionRepresentation c : competitions) {
      if (!c.getName().toLowerCase().contains(filterValue.toLowerCase())) {
        continue;
      }

      filtered.add(c);
    }
    data = FXCollections.observableList(filtered);
  }

  private void refreshView(Optional<CompetitionRepresentation> competition) {
    CompetitionRepresentation newSelection = null;
    if (competition.isPresent()) {
      newSelection = competition.get();
    }

    boolean disable = newSelection == null;
    boolean isOwner = newSelection != null && newSelection.getOwner().equals(String.valueOf(this.discordBotId));
    deleteBtn.setDisable(disable || !isOwner);
    reloadBtn.setDisable(this.discordBotId <= 0);
    addBtn.setDisable(this.discordBotId <= 0);
    joinBtn.setDisable(this.discordBotId <= 0);

    if (competition.isPresent()) {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(true);
      }
      competitionWidgetController.setCompetition(CompetitionType.SUBSCRIPTION, competition.get());
    }
    else {
      if (competitionWidget.getTop() != null) {
        competitionWidget.getTop().setVisible(false);
      }
      competitionWidgetController.setCompetition(CompetitionType.SUBSCRIPTION, null);
    }
    competitionsController.setCompetition(competition.orElse(null));
  }

  @Override
  public void onViewActivated() {
    long guildId = client.getPreference(PreferenceNames.DISCORD_GUILD_ID).getLongValue();
    this.discordBotId = client.getDiscordService().getDiscordStatus(guildId).getBotId();
    if (this.competitionsController != null) {
      refreshView(Optional.empty());
    }
  }

  public void setCompetitionsController(CompetitionsController competitionsController) {
    this.competitionsController = competitionsController;
  }

  public Optional<CompetitionRepresentation> getSelection() {
    CompetitionRepresentation selection = tableView.getSelectionModel().getSelectedItem();
    if (selection != null) {
      return Optional.of(selection);
    }
    return Optional.empty();
  }
}