package de.mephisto.vpin.commons.fx.achievements;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

/**
 * The round achievement badge medal, shared by the Studio UI and the cabinet notifications.
 */
public final class BadgeMedal {

  public static final String DEFAULT_COLOR = "#7FB2FF";
  public static final String DEFAULT_ICON = "mdi2s-star";

  public static final Map<String, String> CATEGORY_COLORS = Map.of(
      "Milestones", "#DD9646", "Prestige", "#E3B930", "Exploration", "#5CC46A", "Playtime", "#5AA9FF",
      "Special", "#FF6B5A", "Rarity", "#C77DFF", "Cloud & Level", "#7FB2FF");

  public static final Map<String, String> CATEGORY_ICONS = Map.of(
      "Milestones", "mdi2s-star", "Prestige", "mdi2c-crown", "Exploration", "mdi2m-map-marker", "Playtime", "mdi2c-clock-outline",
      "Special", "mdi2f-fire", "Rarity", "mdi2d-diamond-stone", "Cloud & Level", "mdi2c-cloud-outline");

  private BadgeMedal() {
  }

  /**
   * A round badge medal: filled with the category color when earned, dashed outline when locked.
   */
  public static StackPane create(String category, boolean earned, double size) {
    String color = category != null ? CATEGORY_COLORS.getOrDefault(category, DEFAULT_COLOR) : DEFAULT_COLOR;
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
    String iconLiteral = category != null ? CATEGORY_ICONS.getOrDefault(category, DEFAULT_ICON) : DEFAULT_ICON;
    FontIcon icon = new FontIcon();
    try {
      icon.setIconLiteral(iconLiteral);
    }
    catch (Exception e) {
      icon.setIconLiteral(DEFAULT_ICON);
    }
    icon.setIconColor(Color.web(earned ? "#111111" : "#6E6E6E"));
    icon.setIconSize((int) (size * 0.55));
    medal.getChildren().add(icon);
    return medal;
  }
}
