package de.mephisto.vpin.ui.mania.widgets;

import de.mephisto.vpin.commons.fx.widgets.WidgetController;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.mania.TarcisioWheelsDB;
import de.mephisto.vpin.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class ManiaWidgetVPSTableController extends WidgetController implements Initializable {
  private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy / hh:mm");

  @FXML
  private BorderPane root;

  @FXML
  private VBox highscoreVBox;

  @FXML
  private ImageView wheelImageView;

  @FXML
  private Label tableLabel;

  @FXML
  private Label installedLabel;

  @FXML
  private Label nameLabel;
  private ManiaWidgetVPSTablesController vpsTablesController;
  private VpsTable vpsTable;

  // Add a public no-args constructor
  public ManiaWidgetVPSTableController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    installedLabel.setVisible(false);
  }

  @FXML
  private void onSelect() {
    this.vpsTablesController.selectTable(this.vpsTable);
  }

  public void setData(VpsTable vpsTable) {
    this.vpsTable = vpsTable;
    InputStream imageInput = TarcisioWheelsDB.getWheelImage(Studio.class, client, vpsTable.getId());
    Image image = new Image(imageInput);
    wheelImageView.setImage(image);

    tableLabel.setText(vpsTable.getName());

    String result = "";
    if (vpsTable.getManufacturer() != null && vpsTable.getManufacturer().trim().length() > 0) {
      result = result + vpsTable.getManufacturer();
    }

    if (vpsTable.getYear() > 0) {
      result = result + " (" + vpsTable.getYear() + ")";
    }
    nameLabel.setText(result);


    GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(vpsTable.getId(), null);
    installedLabel.setVisible(gameByVpsTable != null);
  }

  public void setTablesController(ManiaWidgetVPSTablesController vpsTablesController) {
    this.vpsTablesController = vpsTablesController;
  }
}