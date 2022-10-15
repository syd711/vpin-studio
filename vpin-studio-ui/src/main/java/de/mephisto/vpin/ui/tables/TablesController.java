package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

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
  private MediaView screenPlayfield;


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
    GameMediaItemRepresentation item = gameMedia.getItem(PopperScreen.PlayField);
    String uri = client.getURL(item.getUri());

    Media media = new Media(uri);
    MediaPlayer mediaPlayer = new MediaPlayer(media);
    mediaPlayer.setAutoPlay(true);
    mediaPlayer.setCycleCount(-1);
    mediaPlayer.setMute(true);
    mediaPlayer.setOnError(() -> {
      System.out.println("Current error: " + mediaPlayer.getError());
      mediaPlayer.getError().printStackTrace();
    });
    screenPlayfield.rotateProperty().set(90);
    screenPlayfield.setMediaPlayer(mediaPlayer);
  }
}