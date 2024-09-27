package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class VpsEntryComment extends HBox {

  public VpsEntryComment(String comment) {
    this.setAlignment(Pos.BASELINE_LEFT);
    this.setStyle("-fx-padding: 0 0 3px 0;");

    this.getChildren().add(spacer(100));

    Label commentLabel = WidgetFactory.createDefaultLabel(comment.trim());
    commentLabel.setStyle("-fx-font-size: 12px;-fx-font-color: #B0ABAB;-fx-text-fill: #B0ABAB;");
    commentLabel.setPrefWidth(306);
    commentLabel.setTooltip(new Tooltip(comment));
    this.getChildren().add(commentLabel);


    this.getChildren().add(spacer(70));
    this.getChildren().add(spacer(100));
  }

  public static Label spacer(int width) {
    Label spacer = new Label("");
    spacer.setPrefWidth(width);
    return spacer;
  }
}
