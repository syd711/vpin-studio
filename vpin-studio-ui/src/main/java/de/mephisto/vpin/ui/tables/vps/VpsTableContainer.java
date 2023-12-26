package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class VpsTableContainer extends VBox {
  private final static int TITLE_WIDTH = 100;

  public VpsTableContainer(VpsTable item) {
    super(3);

    String name = item.getName();
    if (name.length() > 40) {
      name = name.substring(0, 39) + "...";
    }

    Label title = new Label(name);
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
    this.getChildren().add(title);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Year:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
    Label valueLabel = new Label(String.valueOf(item.getYear()));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
    row.getChildren().addAll(titleLabel, valueLabel);

    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Manufacturer:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
    valueLabel = new Label(item.getManufacturer());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Type:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
    valueLabel = new Label(item.getType());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0,  6, 0));
  }
}
