package de.mephisto.vpin.ui.vps.containers;

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

public class VpsVersionContainer extends VBox {
  private final static int TITLE_WIDTH = 100;

  public VpsVersionContainer(VpsTableVersion item) {
    this(item, "");
  }

  public VpsVersionContainer(VpsTableVersion item, String customStyle) {
    super(3);

    setMinHeight(120);
    setMaxHeight(120);
    setMaxWidth(500);

    String comment = item.getComment();
    if (comment != null && !comment.trim().isEmpty()) {
      Label title = new Label(comment);
      title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
      this.getChildren().add(title);
    }

    if(item.getAuthors() != null && item.getAuthors().size() > 0) {
      String authors = String.join(", ", item.getAuthors());
      Label title = new Label(authors);
      title.getStyleClass().add("default-text");
      title.setStyle(customStyle);
      if (comment == null || comment.trim().isEmpty()) {
        title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;" + customStyle);
      }
      this.getChildren().add(title);
      if (comment == null || comment.trim().isEmpty()) {
        Label spacer = new Label();
        spacer.setStyle("-fx-font-size : 14px;");
        this.getChildren().add(spacer);
      }
    }

    HBox row = new HBox(6);
    Label titleLabel = new Label("Version:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    Label valueLabel = new Label(item.getVersion());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);

    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Updated At:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;" + customStyle);
    valueLabel = new Label(DateFormat.getDateInstance().format(new Date(item.getUpdatedAt())));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;" + customStyle);
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    List<String> features = item.getFeatures();
    if (features != null) {
      for (String feature : features) {
        Label badge = new Label(feature);
        badge.getStyleClass().add("white-label");
        badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
        badge.getStyleClass().add("vps-badge");
        badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";" + customStyle);
        row.getChildren().add(badge);
      }
    }
    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0,  6, 0));
  }
}
