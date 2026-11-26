package de.mephisto.vpin.ui.tables.vps;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Comment line below a {@link VpsEntry}.
 */
public class VpsEntryComment extends HBox {

  public VpsEntryComment(String comment) {
    this.getStyleClass().add("vps-entry-comment");
    this.setPadding(new Insets(0, 14, 10, 14));

    Label commentLabel = new Label(comment.trim());
    commentLabel.getStyleClass().add("sm-small");
    commentLabel.setWrapText(true);
    commentLabel.setTooltip(new Tooltip(comment));
    this.getChildren().add(commentLabel);
  }
}