package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.TransitionUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TablesController implements Initializable {

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnFilename;

  @FXML
  private TableView tableView;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private BorderPane screenTopper;

  @FXML
  private BorderPane screenBackglass;

  @FXML
  private BorderPane screenDMD;

  @FXML
  private BorderPane screenPlayfield;

  @FXML
  private BorderPane screenInfo;

  @FXML
  private BorderPane screenHelp;

  @FXML
  private BorderPane screenLoading;

  @FXML
  private Label labelTableCount;

  @FXML
  private Node main;

  // Add a public no-args constructor
  public TablesController() {
  }

  private VPinStudioClient client;
  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    if (e.getCode().equals(KeyCode.ENTER)) {
      tableView.getSelectionModel().select(0);
      tableView.requestFocus();
    }
  }

  @FXML
  private void onTableMouseClicked(MouseEvent mouseEvent) {
    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
      if (mouseEvent.getClickCount() == 2) {
        TransitionUtil.createTranslateByXTransition(main, 300, 600).playFromStart();
      }
    }
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();

    bindTable();
    bindSearchField();
  }

  private void bindSearchField() {
    textfieldSearch.textProperty().addListener((observableValue, s, filterValue) -> {
      List<GameRepresentation> filtered = new ArrayList<>();
      for (GameRepresentation game : games) {
        if (game.getGameDisplayName().toLowerCase().contains(filterValue.toLowerCase())) {
          filtered.add(game);
        }
      }
      data.setAll(filtered);
    });
  }

  private void bindTable() {
    games = client.getGames();
    data = FXCollections.observableArrayList(games);
    labelTableCount.setText(data.size() + " tables");

    columnDisplayName.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("gameDisplayName")
    );
    columnFilename.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("rom")
    );

    tableView.setItems(data);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        GameRepresentation game = (GameRepresentation) newSelection;
        updateMedia(game);
      }
    });

    if (!data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void updateMedia(GameRepresentation game) {
    GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Topper);
    WidgetFactory.createMediaContainer(screenTopper, client, item);

    item = gameMedia.getItem(PopperScreen.BackGlass);
    WidgetFactory.createMediaContainer(screenBackglass, client, item);

    item = gameMedia.getItem(PopperScreen.DMD);
    WidgetFactory.createMediaContainer(screenDMD, client, item);

    item = gameMedia.getItem(PopperScreen.GameInfo);
    WidgetFactory.createMediaContainer(screenInfo, client, item);

    item = gameMedia.getItem(PopperScreen.GameHelp);
    WidgetFactory.createMediaContainer(screenHelp, client, item);

    item = gameMedia.getItem(PopperScreen.PlayField);
    WidgetFactory.createMediaContainer(screenPlayfield, client, item);

    item = gameMedia.getItem(PopperScreen.Loading);
    WidgetFactory.createMediaContainer(screenLoading, client, item);
  }
}