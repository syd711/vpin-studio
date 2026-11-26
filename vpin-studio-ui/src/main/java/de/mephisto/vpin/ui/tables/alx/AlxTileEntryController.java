package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxTileEntry;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

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

  /**
   * Fills the tile and sizes it to a column of the given stage (used by dialogs with a fixed column layout).
   */
  public void refresh(@NonNull Stage stage, @NonNull AlxTileEntry entry) {
    double v = AlxFactory.calculateColumnWidth(stage);
    root.setPrefWidth(v - 24);
    refresh(entry);
  }

  /**
   * Fills the tile, the width is given by the parent.
   */
  public void refresh(@NonNull AlxTileEntry entry) {
    titleLabel.setText(entry.getTitle());
    valueLabel.setText(entry.getValue());
    descriptionLabel.setText(entry.getDescription());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
