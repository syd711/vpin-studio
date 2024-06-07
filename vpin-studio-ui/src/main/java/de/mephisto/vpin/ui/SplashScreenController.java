package de.mephisto.vpin.ui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ThreadLocalRandom;

public class SplashScreenController implements Initializable {

  @FXML
  private Label versionLabel;

  @FXML
  private Label factLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    versionLabel.setText("Version " + Studio.getVersion());
    factLabel.setText("");
    try {
      List<String> facts = IOUtils.readLines(SplashScreenController.class.getResourceAsStream("facts.txt"), Charset.defaultCharset());
      int i = ThreadLocalRandom.current().nextInt(0, facts.size() - 1);
      String fact = facts.get(i);
      factLabel.setText("\"" + fact + "\"");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
