package de.mephisto.vpin.commons.fx.cards;

import java.io.File;
import java.util.List;

/**
 * TEMP CLASS FOR TESTING FOR TIME BEING
 */
public interface CardData {

  String MARKER_EXTERNAL_SCORE = "!^";

  String getGameDisplayName();

  File getWheelImage();

  File getBackgroundImage();


  String getRawScore();

  List<String> getScores();

}
