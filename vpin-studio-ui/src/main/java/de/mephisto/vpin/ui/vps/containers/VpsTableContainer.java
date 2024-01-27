package de.mephisto.vpin.ui.vps.containers;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;

public class VpsTableContainer extends VBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTableContainer.class);
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

    row = new HBox(6);
    Button button = new Button("Download Table");

    button.setOnAction(event -> {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(VPS.getVpsTableUrl(item.getId())));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    });

    FontIcon icon = new FontIcon("mdi2o-open-in-new");
    icon.setIconSize(8);
    icon.setIconColor(Paint.valueOf("#FFFFFF"));
    button.setGraphic(icon);
    button.setStyle("-fx-font-size: 12px;");
    button.getStyleClass().add("external-component");

    titleLabel = new Label("");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    row.getChildren().addAll(titleLabel, button);


    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0, 6, 0));
  }
}
