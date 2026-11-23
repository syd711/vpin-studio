package de.mephisto.vpin.ui.achievements;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.ui.Studio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * Widgets and colors of the achievements views (VPin Studio dark theme, see the visual concept).
 */
public final class AchievementsUI {

  public static final String CARD = "#2A2A2A";
  public static final String ROW_EVEN = "#283335";
  public static final String ROW_ODD = "#303733";
  public static final String ROW_SELECTED = "#333344";
  public static final String ACCENT = "#8C8CE0";
  public static final String ORANGE = "#DD9646";
  public static final String BLUE = "#7FB2FF";
  public static final String GREEN = "#5CC46A";
  public static final String MUTED = "#BDBDBD";
  public static final String TRACK = "#161616";

  public static final Map<String, String> RARITY_COLORS = Map.of(
      "Common", "#EDEDED", "Uncommon", "#5CC46A", "Rare", "#5AA9FF", "Epic", "#C77DFF", "Legendary", "#DD9646");

  public static final Map<String, String> CATEGORY_COLORS = Map.of(
      "Milestones", "#DD9646", "Prestige", "#E3B930", "Exploration", "#5CC46A", "Playtime", "#5AA9FF",
      "Special", "#FF6B5A", "Rarity", "#C77DFF", "Cloud & Level", "#7FB2FF");

  public static final Map<String, String> CATEGORY_ICONS = Map.of(
      "Milestones", "mdi2s-star", "Prestige", "mdi2c-crown", "Exploration", "mdi2m-map-marker", "Playtime", "mdi2c-clock-outline",
      "Special", "mdi2f-fire", "Rarity", "mdi2d-diamond-stone", "Cloud & Level", "mdi2c-cloud-outline");

  private AchievementsUI() {
  }

  public static VBox card() {
    VBox box = new VBox(8);
    box.setPadding(new Insets(14, 16, 14, 16));
    box.setStyle("-fx-background-color: " + CARD + "; -fx-background-radius: 6;");
    return box;
  }

  public static Label headline(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
    return l;
  }

  public static Label text(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
    return l;
  }

  public static Label bold(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
    return l;
  }

  public static Label muted(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 14px; -fx-text-fill: " + MUTED + ";");
    return l;
  }

  public static Label colored(String text, String color, int size, boolean bold) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: " + size + "px; -fx-text-fill: " + color + ";" + (bold ? " -fx-font-weight: bold;" : ""));
    return l;
  }

  public static Label big(String text) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Monospaced';");
    return l;
  }

  public static HBox row(int index, boolean selected) {
    HBox row = new HBox(10);
    row.setAlignment(Pos.CENTER_LEFT);
    row.setPadding(new Insets(7, 10, 7, 10));
    row.setStyle(rowStyle(index, selected));
    return row;
  }

  public static String rowStyle(int index, boolean selected) {
    if (selected) {
      return "-fx-background-color: " + ROW_SELECTED + "; -fx-border-color: transparent transparent transparent " + ACCENT + "; -fx-border-width: 0 0 0 3;";
    }
    return "-fx-background-color: " + (index % 2 == 1 ? ROW_ODD : ROW_EVEN) + ";";
  }

  public static Region spacer() {
    Region r = new Region();
    HBox.setHgrow(r, Priority.ALWAYS);
    return r;
  }

  public static Region vspacer() {
    Region r = new Region();
    VBox.setVgrow(r, Priority.ALWAYS);
    return r;
  }

  /**
   * A flat progress bar like in the concept.
   */
  public static Node progress(double pct, String color, double height) {
    StackPane track = new StackPane();
    track.setMinHeight(height);
    track.setPrefHeight(height);
    track.setMaxHeight(height);
    track.setStyle("-fx-background-color: " + TRACK + ";");
    track.setAlignment(Pos.CENTER_LEFT);
    Pane bar = new Pane();
    bar.setStyle("-fx-background-color: " + color + ";");
    bar.setMinHeight(height);
    bar.setMaxHeight(height);
    double fraction = Math.max(0, Math.min(1, pct / 100d));
    bar.maxWidthProperty().bind(track.widthProperty().multiply(fraction));
    bar.prefWidthProperty().bind(track.widthProperty().multiply(fraction));
    track.getChildren().add(bar);
    HBox.setHgrow(track, Priority.ALWAYS);
    return track;
  }

  public static Label chip(String text, String color) {
    Label l = new Label(text);
    l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8 2 8; -fx-border-color: " + color + "; -fx-text-fill: " + color + ";");
    return l;
  }

  @Nullable
  public static Node rarityChip(@Nullable String rarity) {
    if (rarity == null) {
      return null;
    }
    return chip(rarity, RARITY_COLORS.getOrDefault(rarity, MUTED));
  }

  public static Circle dot(String color, double radius) {
    Circle c = new Circle(radius);
    c.setFill(Color.web(color));
    return c;
  }

  public static FontIcon icon(String literal, String color, int size) {
    FontIcon icon = new FontIcon();
    try {
      icon.setIconLiteral(literal);
    }
    catch (Exception e) {
      icon.setIconLiteral("mdi2s-star");
    }
    icon.setIconColor(Color.web(color));
    icon.setIconSize(size);
    return icon;
  }

  /**
   * A round badge medal: filled with the category color when earned, dashed outline when locked.
   */
  public static StackPane medal(String category, boolean earned, double size) {
    String color = CATEGORY_COLORS.getOrDefault(category, BLUE);
    StackPane medal = new StackPane();
    medal.setMinSize(size, size);
    medal.setPrefSize(size, size);
    medal.setMaxSize(size, size);
    if (earned) {
      medal.setStyle("-fx-background-color: " + color + "; -fx-background-radius: " + size + ";");
    }
    else {
      medal.setStyle("-fx-background-color: #222222; -fx-background-radius: " + size + "; -fx-border-radius: " + size + "; -fx-border-color: #4A4A4A; -fx-border-style: dashed;");
    }
    medal.getChildren().add(icon(CATEGORY_ICONS.getOrDefault(category, "mdi2s-star"), earned ? "#111111" : "#6E6E6E", (int) (size * 0.55)));
    return medal;
  }

  /**
   * Circle with the level number.
   */
  public static StackPane levelCircle(int level, double size) {
    StackPane circle = new StackPane();
    circle.setMinSize(size, size);
    circle.setPrefSize(size, size);
    circle.setMaxSize(size, size);
    circle.setStyle("-fx-background-color: #3A3A3A; -fx-background-radius: " + size + "; -fx-border-radius: " + size
        + "; -fx-border-color: " + ORANGE + "; -fx-border-width: 3;");
    VBox content = new VBox(-2);
    content.setAlignment(Pos.CENTER);
    content.getChildren().addAll(colored(String.valueOf(level), "white", (int) (size * 0.36), true), colored("LEVEL", MUTED, 9, false));
    circle.getChildren().add(content);
    return circle;
  }

  /**
   * Rank number, medal colors for the first three places.
   */
  public static Node rank(int rank) {
    String[][] medals = {{"#E3B930", "#8A6E12"}, {"#C9C9C9", "#7A7A7A"}, {"#D9925A", "#8A5326"}};
    if (rank >= 1 && rank <= 3) {
      StackPane p = new StackPane();
      p.setMinSize(24, 24);
      p.setMaxSize(24, 24);
      p.setStyle("-fx-background-color: " + medals[rank - 1][0] + "; -fx-background-radius: 24; -fx-border-radius: 24; -fx-border-color: "
          + medals[rank - 1][1] + "; -fx-border-width: 2;");
      p.getChildren().add(colored(String.valueOf(rank), "#111111", 14, true));
      return p;
    }
    return bold(rank > 0 ? "#" + rank : "-");
  }

  public static StackPane initials(@Nullable String initials, double size) {
    StackPane p = new StackPane();
    p.setMinSize(size, size);
    p.setMaxSize(size, size);
    p.setStyle("-fx-background-color: #9A9A9A; -fx-background-radius: " + size + "; -fx-border-radius: " + size + "; -fx-border-color: #5A5A5A; -fx-border-width: 2;");
    p.getChildren().add(colored(initials != null ? initials : "?", "#111111", 10, true));
    return p;
  }

  public static ScrollPane scroll(Node content) {
    ScrollPane scroll = new ScrollPane(content);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    return scroll;
  }

  public static void tooltip(Node node, @Nullable String text) {
    if (text != null && !text.isEmpty()) {
      Tooltip.install(node, new Tooltip(text));
    }
  }

  /**
   * Localized badge name, falls back to the server provided name.
   */
  public static String badgeName(BadgeInfo b) {
    return localized("achievements.badge." + b.id + ".name", b.name);
  }

  /**
   * Localized badge description, falls back to the server provided description.
   */
  public static String badgeDescription(BadgeInfo b) {
    return localized("achievements.badge." + b.id + ".description", b.description);
  }

  /**
   * Localized progress of a locked badge, falls back to the server provided progress text.
   */
  @Nullable
  public static String badgeProgress(BadgeInfo b) {
    if (b.earned || b.progressKind == null) {
      return b.progressText;
    }
    return switch (b.progressKind) {
      case "count" -> Messages.get("achievements.badge_progress.count", number(b.current), number(b.target));
      case "prestige" -> Messages.get("achievements.badge_progress.prestige", b.current, b.target);
      case "hours" -> Messages.get("achievements.badge_progress.hours", b.current, b.target);
      case "best" -> Messages.get("achievements.badge_progress.best", b.current);
      case "best_minutes" -> Messages.get("achievements.badge_progress.best_minutes", b.current);
      case "flag" -> Messages.get("achievements.badge_progress.flag");
      case "level" -> Messages.get("achievements.badge_progress.level", b.current, b.target);
      default -> b.progressText;
    };
  }

  /**
   * Tooltip of a badge medal: name, description and the progress for locked badges.
   */
  public static String badgeTooltip(BadgeInfo b) {
    String progress = badgeProgress(b);
    if (b.earned || progress == null) {
      return Messages.get("achievements.badge_tooltip", badgeName(b), badgeDescription(b));
    }
    return Messages.get("achievements.badge_tooltip_locked", badgeName(b), badgeDescription(b), progress);
  }

  private static String localized(String key, @Nullable String fallback) {
    if (Messages.getBundle().containsKey(key)) {
      return Messages.get(key);
    }
    return fallback != null ? fallback : "";
  }

  /**
   * Shows or hides a node, hidden nodes are excluded from the layout.
   */
  public static void show(Node node, boolean visible) {
    node.setVisible(visible);
    node.setManaged(visible);
  }

  public static boolean isManiaAvailable() {
    return Studio.Features.MANIA_ENABLED && Studio.maniaClient != null;
  }

  public static String nextLevelText(PlayerAchievements p) {
    return p.level.maxLevel ? Messages.get("achievements.max_level")
        : Messages.get("achievements.to_next_level", number(p.level.effective), number(p.level.nextAt), p.level.nextName);
  }

  public static String date(@Nullable Date date) {
    if (date == null) {
      return "—";
    }
    return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
  }

  public static String number(long value) {
    return String.format(Locale.getDefault(), "%,d", value);
  }

  public static String pct(double value) {
    return String.format(Locale.ENGLISH, "%.1f%%", value);
  }
}
