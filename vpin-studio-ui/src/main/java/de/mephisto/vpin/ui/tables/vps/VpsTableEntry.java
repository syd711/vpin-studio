package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.vps.VpsUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsTableEntry extends HBox {

  public VpsTableEntry(@Nullable TablesSidebarController tablesController, GameRepresentation game, String tableId, String versionId, String version, List<String> authors, String link, String type, long changeDate, String update) {
    this.setAlignment(Pos.CENTER_LEFT);
    this.setStyle("-fx-padding: 3px 0 0 0;");

    Button copyBtn = new Button();
    FontIcon icon = WidgetFactory.createIcon("mdi2c-content-copy");
    icon.setIconSize(12);
    copyBtn.setGraphic(icon);
    copyBtn.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        String vpsTableUrl = VPS.getVpsTableUrl(tableId, versionId);
        javafx.scene.input.Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(vpsTableUrl);
        clipboard.setContent(content);
      }
    });

    Label versionLabel = WidgetFactory.createDefaultLabel(version);
    versionLabel.setStyle("-fx-padding: 0 0 0 3px;-fx-font-size: 14px;");
    if (!StringUtils.isEmpty(version)) {
      versionLabel.setTooltip(new Tooltip(version));
    }

    HBox versionBox = new HBox(3);
    versionBox.setPrefWidth(100);
    versionBox.setAlignment(Pos.CENTER_LEFT);

    if (Features.MANIA_ENABLED) {
      versionBox.getChildren().add(copyBtn);
    }
    versionBox.getChildren().add(versionLabel);
    this.getChildren().add(versionBox);

    Label typeLabel = new Label();
    typeLabel.setMinWidth(34);
    if (type != null) {
      typeLabel.setTextAlignment(TextAlignment.CENTER);
      typeLabel.setAlignment(Pos.CENTER);
      typeLabel.setStyle("-fx-font-weight:bold; -fx-font-size: 12px; -fx-text-fill: #FFFFFF;-fx-background-color: " + VpsUtil.getColor(type) + ";");
      typeLabel.setText(type);
    }
    this.getChildren().add(typeLabel);
    this.getChildren().add(spacer(5));


    Label authorLabel = WidgetFactory.createDefaultLabel("");
    if (authors != null && !authors.isEmpty()) {
      authorLabel.setText(String.join(", ", authors));
      authorLabel.setTooltip(new Tooltip(String.join(", ", authors)));
    }

    GameRepresentation gameByVpsTable = client.getGameService().getGameByVpsTable(tableId, versionId);
    HBox authorBox = new HBox(6);
    authorBox.setAlignment(Pos.CENTER_LEFT);
    if (gameByVpsTable != null) {
      // show all installed versions when no game is selected (case of VPS Tab), 
      // or only the selected one in sidebar
      if (game == null || game.getId() == gameByVpsTable.getId()) {
        //FontIcon checkboxIcon = WidgetFactory.createCheckboxIcon();
        //checkboxIcon.setIconSize(14);
        //checkboxIcon.setIconColor(Paint.valueOf("#66FF66"));
        //authorBox.getChildren().add(checkboxIcon);
        authorLabel.setStyle("-fx-font-weight:bold; -fx-font-size: 14px; -fx-text-fill: #66FF66;");
      }
    }
    authorBox.setPrefWidth(230);

    authorBox.getChildren().add(authorLabel);
    this.getChildren().add(authorBox);

    if (link != null) {
      String abb = VpsUtil.abbreviate(link);
      String color = VpsUtil.getColor(abb);
      Button button = new Button(abb);
      button.getStyleClass().add("vps-button");
      button.setStyle("-fx-background-color: " + color + ";");
      button.setPrefWidth(70);
      button.setTooltip(new Tooltip(link));
      button.setOnAction(event -> {
        VpsInstallerUtils.installTable(tablesController, game, link, tableId, versionId, version);
      });

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(14);
      fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      fontIcon.setIconLiteral(VpsUtil.getIconClass(abb));
      button.setGraphic(fontIcon);

      Label label = new Label();
      label.setPrefWidth(20);
      List<Node> children = new ArrayList<>();
      if (update != null) {
        FontIcon updateIcon = WidgetFactory.createUpdateIcon();
        label.setGraphic(updateIcon);
        label.setTooltip(new Tooltip("Update Available\n\n" + update));
      }
      children.add(label);

      if (abb.equals("Dropbox")) {
        children.add(button);
      }
      else if (abb.equals("Mega")) {
        children.add(spacer(5));
        button.setPrefWidth(60);
        children.add(button);
        children.add(spacer(5));
      }
      else {
        children.add(spacer(10));
        button.setPrefWidth(50);
        children.add(button);
        children.add(spacer(10));
      }

      this.getChildren().addAll(children);
    }
    else {
      Label spacer = new Label();
      spacer.setPrefWidth(90);
      this.getChildren().add(spacer);
    }


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


  public static Label spacer(int width) {
    Label spacer = new Label("");
    spacer.setPrefWidth(width);
    return spacer;
  }
}
