package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ImageUtil;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
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
  private void onUpdateCheck() {
    VPinConnection connection = checkConnection("localhost");
    data.clear();
    if (connection != null) {
      data.add(connection);
    }
  }

  @FXML
  private void onNewConnection() {

  }


  @FXML
  private void onConnect() {
    VPinConnection selectedItem = tableView.getSelectionModel().getSelectedItem();
    VPinStudioClient client = new VPinStudioClient(selectedItem.getHost());
    if (client.ping()) {
      stage.close();
      Studio.loadStudio(new Stage(), client);
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
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

    onUpdateCheck();
  }


  static class ColorRectCell extends ListCell<VPinConnection> {
    @Override
    public void updateItem(VPinConnection item, boolean empty) {
      super.updateItem(item, empty);
      if (item != null) {

        Tile build = TileBuilder.create()
            .skinType(Tile.SkinType.IMAGE)
            .prefSize(75, 75)
            .backgroundColor(Color.TRANSPARENT)
            .image(item.getAvatar())
            .imageMask(Tile.ImageMask.ROUND)
            .textSize(Tile.TextSize.BIGGER)
            .textAlignment(TextAlignment.CENTER)
            .build();
        setGraphic(build);
        setText(item.getName());
      }
    }
  }

  private VPinConnection checkConnection(String host) {
    VPinStudioClient client = new VPinStudioClient(host);
    boolean ping = client.ping();
    if (ping) {
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
