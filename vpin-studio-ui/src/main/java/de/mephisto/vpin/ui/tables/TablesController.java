package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TablesController implements Initializable {

  @FXML
  private TableColumn<GameRepresentation,String> columnDisplayName;

  @FXML
  private TableColumn<GameRepresentation,String> columnFilename;

  @FXML
  private TableView tableView;

  private VPinStudioClient client;

  // Add a public no-args constructor
  public TablesController() {
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    client = new VPinStudioClient();
    final ObservableList<GameRepresentation> data = FXCollections.observableArrayList(client.getGames());

    columnDisplayName.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation,String>("gameDisplayName")
    );
    columnFilename.setCellValueFactory(
        new PropertyValueFactory<GameRepresentation,String>("gameDisplayName")
    );

    tableView.setItems(data);
  }
}