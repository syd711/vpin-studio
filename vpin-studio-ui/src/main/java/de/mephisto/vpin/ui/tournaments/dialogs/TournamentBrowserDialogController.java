package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.LoadingOverlayController;
import de.mephisto.vpin.commons.fx.UIDefaults;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentSearchResult;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentSearchResultItem;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.AvatarImageUtil;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentBrowserDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentBrowserDialogController.class);

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private VBox avatarPane;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField searchText;

  @FXML
  private Label ownerLabel;

  @FXML
  private Label nameLabel;

  @FXML
  private Label startLabel;

  @FXML
  private Label remainingLabel;

  @FXML
  private Label endLabel;

  @FXML
  private Hyperlink discordLink;

  @FXML
  private Label descriptionText;

  @FXML
  private TableView<ManiaTournamentSearchResultItem> tableView;

  @FXML
  private TableColumn<ManiaTournamentSearchResultItem, Object> avatarColumn;

  @FXML
  private TableColumn<ManiaTournamentSearchResultItem, String> nameColumn;

  @FXML
  private TableColumn<ManiaTournamentSearchResultItem, String> playersColumn;

  @FXML
  private TableColumn<ManiaTournamentSearchResultItem, String> detailsColumn;

  @FXML
  private TableColumn<ManiaTournamentSearchResultItem, String> tablesColumn;

  @FXML
  private StackPane viewStack;


  private ManiaTournamentSearchResultItem tournament;
  private Optional<ManiaTournamentSearchResultItem> selection = Optional.empty();
  private Parent loadingOverlay;
  private int pageSize = 30;
  private int page = 0;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.tournament = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onDiscordLink() {
    if (this.selection.isPresent()) {
      ManiaTournamentSearchResultItem item = selection.get();
      String link = item.getDiscordLink();
      if (!StringUtils.isEmpty(link)) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(link));
          } catch (Exception e) {
            LOG.error("Failed to open discord link: " + e.getMessage(), e);
          }
        }
      }
    }
  }

  @Override
  public void onDialogCancel() {
    this.tournament = null;
  }

  @FXML
  private void loadIScoredTables() {
    this.tableView.refresh();

//    if (!StringUtils.isEmpty(dashboardUrl)) {
//      try {
//        GameRoom gameRoom = IScored.loadGameRoom(dashboardUrl);
//        iscoredScoresEnabled.setSelected(gameRoom.getSettings().isPublicScoresEnabled());
//
//        List<Game> games = gameRoom.getGames();
//        for (Game game : games) {
//          List<String> tags = game.getTags();
//          Optional<String> first = tags.stream().filter(t -> t.startsWith(VPS.BASE_URL)).findFirst();
//          if (first.isPresent()) {
//            String vpsUrl = first.get();
//            String idSegment = vpsUrl.substring(vpsUrl.lastIndexOf("/") + 1);
//            String[] split = idSegment.split("#");
//            VpsTable vpsTable = VPS.getInstance().getTableById(split[0]);
//            VpsTableVersion vpsVersion = null;
//            if (vpsTable != null && split.length > 1) {
//              vpsVersion = vpsTable.getVersion(split[1]);
//            }
//            GameRepresentation gameRep = null;
//            if (vpsTable != null) {
//              gameRep = client.getGameService().getGameByVpsTable(vpsTable, vpsVersion);
//            }
//            this.tableSelection.add(new TournamentTreeModel(tournament, gameRep, vpsTable, vpsVersion));
//            this.tableView.refresh();
//          }
//        }
//      } catch (Exception e) {
//        LOG.warn("Failed to load iscored dashboard: " + e.getMessage());
//      }
//    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                  No tournaments found!\nUse the search field to filter tournaments."));
    saveBtn.setDisable(true);
    searchText.textProperty().addListener((observableValue, s, t1) -> debouncer.debounce("nameField", () -> {
      this.page = 0;
      Platform.runLater(() -> {
        doSearch(t1);
      });
    }, 300));

    avatarColumn.setCellValueFactory(cellData -> {
      ManiaTournamentSearchResultItem value = cellData.getValue();
      String avatarUrl = maniaClient.getAccountClient().getAvatarUrl(value.getOwnerUuid());
      Image image = new Image(client.getCachedUrlImage(avatarUrl));
      Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(UIDefaults.DEFAULT_AVATARSIZE, UIDefaults.DEFAULT_AVATARSIZE)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .text("")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();
      return new SimpleObjectProperty<>(avatar);
    });

    nameColumn.setCellValueFactory(cellData -> {
      ManiaTournamentSearchResultItem value = cellData.getValue();
      return new SimpleObjectProperty(value.getDisplayName());
    });

    playersColumn.setCellValueFactory(cellData -> {
      ManiaTournamentSearchResultItem value = cellData.getValue();
      return new SimpleObjectProperty(value.getPlayerCount());
    });

    detailsColumn.setCellValueFactory(cellData -> {
      ManiaTournamentSearchResultItem value = cellData.getValue();
      value.getTableIdList();
      return new SimpleObjectProperty("");
    });

    tablesColumn.setCellValueFactory(cellData -> {
      ManiaTournamentSearchResultItem value = cellData.getValue();
      return new SimpleObjectProperty(new SimpleObjectProperty(""));
    });

    tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ManiaTournamentSearchResultItem>) c -> {
      if (c.getList().isEmpty()) {
        selection = Optional.empty();
      }
      else {
        selection = Optional.of(c.getList().get(0));
      }

      updateSelection(selection);
    });

    try {
      FXMLLoader loader = new FXMLLoader(LoadingOverlayController.class.getResource("loading-overlay.fxml"));
      loadingOverlay = loader.load();
      LoadingOverlayController ctrl = loader.getController();
      ctrl.setLoadingMessage("Loading Tournaments...");
    } catch (IOException e) {
      LOG.error("Failed to load loading overlay: " + e.getMessage());
    }

    Platform.runLater(() -> {
      doSearch("");
    });
  }

  private void doSearch(String t1) {
    Platform.runLater(() -> {
      try {
        viewStack.getChildren().add(loadingOverlay);
        ManiaTournamentSearchResult search = maniaClient.getTournamentClient().search(t1, 0, UIDefaults.TOURNAMENT_BROWSER_PAGE_SIZE);
        List<ManiaTournamentSearchResultItem> results = search.getResults();
        new Thread(() -> {
          Platform.runLater(() -> {
            for (ManiaTournamentSearchResultItem result : results) {
              String avatarUrl = maniaClient.getAccountClient().getAvatarUrl(result.getOwnerUuid());
              client.getCachedUrlImage(avatarUrl);
            }


            tableView.setItems(FXCollections.observableList(results));

            if (!results.isEmpty()) {
              tableView.getSelectionModel().select(0);
            }
            tableView.refresh();
            viewStack.getChildren().remove(loadingOverlay);
          });
        }).start();
      } catch (Exception e) {
        viewStack.getChildren().remove(loadingOverlay);
        WidgetFactory.showAlert(Studio.stage, "Error", "Search failed: " + e.getMessage());
      }
    });
  }

  private void updateSelection(Optional<ManiaTournamentSearchResultItem> selection) {
    nameLabel.setText("-");
    startLabel.setText("-");
    endLabel.setText("-");
    remainingLabel.setText("-");
    discordLink.setText("-");
    ownerLabel.setText("-");
    descriptionText.setText("-");
    avatarPane.getChildren().removeAll(avatarPane.getChildren());

    if (selection.isPresent()) {
      ManiaTournamentSearchResultItem item = selection.get();
      nameLabel.setText(item.getDisplayName());
      startLabel.setText(DateFormat.getDateTimeInstance().format(item.getStartDate()));
      endLabel.setText(DateFormat.getDateTimeInstance().format(item.getEndDate()));
      remainingLabel.setText(DateUtil.formatDuration(item.getStartDate(), item.getEndDate()));
      if (!StringUtils.isEmpty(item.getDiscordLink())) {
        discordLink.setText(item.getDiscordLink());
      }
      if (!StringUtils.isEmpty(item.getDescription())) {
        descriptionText.setText(item.getDescription());
      }

      ownerLabel.setText("-");
      Image image = new Image(maniaClient.getAccountClient().getAvatarUrl(item.getOwnerUuid()));
      Tile avatar = TileBuilder.create()
        .skinType(Tile.SkinType.IMAGE)
        .prefSize(UIDefaults.DEFAULT_AVATARSIZE * 2, UIDefaults.DEFAULT_AVATARSIZE * 2)
        .backgroundColor(Color.TRANSPARENT)
        .image(image)
        .imageMask(Tile.ImageMask.ROUND)
        .text("")
        .textSize(Tile.TextSize.BIGGER)
        .textAlignment(TextAlignment.CENTER)
        .build();

      avatarPane.getChildren().add(avatar);
    }
  }

  public ManiaTournamentRepresentation getTournament() {
    return tournament;
  }

}
