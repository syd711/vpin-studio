package de.mephisto.vpin.ui.tables.vps;


import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.connectors.vps.model.VpsUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class VpsTableVersionCell extends ListCell<VpsTableVersion> {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTableVersionCell.class);
  private final static int TITLE_WIDTH = 100;

  public VpsTableVersionCell() {

  }

  protected void updateItem(VpsTableVersion item, boolean empty) {
    super.updateItem(item, empty);
    setGraphic(null);
    setText(null);
    if (item != null) {
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

      VBox root = new VBox(3);
      Label title = new Label(builder.toString());
      title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
      root.getChildren().add(title);

      HBox row = new HBox(6);
      Label titleLabel = new Label("Version:");
      titleLabel.setPrefWidth(TITLE_WIDTH);
      titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
      Label valueLabel = new Label(item.getVersion());
      valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
      row.getChildren().addAll(titleLabel, valueLabel);
      root.getChildren().add(row);

      row = new HBox(6);
      titleLabel = new Label("Updated At:");
      titleLabel.setPrefWidth(TITLE_WIDTH);
      titleLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;-fx-font-weight : bold;");
      valueLabel = new Label(DateFormat.getDateInstance().format(new Date(item.getUpdatedAt())));
      valueLabel.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 12px;");
      row.getChildren().addAll(titleLabel, valueLabel);
      root.getChildren().add(row);

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
      root.getChildren().add(row);

      row.setPadding(new Insets(6, 0,  6, 3));

      setGraphic(root);
    }
  }
}