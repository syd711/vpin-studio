package de.mephisto.vpin.ui.vps;

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
    super(3);

    StringBuilder builder = new StringBuilder();
    String comment = item.getComment();
    if (comment != null && comment.trim().length() > 0) {
      builder.append(comment);
    }
    else {
      String authors = String.join(", ", item.getAuthors());
      if (authors.length() > 40) {
        authors = authors.substring(0, 39) + "...";
      }
      builder.append(authors);
    }

    Label title = new Label(builder.toString());
    title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
    this.getChildren().add(title);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Version:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
    Label valueLabel = new Label(item.getVersion());
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
    row.getChildren().addAll(titleLabel, valueLabel);

    this.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Updated At:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
    valueLabel = new Label(DateFormat.getDateInstance().format(new Date(item.getUpdatedAt())));
    valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
    row.getChildren().addAll(titleLabel, valueLabel);
    this.getChildren().add(row);

    row = new HBox(6);
    List<String> features = item.getFeatures();
    if (features != null) {
      for (String feature : features) {
        Label badge = new Label(feature);
        badge.setTooltip(new Tooltip(feature));
        badge.getStyleClass().add("vps-badge");
        badge.setStyle("-fx-background-color: " + VpsUtil.getFeatureColor(feature) + ";");
        row.getChildren().add(badge);
      }
    }
    this.getChildren().add(row);

    row.setPadding(new Insets(3, 0,  6, 0));
  }
}
