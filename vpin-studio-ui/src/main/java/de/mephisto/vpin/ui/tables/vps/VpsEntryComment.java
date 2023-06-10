package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsUtil;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VpsEntryComment extends HBox {

  public VpsEntryComment(String comment) {
    this.setAlignment(Pos.BASELINE_LEFT);
    this.setStyle("-fx-padding: 0 0 3px 0;");

    this.getChildren().add(spacer(80));

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
