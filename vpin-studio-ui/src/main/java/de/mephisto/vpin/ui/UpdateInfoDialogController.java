package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class UpdateInfoDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateInfoDialogController.class);

  @FXML
  private BorderPane center;

  @FXML
  private Button updateBtn;

  @FXML
  private Button cancelBtn;

  @FXML
  private Hyperlink kofiLink;
  private WebEngine webEngine;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUpdate(ActionEvent e) {
    Platform.runLater(() -> {
      Dialogs.openUpdateDialog();
    });
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onKofiLink() {
    Studio.browse("https://ko-fi.com/syd711");
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Parser parser = Parser.builder().build();
    String version = Studio.client.getSystemService().getVersion();
    Node document = parser.parse(download("https://raw.githubusercontent.com/syd711/vpin-studio/" + version + "/RELEASE_NOTES.md"));
    HtmlRenderer renderer = HtmlRenderer.builder().build();
    String render = renderer.render(document);
    updateBtn.managedProperty().bindBidirectional(updateBtn.visibleProperty());
    updateBtn.setVisible(false);

    try {
      WebView webview = new WebView();
      webview.setPrefSize(900, 700);
      webEngine = webview.getEngine();
      webEngine.setUserStyleSheetLocation(Studio.class.getResource("web-style.css").toString());
      webEngine.loadContent(render);
      center.setCenter(webview);
    }
    catch (Exception e) {
      LOG.error("Failed to load HTML: " + e.getMessage());
    }

    Image image6 = new Image(Studio.class.getResourceAsStream("ko-fi.png"));
    ImageView view6 = new ImageView(image6);
    view6.setPreserveRatio(true);
    view6.setFitHeight(28);
    kofiLink.setGraphic(view6);
  }

  public static String download(String downloadUrl) {
    try {
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      byte[] bytes = IOUtils.toByteArray(in);
      in.close();

      LOG.info("Downloaded " + downloadUrl);
      return new String(bytes);
    }
    catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage());
    }
    return "Failed to load release notes.";
  }

  public void setForUpdate(String version) {
    updateBtn.setVisible(true);

    try {
      Parser parser = Parser.builder().build();
      Node document = parser.parse(download("https://raw.githubusercontent.com/syd711/vpin-studio/" + version + "/RELEASE_NOTES.md"));
      HtmlRenderer renderer = HtmlRenderer.builder().build();
      String render = renderer.render(document);
      webEngine.loadContent(render);
    }
    catch (Exception e) {
      LOG.error("Failed to load release info: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to load release info: " + e.getMessage());
    }
  }

  @Override
  public void onDialogCancel() {
  }
}
