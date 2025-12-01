package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.assets.AssetRequest;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class AssetMetadataController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private VBox dataRoot;

  @FXML
  private Button closeBtn;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void setData(AssetRequest request) {
    if(request.getResult() != null) {
      dataRoot.getChildren().add(WidgetFactory.createDefaultLabel(request.getResult()));
      return;
    }

    AssetMetaData metaData = request.getMetaData();
    Set<Map.Entry<String, Object>> entries = metaData.getData().entrySet();
    for (Map.Entry<String, Object> entry : entries) {
      HBox row = new HBox(3);
      Label defaultLabel = WidgetFactory.createDefaultLabel(entry.getKey() + ":");
      defaultLabel.setPrefWidth(200);
      row.getChildren().add(defaultLabel);

      Object value = entry.getValue();
      row.getChildren().add(WidgetFactory.createDefaultLabel(String.valueOf(value)));

      dataRoot.getChildren().add(row);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  @Override
  public void onDialogCancel() {

  }
}
