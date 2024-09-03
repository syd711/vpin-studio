package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AlxTileEntryController implements Initializable {

  @FXML
  private VBox root;

  @FXML
  private Label titleLabel;

  @FXML
  private Label descriptionLabel;

  @FXML
  private Label valueLabel;

  public void refresh(@NonNull Stage stage, @NonNull AlxTileEntry entry) {
    double v = AlxFactory.calculateColumnWidth(stage);
    root.setPrefWidth(v - 24);

    titleLabel.setText(entry.getTitle());
    valueLabel.setText(entry.getValue());
    descriptionLabel.setText(entry.getDescription());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
