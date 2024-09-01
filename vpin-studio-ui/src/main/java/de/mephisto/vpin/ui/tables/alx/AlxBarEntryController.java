package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableDialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxBarEntryController implements Initializable {

  @FXML
  private Label titleLabel;

  @FXML
  private Label valueLabel;

  @FXML
  private HBox bar;

  @FXML
  private BorderPane root;

  public void refresh(@NonNull AlxBarEntry entry) {
    String title = entry.getTitle();
    if (title.length() > 40) {
      title = title.substring(0, 39) + "...";
    }

    titleLabel.setText(title);
    valueLabel.setText(entry.getValue());

    bar.setStyle("-fx-background-color: " + entry.getColor() + ";");
    GameRepresentation game = client.getGameService().getGame(entry.getGameId());
    if (game != null) {
      root.setStyle("-fx-cursor: hand;");
      root.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          TableDialogs.openTableDataDialog(null, game, 6);
        }
      });
    }

    int percentage = entry.getPercentage();
    double v = AlxFactory.calculateColumnWidth() - 24;
    double barWidth = v * percentage / 100;
    if (barWidth < 1) {
      barWidth = 1;
    }

    bar.setPrefWidth(barWidth);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
