package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
import de.mephisto.vpin.ui.tables.TableDialogs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import static de.mephisto.vpin.ui.Studio.client;

/**
 * One row of a Table Statistics column: table name + value and a rounded bar whose width is
 * the percentage of the column width. A negative percentage shows the value as hint below the name instead.
 */
public class AlxBarEntryController implements Initializable {

  @FXML
  private VBox root;

  @FXML
  private Label titleLabel;

  @FXML
  private Label valueLabel;

  @FXML
  private Label subLabel;

  @FXML
  private StackPane barStack;

  @FXML
  private Region bar;

  public void refresh(@NonNull AlxBarEntry entry) {
    titleLabel.setText(entry.getTitle());
    titleLabel.setTooltip(new Tooltip(entry.getTitle()));

    boolean showBar = entry.getPercentage() >= 0;
    Label target = showBar ? valueLabel : subLabel;
    target.setText(entry.getValue());
    target.setTooltip(new Tooltip(entry.getTitle()));

    valueLabel.setVisible(showBar);
    valueLabel.setManaged(showBar);
    subLabel.setVisible(!showBar);
    subLabel.setManaged(!showBar);
    barStack.setVisible(showBar);
    barStack.setManaged(showBar);

    if (showBar) {
      bar.setStyle("-fx-background-color: " + entry.getColor() + ";");
      double fraction = Math.max(0, Math.min(1, entry.getPercentage() / 100d));
      // at least a dot so that small values stay visible
      bar.minWidthProperty().bind(barStack.widthProperty().map(w -> Math.max(8, w.doubleValue() * fraction)));
      bar.prefWidthProperty().bind(bar.minWidthProperty());
      bar.maxWidthProperty().bind(bar.minWidthProperty());
    }

    CompletableFuture.supplyAsync(() -> client.getGameService().getGame(entry.getGameId()))
        .thenAcceptAsync(game -> {
          if (game != null) {
            root.getStyleClass().add("sm-list-row");
            root.setOnMouseClicked(event -> TableDialogs.openTableDataDialog(null, game, 6));
          }
        }, Platform::runLater);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }
}
