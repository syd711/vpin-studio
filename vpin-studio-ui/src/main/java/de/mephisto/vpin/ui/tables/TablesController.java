package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class TablesController implements Initializable {

  @FXML
  private TableColumn<GameRepresentation, String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation, String> columnFilename;

  @FXML
  private TableView tableView;

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


  private VPinStudioClient client;

  // Add a public no-args constructor
  public TablesController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();
    final ObservableList<GameRepresentation> data = FXCollections.observableArrayList(client.getGames());

    columnDisplayName.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("gameDisplayName")
    );
    columnFilename.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation, String>("gameDisplayName")
    );

    tableView.setItems(data);

    GameMediaRepresentation gameMedia = client.getGameMedia(60);

    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.Topper);
    WidgetFactory.createMediaContainer(screenTopper, item.getMimeType(), client.getURL(item.getUri()));

    item = gameMedia.getItem(PopperScreen.BackGlass);
    WidgetFactory.createMediaContainer(screenBackglass, item.getMimeType(), client.getURL(item.getUri()));
//
//    item = gameMedia.getItem(PopperScreen.DMD);
//    WidgetFactory.createMediaContainer(screenDMD, item.getMimeType(), client.getURL(item.getUri()));

    item = gameMedia.getItem(PopperScreen.GameInfo);
    WidgetFactory.createMediaContainer(screenInfo, item.getMimeType(), client.getURL(item.getUri()));

    item = gameMedia.getItem(PopperScreen.GameHelp);
    WidgetFactory.createMediaContainer(screenHelp, item.getMimeType(), client.getURL(item.getUri()));

    item = gameMedia.getItem(PopperScreen.PlayField);
    Node mediaContainer = WidgetFactory.createMediaContainer(screenPlayfield, item.getMimeType(), client.getURL(item.getUri()));
    mediaContainer.rotateProperty().set(90);
  }
}