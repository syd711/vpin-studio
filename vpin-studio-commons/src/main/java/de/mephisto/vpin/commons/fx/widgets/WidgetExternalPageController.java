package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.OverlaySettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class WidgetExternalPageController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private WebView webView;

  @FXML
  private StackPane viewStack;

  public WidgetExternalPageController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webView.setZoom(0.5);
    webView.getEngine().setUserStyleSheetLocation(ServerFX.class.getResource("web-style.css").toString());

    OverlaySettings overlaySettings = ServerFX.client.getJsonPreference(PreferenceNames.OVERLAY_SETTINGS, OverlaySettings.class);
    String pageUrl = overlaySettings.getPageUrl();
    if (!StringUtils.isEmpty(pageUrl)) {
      webView.getEngine().load(pageUrl);
    }
  }

  public void refresh() {
    webView.getEngine().reload();
  }
}