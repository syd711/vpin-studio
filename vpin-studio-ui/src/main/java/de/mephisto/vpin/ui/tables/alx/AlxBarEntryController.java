package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
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
    String title = entry.getTitle();
    if(title.length() > 40) {
      title = title.substring(0, 39) + "...";
    }

    titleLabel.setText(title);
    valueLabel.setText(entry.getValue());


    bar.setStyle("-fx-background-color: " + entry.getColor() + ";");

    int percentage = entry.getPercentage();
    double barWidth = 400 * percentage / 100;
    if(barWidth < 1) {
      barWidth = 1;
    }

    bar.setPrefWidth(barWidth);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
