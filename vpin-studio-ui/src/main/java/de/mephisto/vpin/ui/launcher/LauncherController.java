package de.mephisto.vpin.ui.launcher;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.Studio;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
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
  private ListView<VPinConnection> listView;
  private ObservableList<VPinConnection> data;


  @FXML
  private void onUpdateCheck() {
    VPinConnection connection = checkConnection("localhost");
    if (connection != null) {
      data.add(connection);
    }
  }

  @FXML
  private void onNewConnection() {

  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Font font = Font.font("Impact", FontPosture.findByName("regular"), 28);
    studioLabel.setFont(font);

    versionLabel.setText("bubu");

    listView.setCellFactory(list -> new ColorRectCell());
    List<VPinConnection> connections = new ArrayList<>();
    data = FXCollections.observableList(connections);
    listView.setItems(data);
  }

  static class ColorRectCell extends ListCell<VPinConnection> {
    @Override
    public void updateItem(VPinConnection item, boolean empty) {
      super.updateItem(item, empty);
      if (item != null) {

        Tile build = TileBuilder.create()
            .skinType(Tile.SkinType.IMAGE)
            .prefSize(100, 100)
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
