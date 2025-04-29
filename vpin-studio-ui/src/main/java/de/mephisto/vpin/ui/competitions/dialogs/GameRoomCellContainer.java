package de.mephisto.vpin.ui.competitions.dialogs;

import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GameRoomCellContainer extends HBox {
  private final static int TITLE_WIDTH = 140;

  public GameRoomCellContainer(GameRoom gameRoom, IScoredGame game, String customStyle) {
    super(3);

    String gameName = game.getName();
    String name = gameRoom.getSettings().getRoomName();
//    if (name.length() > 40) {
//      name = name.substring(0, 39) + "...";
//    }

    VBox column = new VBox(3);
    this.getChildren().add(column);

    Label title = new Label(gameName);
    title.setTooltip(new Tooltip(gameName));
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
    column.getChildren().add(title);

    HBox row = new HBox(6);
    Label titleLabel = new Label(name);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    row.getChildren().add(titleLabel);
    column.getChildren().add(row);

    boolean publicScoreEnabled = gameRoom.getSettings().isPublicScoreEnteringEnabled();
    row = new HBox(6);
    titleLabel = new Label("Public Scores Enabled:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    Label valueLabel = new Label(String.valueOf(publicScoreEnabled));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    column.getChildren().add(row);


    boolean longNamesEnabled = gameRoom.getSettings().isLongNameInputEnabled();
    row = new HBox(6);
    titleLabel = new Label("Long Names Enabled:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    valueLabel = new Label(String.valueOf(longNamesEnabled));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    column.getChildren().add(row);


    Hyperlink hyperlink = new Hyperlink(gameRoom.getUrl());
    hyperlink.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    hyperlink.setOnAction(event -> {
      Studio.browse(gameRoom.getUrl());
    });
    column.getChildren().add(hyperlink);
    setPadding(new Insets(3, 0, 6, 0));
  }
}
