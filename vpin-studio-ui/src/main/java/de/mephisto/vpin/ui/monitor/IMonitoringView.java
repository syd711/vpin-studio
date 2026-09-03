package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.monitor.PlayfieldRotation;

import java.util.List;

public interface IMonitoringView {

  void dispose();

  void setZoom(double zoom);

  void refresh();

  void updateScreens(List<VPinScreen> disabledScreens);

  /**
   * Only relevant for views that render the PlayField preview rotated (StreamingView).
   * No-op for every other view.
   */
  default void setPlayfieldRotation(PlayfieldRotation rotation) {
    //not supported by default
  }
}
