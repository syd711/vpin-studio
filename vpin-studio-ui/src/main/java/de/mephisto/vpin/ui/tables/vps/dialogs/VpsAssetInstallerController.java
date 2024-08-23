package de.mephisto.vpin.ui.tables.vps.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class VpsAssetInstallerController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(VpsAssetInstallerController.class);

  @FXML
  private WebView aboutWebView;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {


  }

  public void setData(String link) {
    LOG.info("VPS Asset installer loads:" + link);
    try {
      Document doc = Jsoup
          .connect(link)
          .userAgent("Mozilla")
          .get();

      String html = null;
      Elements select = doc.select("h2");
      for (Element element : select) {
        String text = element.text();
        if (text.equalsIgnoreCase("About This File")) {
          html = element.parent().html();
          break;
        }
      }

      if (html != null) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head>\n");
        Elements css = doc.select("link");
        for (Element element : css) {
          if (element.attr("rel").equalsIgnoreCase("stylesheet")) {
            String href = element.attr("href");
//            builder.append("<link rel=\"stylesheet\" href=\"" + href + "\" />\n");
          }
        }

        builder.append("</head><body>\n");
        builder.append(html);
        builder.append("</body><html>");

        System.out.println(builder);


        aboutWebView.getEngine().setUserStyleSheetLocation(ServerFX.class.getResource("web-style.css").toString());
        aboutWebView.getEngine().loadContent(builder.toString());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to resolve target HTML page: " + e.getMessage(), e);
    }
  }

  @Override
  public void onDialogCancel() {

  }
}
