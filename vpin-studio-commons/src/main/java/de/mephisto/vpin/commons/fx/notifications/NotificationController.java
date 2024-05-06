package de.mephisto.vpin.commons.fx.notifications;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class NotificationController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(NotificationController.class);

  @FXML
  private Label title1;

  @FXML
  private Label title2;

  @FXML
  private Label title3;

  @FXML
  private ImageView logoImageView;

  @FXML
  private ImageView rowImageView;

  @FXML
  private VBox labelContainer;


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

  }

  public void setNotification(Notification notification) {
    if (notification.getImage() != null) {
      logoImageView.setImage(notification.getImage());
    }

    if (notification.getTitle1() != null) {
      title1.setText(notification.getTitle1());
    }
    else {
      title1.setText("");
    }

    if (notification.getTitle2() != null) {
      title2.setText(notification.getTitle2());
    }
    else{
      title2.setText("");
    }

    if (!StringUtils.isEmpty(notification.getTitle3())) {
      title3.setText(notification.getTitle3());
    }
    else {
      title3.setText("");
    }
  }
}
