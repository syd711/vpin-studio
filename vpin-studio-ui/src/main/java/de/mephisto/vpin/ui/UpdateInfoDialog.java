package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

public class UpdateInfoDialog implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(UpdateInfoDialog.class);

  @FXML
  private HTMLEditor html;

  @FXML
  private Button updateBtn;

  @FXML
  private Button cancelBtn;


  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onUpdateClick(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
    stage.close();
    Dialogs.openUpdateDialog();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
//    Parser parser = Parser.builder().build();
//    Node document = parser.parse(download("https://raw.githubusercontent.com/syd711/vpin-studio/main/RELEASE_NOTES.md"));
//    HtmlRenderer renderer = HtmlRenderer.builder().build();
//    String render = renderer.render(document);

    try {
//      WebEngine webEngine = webview.getEngine();
//      webEngine.load("<p>bubu</p>");
      html.setHtmlText("<p>bubu</p>");
    } catch (Exception e) {
      LOG.error("Failed to load HTML: " + e.getMessage());
    }
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
    } catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
    }
    return "Failed to load release notes.";
  }

  @Override
  public void onDialogCancel() {

  }
}