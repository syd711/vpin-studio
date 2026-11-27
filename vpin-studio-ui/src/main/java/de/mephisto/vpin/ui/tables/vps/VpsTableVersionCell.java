package de.mephisto.vpin.ui.tables.vps;


import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.ui.vps.VpsUtil;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

/**
 * Cell of the VPS table version combo boxes (table data manager, tables sidebar).
 * The popup rows show version, authors, update date, features and comment like a VPS download entry,
 * the button cell is a single line (version + authors + date) that fits into the 30px combo box.
 */
public class VpsTableVersionCell extends ListCell<VpsTableVersion> {

  private final boolean compact;

  /**
   * Popup cell (full entry)
   */
  public VpsTableVersionCell() {
    this(false);
  }

  /**
   * @param compact true for the button cell of the combo box
   */
  public VpsTableVersionCell(boolean compact) {
    this.compact = compact;
  }

  @Override
  protected void updateItem(VpsTableVersion item, boolean empty) {
    super.updateItem(item, empty);
    setGraphic(null);
    setText(null);
    if (item != null && !empty) {
      setGraphic(compact ? createCompact(item) : createEntry(item));
    }
  }

  private HBox createCompact(VpsTableVersion item) {
    HBox row = new HBox(8);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setMinWidth(0);

    row.getChildren().add(createVersionLabel(item));

    Label authors = createAuthorsLabel(item);
    if (authors != null) {
      row.getChildren().add(authors);
    }

    Label date = new Label(formatDate(item));
    date.getStyleClass().add("sm-small");
    date.setMinWidth(Label.USE_PREF_SIZE);
    row.getChildren().add(date);
    return row;
  }

  private VBox createEntry(VpsTableVersion item) {
    VBox entry = new VBox(4);
    entry.setMinWidth(0);
    entry.setMaxWidth(480);

    HBox line = new HBox(8);
    line.setAlignment(Pos.CENTER_LEFT);
    line.getChildren().add(createVersionLabel(item));
    Label authors = createAuthorsLabel(item);
    if (authors != null) {
      line.getChildren().add(authors);
    }
    entry.getChildren().add(line);

    HBox infoLine = new HBox(6);
    infoLine.setAlignment(Pos.CENTER_LEFT);
    Label date = new Label(Messages.get("tables.section_vps.updated", formatDate(item)));
    date.getStyleClass().add("sm-small");
    date.setMinWidth(Label.USE_PREF_SIZE);
    infoLine.getChildren().add(date);

    List<String> features = item.getFeatures();
    if (features != null) {
      for (String feature : features) {
        Label badge = new Label(feature);
        badge.getStyleClass().addAll("sm-pill", "sm-pill-ok");
        badge.setMinWidth(Label.USE_PREF_SIZE);
        badge.setTooltip(new Tooltip(VpsUtil.getFeatureColorTooltip(feature)));
        infoLine.getChildren().add(badge);
      }
    }
    entry.getChildren().add(infoLine);

    String comment = item.getComment();
    if (!StringUtils.isBlank(comment)) {
      Label commentLabel = new Label(comment.trim());
      commentLabel.getStyleClass().add("sm-small");
      commentLabel.setWrapText(true);
      entry.getChildren().add(commentLabel);
    }
    return entry;
  }

  private static Label createVersionLabel(VpsTableVersion item) {
    String version = StringUtils.defaultIfBlank(item.getVersion(), "-");
    Label label = new Label(version);
    label.getStyleClass().add("vps-entry-version");
    label.setMinWidth(Label.USE_PREF_SIZE);
    return label;
  }

  private static Label createAuthorsLabel(VpsTableVersion item) {
    if (item.getAuthors() == null || item.getAuthors().isEmpty()) {
      return null;
    }
    String authors = String.join(", ", item.getAuthors());
    Label label = new Label(authors);
    label.getStyleClass().add("sm-text");
    label.setTextOverrun(OverrunStyle.ELLIPSIS);
    label.setMinWidth(0);
    label.setMaxWidth(Double.MAX_VALUE);
    label.setTooltip(new Tooltip(authors));
    HBox.setHgrow(label, Priority.ALWAYS);
    return label;
  }

  private static String formatDate(VpsTableVersion item) {
    if (item.getCreatedAt() == null || item.getCreatedAt() <= 0) {
      return Messages.get("tables.section_vps.unknown");
    }
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(item.getCreatedAt()));
  }
}
