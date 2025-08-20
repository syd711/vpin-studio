package de.mephisto.vpin.commons.fx.cards;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Define a Layer in the card.
 * The "draw" method is implemented to configure and layout the elements
 */
public interface CardLayer {

  /**
   * Override the Node method to prevent any autosizing 
   */
  default boolean isResizable() {
    return false;
  }

  /**
   * Whether the layer can be selected and a dragbox set, true by default
   */
  default boolean isSelectable() {
    return true;
  }

  /**
   * The zoomX/zoomY are used to transform the CardTemplate coordinates into rendering coordinates
   */
  void draw(@Nonnull CardTemplate template, @Nullable CardData data, double zoomX, double zoomY) throws Exception;

  //------------------------------------------

  // methods of Node exposed via CardLayer
  boolean isVisible();
  void setVisible(boolean visible);

  double getWidth();
  void setWidth(double w);
  double getHeight();
  void setHeight(double h);

  double getLayoutX();
  double getLayoutY();
  void relocate(double w, double y);

  /**
   * Defines the x of the top left bounding box corner.
   * By default layoutX but can be overriden for adjustment
   */
  default double getLocX() {
    return getLayoutX();
  }
  /**
   * Defines the y of the top left bounding box corner.
   * By default layoutY but can be overriden for adjustment
   */
  default double getLocY() {
    return getLayoutY();
  }

  //------------------------------------------

  default Font createFont(String family, String posture, double size) {
    FontWeight fontWeight = FontWeight.findByName(posture);
    FontPosture fontPosture = FontPosture.findByName(posture);
    if (posture != null && posture.contains(" ")) {
      String[] split = posture.split(" ");
      fontWeight = FontWeight.findByName(split[0]);
      fontPosture = FontPosture.findByName(split[1]);
    }
    return Font.font(family, fontWeight, fontPosture, (int) size);
  }
}
