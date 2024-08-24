package de.mephisto.vpin.ui.dropins;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.util.DateUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class DropInContainerController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(DropInContainerController.class);

  @FXML
  private Label filenameLabel;

  @FXML
  private Label sizeLabel;

  @FXML
  private void onOpen() {

  }

  @FXML
  private void onDelete() {

  }

  @FXML
  private void onInstall() {

  }

  public void setData(@NonNull File file) {
    filenameLabel.setText(file.getName());
    filenameLabel.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");
    filenameLabel.setTooltip(new Tooltip(file.getName()));
    sizeLabel.setText("" + FileUtils.readableFileSize(file.length()) + ", created " + DateUtil.formatDateTime(new Date(file.lastModified())));
    sizeLabel.setStyle("-fx-font-size: 13px");
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {

  }
}
