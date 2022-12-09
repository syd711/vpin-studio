package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ImageUtil;
import de.mephisto.vpin.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class LauncherController implements Initializable {

  @FXML
  private Label studioLabel;

  @FXML
  private Label versionLabel;

  @FXML
  private Button connectBtn;

  @FXML
  private Button refreshBtn;

  @FXML
  private Button newConnectionBtn;

  @FXML
  private TableColumn<VPinConnection, String> avatarColumn;

  @FXML
  private TableColumn<VPinConnection, String> nameColumn;

  @FXML
  private TableColumn<VPinConnection, String> hostColumn;

  @FXML
  private TableView<VPinConnection> tableView;

  private ObservableList<VPinConnection> data;

  private Stage stage;

  @FXML
  private void onConnectionRefresh() {
    refreshBtn.setDisable(true);
    connectBtn.setDisable(true);
    newConnectionBtn.setDisable(true);

    stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      VPinConnection connection = checkConnection("localhost");
      Platform.runLater(() -> {
        stage.getScene().setCursor(Cursor.DEFAULT);
        data.clear();
        if (connection != null) {
          data.add(connection);
        }

        refreshBtn.setDisable(false);
        newConnectionBtn.setDisable(false);
      });
    }).start();
  }

  @FXML
  private void onUpdateCheck() {
    stage.getScene().setCursor(Cursor.WAIT);
    new Thread(() -> {
      String s = Updater.checkForUpdate(Studio.getVersion());
      Platform.runLater(() -> {
        stage.getScene().setCursor(Cursor.DEFAULT);

        if(s == null) {
          WidgetFactory.showAlert("Unable to retrieve update information. Please check log files.");
        }
        else if(!s.equalsIgnoreCase(Studio.getVersion())) {
          WidgetFactory.showConfirmation("Download and install version " + s + "?", "Update available");
        }
      });
    }).start();
  }

  @FXML
  private void onNewConnection() {

  }


  @FXML
  private void onConnect() {
    VPinConnection selectedItem = tableView.getSelectionModel().getSelectedItem();
    VPinStudioClient client = new VPinStudioClient(selectedItem.getHost());
    if (client.version() != null) {
      stage.close();
      Studio.loadStudio(new Stage(), client);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    this.onConnectionRefresh();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tableView.setPlaceholder(new Label("                 No connections found.\n" +
        "Install the service or connect to another system."));

    connectBtn.setDisable(true);
    tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> connectBtn.setDisable(newValue == null));

    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);
    versionLabel.setText(Studio.getVersion());

    List<VPinConnection> connections = new ArrayList<>();
    data = FXCollections.observableList(connections);
    tableView.setItems(data);

    nameColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });

    hostColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      return new SimpleObjectProperty(value.getHost());
    });

    avatarColumn.setCellValueFactory(cellData -> {
      VPinConnection value = cellData.getValue();
      ImageView view = new ImageView(value.getAvatar());
      view.setPreserveRatio(true);
      view.setFitWidth(50);
      view.setFitHeight(50);
      ImageUtil.setClippedImage(view, (int) (value.getAvatar().getWidth() / 2));
      return new SimpleObjectProperty(view);
    });
  }

  private VPinConnection checkConnection(String host) {
    VPinStudioClient client = new VPinStudioClient(host);
    String version = client.version();
    if (version != null) {
      VPinConnection connection = new VPinConnection();
      PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
      PreferenceEntryRepresentation systemName = client.getPreference(PreferenceNames.SYSTEM_NAME);
      connection.setHost(host);
      connection.setName(systemName.getValue());

      if (!StringUtils.isEmpty(avatarEntry.getValue())) {
        connection.setAvatar(new Image(client.getAsset(avatarEntry.getValue())));
      }
      else {
        Image image = new Image(Studio.class.getResourceAsStream("avatar-default.png"));
        connection.setAvatar(image);
      }
      return connection;
    }
    return null;
  }
}
