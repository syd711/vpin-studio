package de.mephisto.vpin.ui.tables.alx;

import de.mephisto.vpin.restclient.alx.AlxBarEntry;
import de.mephisto.vpin.ui.tables.TableDialogs;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

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

  @FXML
  private StackPane barStack;

  public void refresh(@NonNull Stage stage, @NonNull AlxBarEntry entry) {
    String title = entry.getTitle();

    valueLabel.setText(entry.getValue());
    valueLabel.setTooltip(new Tooltip(entry.getTitle()));

    bar.setStyle("-fx-background-color: " + entry.getColor() + ";");
    CompletableFuture.supplyAsync(() -> client.getGameService().getGame(entry.getGameId()))
        .thenAcceptAsync(game -> {
          if (game != null) {
            root.setStyle("-fx-cursor: hand;");
            root.setOnMouseClicked(new EventHandler<MouseEvent>() {
              @Override
              public void handle(MouseEvent event) {
                TableDialogs.openTableDataDialog(null, game, 6);
              }
            });
          }
        }, Platform::runLater);

    int percentage = entry.getPercentage();
    double v = AlxFactory.calculateColumnWidth(stage) - 24;
    barStack.setPrefWidth(v);

    boolean trimmed = false;
    while (title.length() * 7 > v) {
      trimmed = true;
      title = title.substring(0, title.length() - 1);
    }

    if (trimmed) {
      title = title + "...";
    }
    titleLabel.setText(title);
    titleLabel.setTooltip(new Tooltip(entry.getTitle()));


    double barWidth = v * percentage / 100;
    if (barWidth < 1) {
      barWidth = 1;
    }

    bar.setPrefWidth(barWidth);
    bar.setMaxWidth(barWidth);
    bar.setMinWidth(barWidth);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }
}
