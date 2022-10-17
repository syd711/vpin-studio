package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.StudioFXController;
import de.mephisto.vpin.ui.util.TransitionUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TablesController implements Initializable, StudioFXController {

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnRom;

  @FXML
  private TableColumn<GameRepresentation, String> columnRomAlias;

  @FXML
  private TableColumn<GameRepresentation, String> columnNVOffset;

  @FXML
  private TableColumn<GameRepresentation, String> columnB2S;

  @FXML
  private TableColumn<GameRepresentation, String> columnStatus;

  @FXML
  private TableView<GameRepresentation> tableView;

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
  private BorderPane screenApron;

  @FXML
  private BorderPane screenOther2;

  @FXML
  private BorderPane screenWheel;

  @FXML
  private BorderPane screenInfo;

  @FXML
  private BorderPane screenHelp;

  @FXML
  private BorderPane screenLoading;

  @FXML
  private BorderPane screenAudio;

  @FXML
  private BorderPane screenAudioLaunch;

  @FXML
  private Label labelTableCount;

  @FXML
  private Node main;

  @FXML
  private Accordion accordion;

  @FXML
  private TitledPane titledPaneMedia;

  @FXML
  private Label labelId;

  @FXML
  private Label labelRom;

  @FXML
  private Label labelRomAlias;

  @FXML
  private Label labelNVOffset;

  @FXML
  private Label labelFilename;

  @FXML
  private Label labelLastPlayed;

  @FXML
  private Label labelTimesPlayed;

  // Add a public no-args constructor
  public TablesController() {
  }

  private VPinStudioClient client;
  private ObservableList<GameRepresentation> data;
  private List<GameRepresentation> games;

  @FXML
  private void onPlayClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    MediaView mediaView = (MediaView) borderPane.getCenter();
    mediaView.getMediaPlayer().setMute(false);
    mediaView.getMediaPlayer().setCycleCount(1);
    mediaView.getMediaPlayer().play();
  }

  @FXML
  private void onMediaViewClick(ActionEvent e) {
    Button source = (Button) e.getSource();
    BorderPane borderPane = (BorderPane) source.getParent();
    Node center = borderPane.getCenter();
    if (center instanceof MediaView) {
      MediaView mediaView = (MediaView) center;
      Media media = mediaView.getMediaPlayer().getMedia();
      String s = media.getSource();
      try {
        Desktop.getDesktop().browse(URI.create(s));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    else if(center instanceof ImageView) {
      ImageView imageView = (ImageView) center;
      String url = (String) imageView.getUserData();
      try {
        Desktop.getDesktop().browse(URI.create(url));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

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

  @FXML
  private void onTableScan() {
    GameRepresentation gameRepresentation = tableView.getSelectionModel().selectedItemProperty().get();
    WidgetFactory.createProgressDialog(new TableScanProgressModel(client, "Scanning Table '" + gameRepresentation + "'", gameRepresentation));
    this.onReload();
  }

  @FXML
  private void onTablesScan() {
    WidgetFactory.createProgressDialog(new TablesScanProgressModel(client, "Scanning Tables"));
    this.onReload();
  }

  @FXML
  private void onReload() {
    List<GameRepresentation> games = client.getGames();
    data.removeAll();
    for (GameRepresentation game : games) {
      data.add(game);
    }
    tableView.refresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();
    this.accordion.setExpandedPane(titledPaneMedia);

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


    columnRom.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      String rom = value.getRom();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        return new SimpleStringProperty(value.getOriginalRom());
      }
      return new SimpleStringProperty(rom);
    });

    columnRomAlias.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (!StringUtils.isEmpty(value.getOriginalRom())) {
        return new SimpleStringProperty(value.getRom());
      }
      return new SimpleStringProperty("-");
    });

    columnNVOffset.setCellValueFactory(cellData -> {
      GameRepresentation value = cellData.getValue();
      if (value.getNvOffset() > 0) {
        return new SimpleStringProperty(String.valueOf(value.getNvOffset()));
      }
      return new SimpleStringProperty("");
    });


    tableView.setItems(data);
    tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        updateMedia(newSelection);
      }
    });

    if (!data.isEmpty()) {
      tableView.getSelectionModel().select(0);
    }
  }

  private void updateMedia(GameRepresentation game) {
    Platform.runLater(() -> {
      labelId.setText(String.valueOf(game.getId()));
      labelRom.setText(game.getOriginalRom() != null ? game.getOriginalRom() : game.getRom());
      labelRomAlias.setText(game.getOriginalRom() != null ? game.getRom() : "-");
      labelNVOffset.setText(game.getNvOffset() > 0 ? String.valueOf(game.getNvOffset()) : "-");
      labelFilename.setText(game.getGameFileName());
      labelLastPlayed.setText(game.getLastPlayed() != null ? game.getLastPlayed().toString() : "-");
      labelTimesPlayed.setText(String.valueOf(game.getNumberPlays()));

      GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
      GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Topper);
      WidgetFactory.createMediaContainer(screenTopper, client, item);

      item = gameMedia.getItem(PopperScreen.BackGlass);
      WidgetFactory.createMediaContainer(screenBackglass, client, item);

      item = gameMedia.getItem(PopperScreen.Audio);
      WidgetFactory.createMediaContainer(screenAudio, client, item);

      item = gameMedia.getItem(PopperScreen.AudioLaunch);
      WidgetFactory.createMediaContainer(screenAudioLaunch, client, item);

      item = gameMedia.getItem(PopperScreen.DMD);
      WidgetFactory.createMediaContainer(screenDMD, client, item);

      item = gameMedia.getItem(PopperScreen.GameInfo);
      WidgetFactory.createMediaContainer(screenInfo, client, item);

      item = gameMedia.getItem(PopperScreen.GameHelp);
      WidgetFactory.createMediaContainer(screenHelp, client, item);

      item = gameMedia.getItem(PopperScreen.PlayField);
      WidgetFactory.createMediaContainer(screenPlayfield, client, item);

      item = gameMedia.getItem(PopperScreen.Menu);
      WidgetFactory.createMediaContainer(screenApron, client, item);

      item = gameMedia.getItem(PopperScreen.Loading);
      WidgetFactory.createMediaContainer(screenLoading, client, item);

      item = gameMedia.getItem(PopperScreen.Other2);
      WidgetFactory.createMediaContainer(screenOther2, client, item);

      item = gameMedia.getItem(PopperScreen.Wheel);
      WidgetFactory.createMediaContainer(screenWheel, client, item);
    });

  }

  @Override
  public void dispose() {

  }
}