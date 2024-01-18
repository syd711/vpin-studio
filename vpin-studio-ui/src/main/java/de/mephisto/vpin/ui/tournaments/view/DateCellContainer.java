package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.restclient.util.DateUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.DateFormat;

public class DateCellContainer extends VBox {
  private final static int TITLE_WIDTH = 80;

  public DateCellContainer(Tournament tournament) {
    super(6);

    HBox row = new HBox(6);
    row.setAlignment(Pos.CENTER_LEFT);
    Label titleLabel = new Label("Start:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    Label valueLabel = new Label(DateFormat.getDateTimeInstance().format(tournament.getStartDate()));
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    row.setAlignment(Pos.CENTER_LEFT);
    titleLabel = new Label("End:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    valueLabel = new Label(DateFormat.getDateTimeInstance().format(tournament.getEndDate()));
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    row.setAlignment(Pos.CENTER_LEFT);
    titleLabel = new Label("Remaining:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    valueLabel = new Label(DateUtil.formatDuration(tournament.getStartDate(), tournament.getEndDate()));
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);
  }
}
