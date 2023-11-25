package de.mephisto.vpin.ui.components;

import de.mephisto.vpin.restclient.components.AlxBarEntry;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class AlxBarEntryController implements Initializable {

  @FXML
  private Label titleLabel;

  @FXML
  private Label valueLabel;

  @FXML
  private HBox bar;

  @FXML
  private HBox parent;

  public void refresh(@NonNull AlxBarEntry entry) {
    titleLabel.setText(entry.getTitle());
    valueLabel.setText(entry.getValue());

    bar.setStyle("-fx-background-color: " + entry.getColor() + ";");

    int percentage = entry.getPercentage();
    double width = 560 * percentage / 100;
    bar.setPrefWidth(width);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
