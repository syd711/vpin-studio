package de.mephisto.vpin.ui.monitor;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.List;

public interface IMonitoringView {

  void dispose();

  void setZoom(double zoom);

  void refresh();

  void updateScreens(List<VPinScreen> disabledScreens);
}
