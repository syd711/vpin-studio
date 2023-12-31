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
      VBox root = new VBox(3);
      root.setMinHeight(120);
      root.setMaxHeight(120);
      root.setMaxWidth(500);

      String comment = item.getComment();
      if (comment != null && !comment.trim().isEmpty()) {
        Label title = new Label(comment);
        title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
        root.getChildren().add(title);
      }

      if(item.getAuthors() != null && item.getAuthors().size() > 0) {
        String authors = String.join(", ", item.getAuthors());
        Label title = new Label(authors);
        if (comment == null || comment.trim().isEmpty()) {
          title.setStyle("-fx-text-fill: #FFFFFF;-fx-font-size : 14px;-fx-font-weight : bold;");
        }
        root.getChildren().add(title);
        if (comment == null || comment.trim().isEmpty()) {
          Label spacer = new Label();
          spacer.setStyle("-fx-font-size : 14px;");
          root.getChildren().add(spacer);
        }
      }


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

      row.setPadding(new Insets(6, 0, 6, 3));

      setGraphic(root);
    }
  }
}