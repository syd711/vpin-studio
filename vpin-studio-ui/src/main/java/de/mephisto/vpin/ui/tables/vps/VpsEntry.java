package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.vps.VpsUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class VpsEntry extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final VpsTable table;
  private final VpsTableVersion tableVersion;
  private final GameRepresentation game;
  private final VpsDiffTypes type;
  private final String version;
  private final List<String> authors;
  private final String link;
  private final long changeDate;
  private final String update;
  private final boolean installed;
  private final boolean isFiltered;

  public VpsEntry(GameRepresentation game, VpsDiffTypes type, VpsTable table, VpsTableVersion tableVersion,  
                  String version, List<String> authors, String link,
                  long changeDate, String update, boolean installed, boolean isFiltered) {
    this.game = game;
    this.type = type;
    this.table = table;
    this.tableVersion = tableVersion;
    this.version = version;
    this.authors = authors;
    this.link = link;
    this.changeDate = changeDate;
    this.update = update;
    this.installed = installed;
    this.isFiltered = isFiltered;
    this.setAlignment(Pos.CENTER_LEFT);

    if (table != null) {
      this.setStyle("-fx-padding: 3px 0 0 0;");
    }
    else {
      this.setStyle("-fx-padding: 6px 0 0 0;");
    }
    // Version box
    HBox versionBox = new HBox(3);
    versionBox.setPrefWidth(100);
    versionBox.setAlignment(Pos.CENTER_LEFT);

    if (Features.MANIA_ENABLED && table != null && tableVersion != null) {
      Button copyBtn = new Button();
      FontIcon icon = WidgetFactory.createIcon("mdi2c-content-copy");
      icon.setIconSize(12);
      copyBtn.setGraphic(icon);
      copyBtn.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          String vpsTableUrl = VPS.getVpsTableUrl(table.getId(), tableVersion.getId());
          javafx.scene.input.Clipboard clipboard = Clipboard.getSystemClipboard();
          ClipboardContent content = new ClipboardContent();
          content.putString(vpsTableUrl);
          clipboard.setContent(content);
        }
      });
      versionBox.getChildren().add(copyBtn);
    }

    Label versionLabel = WidgetFactory.createDefaultLabel(version);
    versionLabel.setStyle("-fx-padding: 0 0 0 3px;-fx-font-size: 14px;");
    if (!StringUtils.isEmpty(version)) {
      versionLabel.setTooltip(new Tooltip(version));
    }
    versionBox.getChildren().add(versionLabel);

    this.getChildren().add(versionBox);

    // Author section
    HBox authorBox = new HBox(6);
    authorBox.setAlignment(Pos.CENTER_LEFT);
    authorBox.setPrefWidth(266);
    this.getChildren().add(authorBox);

    if (tableVersion != null) {
      Label typeLabel = new Label();
      typeLabel.setMinWidth(34);
      if (tableVersion.getTableFormat() != null) {
        typeLabel.setTextAlignment(TextAlignment.CENTER);
        typeLabel.setAlignment(Pos.CENTER);
        typeLabel.setStyle("-fx-font-weight:bold; -fx-font-size: 12px; -fx-text-fill: #FFFFFF;-fx-background-color: " + VpsUtil.getColor(tableVersion.getTableFormat()) + ";");
        typeLabel.setText(tableVersion.getTableFormat());
      }
      authorBox.getChildren().add(typeLabel);
    }

    if (authors != null && !authors.isEmpty()) {
      if (installed) {
        Hyperlink hyperlink = new Hyperlink(String.join(", ", authors));
        hyperlink.setTooltip(new Tooltip(String.join(", ", authors)));
        hyperlink.setStyle("-fx-font-weight:bold; -fx-font-size: 14px; -fx-text-fill: #66FF66;");
        hyperlink.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent event) {
            NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId()));
          }
        });
        authorBox.getChildren().add(hyperlink);
      }
      else {
        authorBox.getChildren().add(spacer(1));
        Label authorLabel = WidgetFactory.createDefaultLabel("");
        authorLabel.setText(String.join(", ", authors));
        authorLabel.setTooltip(new Tooltip(String.join(", ", authors)));
        authorBox.getChildren().add(authorLabel);
      }
    }

    if (link != null) {
      String abb = VpsUtil.abbreviate(link);
      String color = VpsUtil.getColor(abb);
      Button button = new Button(abb);
      button.getStyleClass().add("vps-button");
      button.setStyle("-fx-background-color: " + color + ";");
      button.setPrefWidth(70);
      button.setTooltip(new Tooltip(link));
      button.setOnAction(event -> {
        Studio.browse(link);
      });

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(14);
      fontIcon.setIconColor(Paint.valueOf("#FFFFFF"));
      fontIcon.setIconLiteral(VpsUtil.getIconClass(abb));
      button.setGraphic(fontIcon);

      // add context menu on VPS button
      ContextMenu menu = new ContextMenu();
      MenuItem vpsItem = new MenuItem("Open Link");
      vpsItem.setOnAction(actionEvent -> Studio.browse(link));
      menu.getItems().add(vpsItem);
      MenuItem copyItem = new MenuItem("Copy Link");
      copyItem.setOnAction(actionEvent -> {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(link);
        clipboard.setContent(content);
      });
      menu.getItems().add(copyItem);

      if (Features.AUTO_INSTALLER) {
        MenuItem installItem = new MenuItem("Install...");
        installItem.setOnAction(actionEvent -> {
          boolean isInstalled = VpsInstallerUtils.installOrBrowse(game, link, VpsDiffTypes.tableNewVersionVPX);
          if (isInstalled && game != null && table != null && tableVersion != null) {
            // If table has been installed, auto link it to VPS entry and force fix version
            try {
              client.getFrontendService().saveVpsMapping(game.getId(), table.getId(), tableVersion.getId());
              client.getFrontendService().fixVersion(game.getId(), version);
            }
            catch (Exception e) {
              LOG.error("Cannot link table to VPS or fix version, it has to be done manually : " + e.getMessage());
            }
          }
        });
        menu.getItems().add(installItem);
      }

      if (game != null) {
        MenuItem addTodoItem = new MenuItem("Add //TODO Link");
        addTodoItem.setOnAction(actionEvent -> {
          String notes = StringUtils.defaultString(game.getComment());
          if (notes.length() > 0) {
            notes = notes + "\n";
          }
          notes += "//TODO " + link;
          game.setComment(notes);
          client.getGameService().saveGame(game);
          EventManager.getInstance().notifyTableChange(game.getId(), null);
        });
        menu.getItems().add(addTodoItem);
      }
      else if (table != null) {
        MenuItem addTodoItem = new MenuItem("Add //TODO Link");
        addTodoItem.setOnAction(actionEvent -> {
          String notes = StringUtils.defaultString(table.getComment());
          if (notes.length() > 0) {
            notes = notes + "\n";
          }
          notes += "//TODO " + link;
          table.setComment(notes);
          client.getVpsService().saveVpsData(table);
          EventManager.getInstance().notifyVpsTableChange(table.getId());
        });  
        menu.getItems().add(addTodoItem);
      }
      button.setContextMenu(menu);

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

    // Add change date at the end
    Label changedLabel = WidgetFactory.createDefaultLabel(DateFormat.getDateInstance().format(new Date(changeDate)));
    changedLabel.setPrefWidth(100);
    changedLabel.setStyle("-fx-padding: 0 3px 0 0;-fx-font-size: 14px;");
    changedLabel.setAlignment(Pos.CENTER_RIGHT);
    changedLabel.setContentDisplay(ContentDisplay.RIGHT);
    if (changeDate == 0) {
      changedLabel.setText("");
    }
    this.getChildren().add(changedLabel);

    this.setDisable(!isFiltered);
  }

  public static Label spacer(int width) {
    Label spacer = new Label("");
    spacer.setPrefWidth(width);
    return spacer;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    VpsEntry vpsEntry = (VpsEntry) object;
    return changeDate == vpsEntry.changeDate && installed == vpsEntry.installed && isFiltered == vpsEntry.isFiltered
        && Objects.equals(table != null ? table.getId() : null, vpsEntry.table != null ? vpsEntry.table.getId() : null) 
        && Objects.equals(tableVersion != null ? tableVersion.getId() : null, vpsEntry.tableVersion != null ? vpsEntry.tableVersion.getId() : null) 
        && Objects.equals(tableVersion != null ? tableVersion.getTableFormat() : null, vpsEntry.tableVersion != null ? vpsEntry.tableVersion.getTableFormat() : null) 
        && Objects.equals(game, vpsEntry.game) && type == vpsEntry.type && Objects.equals(version, vpsEntry.version) 
        && Objects.equals(authors, vpsEntry.authors) && Objects.equals(link, vpsEntry.link) && Objects.equals(update, vpsEntry.update);
  }

  @Override
  public int hashCode() {
    return Objects.hash(table != null ? table.getId() : null, tableVersion != null ? tableVersion.getId() : null, 
                        game, type, tableVersion != null ? tableVersion.getTableFormat() : null, version, authors, link, 
                        changeDate, update, installed, isFiltered);
  }
}
