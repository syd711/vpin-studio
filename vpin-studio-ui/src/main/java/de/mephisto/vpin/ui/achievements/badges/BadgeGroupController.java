package de.mephisto.vpin.ui.achievements.badges;

import de.mephisto.vpin.ui.util.ThemeColors;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.function.Consumer;

import static de.mephisto.vpin.ui.achievements.AchievementsUI.*;

/**
 * One badge category section: a localized title and a 4-column grid of badge cards.
 */
public class BadgeGroupController {

  private static final int COLUMNS = 4;

  @FXML
  private VBox root;
  @FXML
  private Label titleLabel;
  @FXML
  private GridPane grid;

  public static BadgeGroupController load() {
    try {
      FXMLLoader loader = new FXMLLoader(BadgeGroupController.class.getResource("badge-group.fxml"));
      loader.setResources(Messages.getBundle());
      loader.load();
      return loader.getController();
    }
    catch (IOException e) {
      throw new UncheckedIOException("Failed to load badge-group.fxml", e);
    }
  }

  public VBox getRoot() {
    return root;
  }

  public void render(String category, List<BadgeInfo> badges, String selectedId, Consumer<BadgeInfo> onClick) {
    titleLabel.setText(categoryLabel(category).toUpperCase(Messages.getLocale()));
    grid.getChildren().clear();
    int i = 0;
    for (BadgeInfo b : badges) {
      grid.add(badgeCard(b, b.id.equals(selectedId), onClick), i % COLUMNS, i / COLUMNS);
      i++;
    }
  }

  /**
   * Maps a server category label (e.g. "Cloud &amp; Level") to "achievements.category.cloud_level",
   * falling back to the raw label when no translation exists.
   */
  static String categoryLabel(String category) {
    if (category == null) {
      return "";
    }
    String key = "achievements.category." + category.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    try {
      return Messages.getBundle().getString(key);
    }
    catch (MissingResourceException e) {
      return category;
    }
  }

  private Node badgeCard(BadgeInfo b, boolean isSelected, Consumer<BadgeInfo> onClick) {
    HBox card = new HBox(10);
    card.setAlignment(Pos.CENTER_LEFT);
    card.setPadding(new Insets(8, 10, 8, 10));
    card.setStyle(isSelected ? "-fx-background-color: " + ROW_SELECTED + "; -fx-border-color: " + ACCENT + ";"
        : "-fx-background-color: " + ThemeColors.PANEL + "; -fx-border-color: " + ThemeColors.PANEL_BORDER + ";");
    card.setMaxWidth(Double.MAX_VALUE);
    VBox texts = new VBox(1);
    Label name = b.earned ? bold(b.name) : colored(b.name, MUTED, 14, true);
    String sub = b.earned ? (b.selected ? Messages.get("achievements.display_badge_short") : Messages.get("achievements.badge_earned")) : b.progressText;
    texts.getChildren().addAll(name, colored(sub, ThemeColors.SUBTLE, 11, false));
    card.getChildren().addAll(medal(b.category, b.earned, 32), texts);
    tooltip(card, b.description);
    card.setOnMouseClicked(e -> onClick.accept(b));
    card.setCursor(Cursor.HAND);
    return card;
  }
}
