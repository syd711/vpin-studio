package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsLinkUtil;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URI;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class VpsEntry extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsEntry.class);

  public VpsEntry(String version, List<String> authors, String link, long changeDate) {
    this.setAlignment(Pos.BASELINE_LEFT);
    this.setStyle("-fx-padding: 0 0 3px 0;");
    Label versionLabel = WidgetFactory.createDefaultLabel(version);
    versionLabel.setStyle("-fx-padding: 0 0 0 3px;-fx-font-size: 14px;");
    versionLabel.setPrefWidth(80);
    versionLabel.setTooltip(new Tooltip(version));

    this.getChildren().add(versionLabel);

    Label authorLabel = WidgetFactory.createDefaultLabel("");
    if (authors != null) {
      authorLabel.setText(String.join(", ", authors));
    }
    authorLabel.setTooltip(new Tooltip(String.join(", ", authors)));

    authorLabel.setPrefWidth(326);
    this.getChildren().add(authorLabel);

    String abb = VpsLinkUtil.abbreviate(link);
    Button button = new Button(abb);
    button.setPrefWidth(50);
    button.setTooltip(new Tooltip(link));
    button.setOnAction(event -> {
      Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
      if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          desktop.browse(new URI(link));
        } catch (Exception e) {
          LOG.error("Failed to open link: " + e.getMessage());
        }
      }
    });
    this.getChildren().add(button);

    Label changedLabel = WidgetFactory.createDefaultLabel(DateFormat.getDateInstance().format(new Date(changeDate)));
    changedLabel.setPrefWidth(100);
    changedLabel.setStyle("-fx-padding: 0 3px 0 0;-fx-font-size: 14px;");
    changedLabel.setAlignment(Pos.BASELINE_RIGHT);
    changedLabel.setContentDisplay(ContentDisplay.RIGHT);
    if (changeDate == 0) {
      changedLabel.setText("");
    }
    this.getChildren().add(changedLabel);
  }
}
