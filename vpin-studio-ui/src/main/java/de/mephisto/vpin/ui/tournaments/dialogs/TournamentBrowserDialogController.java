package de.mephisto.vpin.ui.tournaments.dialogs;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetRepresentation;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.ui.tournaments.TournamentDialogs;
import de.mephisto.vpin.ui.tournaments.view.GameCellContainer;
import de.mephisto.vpin.ui.tournaments.view.TournamentTreeModel;
import de.mephisto.vpin.ui.vps.VpsSelection;
import de.mephisto.vpin.ui.vps.VpsTableContainer;
import de.mephisto.vpin.ui.vps.VpsVersionContainer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

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
  private TableView<TournamentTreeModel> tableView;

  @FXML
  private TableColumn<TournamentTreeModel, String> nameColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> playersColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> detailsColumn;

  @FXML
  private TableColumn<TournamentTreeModel, String> tablesColumn;

  private ManiaTournamentRepresentation tournament;

  private List<TournamentTreeModel> tableSelection = new ArrayList<>();

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
//    if (this.tournamentTreeModel.isPresent()) {
//      TournamentTreeModel treeModel = tournamentTreeModel.get();
//      ManiaTournamentRepresentation tournament = treeModel.getTournament();
//      String link = tournament.getDiscordLink();
//      if (!StringUtils.isEmpty(link)) {
//        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
//        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
//          try {
//            desktop.browse(new URI(link));
//          } catch (Exception e) {
//            LOG.error("Failed to open dashboard link: " + e.getMessage(), e);
//          }
//        }
//      }
//    }
  }

  private void reloadTables() {
    tableView.setItems(FXCollections.observableList(tableSelection));
    tableView.refresh();
  }

  @Override
  public void onDialogCancel() {
    this.tournament = null;
  }

  @FXML
  private void loadIScoredTables() {
    this.tableSelection.clear();
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
//      Platform.runLater(() -> {
//        if (nameField.getText().length() > 40) {
//          String sub = nameField.getText().substring(0, 40);
//          nameField.setText(sub);
//        }
//        tournament.setDisplayName(nameField.getText());
//        validate();
//      });
    }, 500));

    playersColumn.setCellValueFactory(cellData -> {
      GameRepresentation game = cellData.getValue().getGame();
      if (game != null) {
        return new SimpleObjectProperty(new GameCellContainer(game));
      }

      Label label = new Label("Table not installed");
      label.setStyle("-fx-padding: 3 6 3 6;");
      label.getStyleClass().add("error-title");
      return new SimpleObjectProperty(label);
    });

    detailsColumn.setCellValueFactory(cellData -> {
      VpsTable vpsTable = cellData.getValue().getVpsTable();
      return new SimpleObjectProperty(new VpsTableContainer(vpsTable));
    });

    tablesColumn.setCellValueFactory(cellData -> {
      VpsTableVersion vpsTableVersion = cellData.getValue().getVpsTableVersion();
      if (vpsTableVersion == null) {
        return new SimpleObjectProperty<>("All versions allowed.");
      }
      return new SimpleObjectProperty(new VpsVersionContainer(vpsTableVersion));
    });

    tableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TournamentTreeModel>() {
      @Override
      public void onChanged(Change<? extends TournamentTreeModel> c) {
        updateSelection();
      }
    });
  }

  private void updateSelection() {

    PlayerRepresentation defaultPlayer = client.getPlayerService().getDefaultPlayer();
    AssetRepresentation asset = defaultPlayer.getAvatar();
    ByteArrayInputStream in = client.getAsset(AssetType.AVATAR, asset.getUuid());

//    Image image = new Image(in);
//    Tile avatar = TileBuilder.create()
//      .skinType(Tile.SkinType.IMAGE)
//      .prefSize(UIDefaults.DEFAULT_AVATARSIZE*2, UIDefaults.DEFAULT_AVATARSIZE*2)
//      .backgroundColor(Color.TRANSPARENT)
//      .image(image)
//      .imageMask(Tile.ImageMask.ROUND)
//      .text("")
//      .textSize(Tile.TextSize.BIGGER)
//      .textAlignment(TextAlignment.CENTER)
//      .build();
//
//    avatarPane.getChildren().add(avatar);

  }

  public ManiaTournamentRepresentation getTournament() {
    return tournament;
  }

}
