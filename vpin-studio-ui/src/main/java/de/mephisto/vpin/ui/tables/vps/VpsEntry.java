package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class VpsEntry extends VBox {
  private final VpsTable table;
  private final VpsTableVersion tableVersion;
  private final GameRepresentation game;
  private final VpsDiffTypes type;
  private final String version;
  private final List<String> authors;
  private final String link;
  private final Long changeDate;
  private final String update;
  private final boolean installed;
  private final boolean isFiltered;

  public VpsEntry(GameRepresentation game, VpsDiffTypes type, VpsTable table, VpsTableVersion tableVersion,
                  String version, List<String> authors, String link,
                  Long changeDate, String update, boolean installed, boolean isFiltered) {
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
    this.getStyleClass().add("vps-entry");
    this.setSpacing(4);

    // first line: version, authors, table format, update marker, link pill
    HBox line = new HBox(8);
    line.setAlignment(Pos.CENTER_LEFT);

    if (Features.MANIA_ENABLED && table != null && tableVersion != null) {
      Button copyBtn = new Button();
      copyBtn.getStyleClass().addAll("sm-button", "sm-button-small");
      FontIcon icon = WidgetFactory.createIcon("mdi2c-content-copy");
      icon.setIconSize(12);
      copyBtn.setGraphic(icon);
      copyBtn.setTooltip(new Tooltip(Messages.get("vps.vps_tables_sidebar.copy_vps_link")));
      copyBtn.setOnAction(event -> copy(VPS.getVpsTableUrl(table.getId(), tableVersion.getId())));
      line.getChildren().add(copyBtn);
    }

    if (!StringUtils.isEmpty(version)) {
      Label versionLabel = new Label(version);
      versionLabel.getStyleClass().add("vps-entry-version");
      versionLabel.setMinWidth(Region.USE_PREF_SIZE);
      versionLabel.setMaxWidth(110);
      versionLabel.setTooltip(new Tooltip(version));
      line.getChildren().add(versionLabel);
    }

    if (authors != null && !authors.isEmpty()) {
      String authorText = String.join(", ", authors);
      if (installed && game != null) {
        Hyperlink hyperlink = new Hyperlink(authorText);
        hyperlink.getStyleClass().add("vps-entry-installed");
        hyperlink.setTooltip(new Tooltip(authorText));
        hyperlink.setOnAction(event -> NavigationController.navigateTo(NavigationItem.Tables, new NavigationOptions(game.getId())));
        hyperlink.setMinWidth(0);
        line.getChildren().add(hyperlink);
      }
      else {
        Label authorLabel = new Label(authorText);
        authorLabel.getStyleClass().add("sm-text");
        authorLabel.setTooltip(new Tooltip(authorText));
        authorLabel.setMinWidth(0);
        line.getChildren().add(authorLabel);
      }
    }

    Region spacer = new Region();
    spacer.setMinWidth(4);
    HBox.setHgrow(spacer, Priority.ALWAYS);
    line.getChildren().add(spacer);

    if (tableVersion != null && tableVersion.getTableFormat() != null) {
      Label typeLabel = new Label(tableVersion.getTableFormat());
      typeLabel.getStyleClass().addAll("sm-pill", "sm-pill-unknown");
      typeLabel.setMinWidth(Region.USE_PREF_SIZE);
      line.getChildren().add(typeLabel);
    }

    if (update != null) {
      Label updateLabel = new Label();
      updateLabel.setGraphic(WidgetFactory.createUpdateIcon());
      updateLabel.setTooltip(new Tooltip(Messages.get("dialog.update_available_details", update)));
      updateLabel.setMinWidth(Region.USE_PREF_SIZE);
      line.getChildren().add(updateLabel);
    }

    if (link != null) {
      line.getChildren().add(createLinkPill(link));
    }

    this.getChildren().add(line);

    // second line: change date
    String date = Messages.get("tables.section_vps.unknown");
    if (changeDate != null) {
      date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(Instant.ofEpochMilli(changeDate).atZone(ZoneId.systemDefault()).toLocalDate());
    }
    Label changedLabel = new Label(Messages.get("tables.section_vps.updated", date));
    changedLabel.getStyleClass().add("sm-small");
    this.getChildren().add(changedLabel);

    this.setDisable(!isFiltered);
  }

  private Button createLinkPill(String link) {
    String abb = VpsUtil.abbreviate(link);

    Circle dot = new Circle(3, Color.web(VpsUtil.getColor(abb)));
    Label text = new Label(abb);
    text.getStyleClass().add("vps-link-pill-text");
    FontIcon arrow = new FontIcon("mdi2a-arrow-top-right");
    arrow.setIconSize(14);
    arrow.getStyleClass().add("c-icon-muted");
    HBox content = new HBox(6, dot, text, arrow);
    content.setAlignment(Pos.CENTER_LEFT);

    Button button = new Button();
    button.setGraphic(content);
    button.getStyleClass().add("vps-link-pill");
    button.setMinWidth(Region.USE_PREF_SIZE);
    button.setTooltip(new Tooltip(link));
    button.setOnAction(event -> Studio.browse(link));

    ContextMenu menu = new ContextMenu();
    MenuItem vpsItem = new MenuItem(Messages.get("dialog.vps_open_link"));
    vpsItem.setOnAction(actionEvent -> Studio.browse(link));
    menu.getItems().add(vpsItem);
    MenuItem copyItem = new MenuItem(Messages.get("dialog.vps_copy_link"));
    copyItem.setOnAction(actionEvent -> copy(link));
    menu.getItems().add(copyItem);

    if (game != null) {
      MenuItem addTodoItem = new MenuItem(Messages.get("dialog.vps_add_todo_link"));
      addTodoItem.setOnAction(actionEvent -> {
        game.setComment(appendTodo(game.getComment(), link));
        client.getGameService().saveGame(game);
        EventManager.getInstance().notifyTableChange(game.getId(), null);
      });
      menu.getItems().add(addTodoItem);
    }
    else if (table != null) {
      MenuItem addTodoItem = new MenuItem(Messages.get("dialog.vps_add_todo_link"));
      addTodoItem.setOnAction(actionEvent -> {
        table.setComment(appendTodo(table.getComment(), link));
        client.getVpsService().saveVpsData(table);
        EventManager.getInstance().notifyVpsTableChange(table.getId());
      });
      menu.getItems().add(addTodoItem);
    }
    button.setContextMenu(menu);
    return button;
  }

  private static String appendTodo(String comment, String link) {
    String notes = StringUtils.defaultString(comment);
    if (!notes.isEmpty()) {
      notes = notes + "\n";
    }
    return notes + "//TODO " + link;
  }

  private static void copy(String text) {
    ClipboardContent content = new ClipboardContent();
    content.putString(text);
    Clipboard.getSystemClipboard().setContent(content);
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
