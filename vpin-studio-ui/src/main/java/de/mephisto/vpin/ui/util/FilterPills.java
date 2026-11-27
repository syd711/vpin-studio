package de.mephisto.vpin.ui.util;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import javax.annotation.Nullable;

/**
 * Filter pills (see UI-STYLEGUIDE.md "Pills"): rounded ToggleButtons of one ToggleGroup above a list, each with an
 * optional status dot, a label and a muted count, e.g. "All 103", "● Ready 36". Exactly one pill of a group is
 * selected, a click on the selected pill keeps it selected.
 */
public final class FilterPills {

  private static final String COUNT_KEY = "sm-filter-pill-count";

  private FilterPills() {
  }

  /**
   * @param dotColor a {@link ThemeColors} constant for the status dot, null for no dot ("All")
   */
  public static ToggleButton create(ToggleGroup group, String label, @Nullable String dotColor, long count) {
    Label text = new Label(label);
    text.getStyleClass().add("sm-filter-pill-label");
    Label countLabel = new Label(String.valueOf(count));
    countLabel.getStyleClass().add("sm-filter-pill-count");

    HBox content = new HBox(6);
    content.setAlignment(Pos.CENTER_LEFT);
    if (dotColor != null) {
      content.getChildren().add(createDot(dotColor));
    }
    content.getChildren().addAll(text, countLabel);

    ToggleButton pill = new ToggleButton();
    pill.getStyleClass().add("sm-filter-pill");
    pill.setGraphic(content);
    pill.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    pill.setMnemonicParsing(false);
    pill.setMinWidth(Region.USE_PREF_SIZE);
    pill.setToggleGroup(group);
    pill.getProperties().put(COUNT_KEY, countLabel);
    SegmentedToggles.keepSelected(pill);
    return pill;
  }

  public static void setCount(ToggleButton pill, long count) {
    Object countLabel = pill.getProperties().get(COUNT_KEY);
    if (countLabel instanceof Label) {
      ((Label) countLabel).setText(String.valueOf(count));
    }
  }

  /**
   * 8px status dot, also used for the group headers of lists filtered by pills
   */
  public static Region createDot(String color) {
    Region dot = new Region();
    dot.setMinSize(8, 8);
    dot.setMaxSize(8, 8);
    dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4;");
    return dot;
  }
}
