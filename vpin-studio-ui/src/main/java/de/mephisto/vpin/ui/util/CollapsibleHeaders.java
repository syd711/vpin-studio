package de.mephisto.vpin.ui.util;

import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;

/**
 * Helpers for the common collapsible panel (see UI-STYLEGUIDE.md, "Collapsible panels"): the header of a TitledPane
 * is a Region in its graphic, which does not grow with the pane on its own. Binding its width lets the actions on
 * the right side of the header stick to the right edge.
 */
public class CollapsibleHeaders {

  /**
   * title padding (12 + 12), chevron (10 + 12), border (1 + 1) and some air
   */
  public static final double HEADER_INSET = 52;

  public static void fill(TitledPane pane) {
    if (pane != null && pane.getGraphic() instanceof Region header) {
      header.setMinWidth(0);
      header.prefWidthProperty().bind(pane.widthProperty().subtract(HEADER_INSET));
    }
  }

  public static void fill(Accordion accordion) {
    for (TitledPane pane : accordion.getPanes()) {
      fill(pane);
    }
  }
}
