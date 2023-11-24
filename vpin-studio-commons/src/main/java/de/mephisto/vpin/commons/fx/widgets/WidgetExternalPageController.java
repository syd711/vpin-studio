package de.mephisto.vpin.commons.fx.widgets;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.restclient.PreferenceNames;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class WidgetExternalPageController extends WidgetController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetExternalPageController.class);

  @FXML
  private WebView webView;

  @FXML
  private StackPane viewStack;

  public WidgetExternalPageController() {
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    webView.setZoom(0.5);
    webView.getEngine().setUserStyleSheetLocation(OverlayWindowFX.class.getResource("web-style.css").toString());

    String pageUrl = OverlayWindowFX.client.getPreference(PreferenceNames.OVERLAY_PAGE_URL).getValue();
    if (!StringUtils.isEmpty(pageUrl)) {
      webView.getEngine().load(pageUrl);
    }
  }

  public void refresh() {
    webView.getEngine().reload();
  }
}